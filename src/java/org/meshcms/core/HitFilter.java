/*
 * Copyright 2004-2009 Luciano Vernaschi
 *
 * This file is part of MeshCMS.
 *
 * MeshCMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MeshCMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MeshCMS.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.meshcms.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.meshcms.util.Path;
import org.meshcms.util.Utils;

/**
 * Filter used to handle the requests for web pages.
 */
public final class HitFilter implements Filter {
  /**
   * Name of a cache file in the repository.
   */
  public static final String CACHE_FILE_NAME = "_cache.gz";
  
  /**
   * Name of the request attribute that contains the name of the current theme
   * file.
   *
   * @see RequestDecoratorMapper
   */
  public static final String THEME_FILE_ATTRIBUTE = "meshcmstheme";
  
  /**
   * Name of the request attribute that contains the name of the current theme
   * folder.
   */
  public static final String THEME_PATH_ATTRIBUTE = "meshcmsthemepath";
  
  public static final String LOCALE_ATTRIBUTE = "meshcmslocale";
  
  public static final String LAST_MODIFIED_ATTRIBUTE = "meshcmslastmodified";
  
  public static final String BLOCK_CACHE_ATTRIBUTE = "meshcmsnocache";
  
  public static final String WEBSITE_ATTRIBUTE = "webSite";
  
  /**
   * Name of the session attribute that allows hotlinking within the session
   * itself.
   */
  public static final String HOTLINKING_ALLOWED = "meshcmshotlinkingallowed";
  
  /**
   * Name of the request parameter that is used to specify some actions.
   * Currently only {@link #ACTION_EDIT} is used as value. This parameter is
   * read by custom JSP tags.
   */
  public static final String ACTION_NAME = "meshcmsaction";
  
  /**
   * Value of {@link #ACTION_NAME} used to indicate that the current page
   * must be edited.
   */
  public static final String ACTION_EDIT = "edit";
  
  public static final String ROOT_WEBSITE = "meshcmsrootsite";
  
  private FilterConfig filterConfig = null;
  
  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;
  }
  
  public void destroy() {
    this.filterConfig = null;
  }
  
  /**
   * This filter manages a page to make sure it is served correctly.
   */
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    if (filterConfig != null && (request instanceof HttpServletRequest)) {
      ServletContext sc = filterConfig.getServletContext();
      HttpServletResponse httpRes = (HttpServletResponse) response;
      WebSite rootSite = getRootSite(sc, false);
      WebSite webSite = rootSite.getWebSite(request);
      
      if (webSite == null) {
        httpRes.sendError(HttpServletResponse.SC_FORBIDDEN, "Site not found");
        return;
      }
      
      request.setAttribute(WEBSITE_ATTRIBUTE, webSite);
      request.setCharacterEncoding(Utils.SYSTEM_CHARSET);
      HttpServletRequest httpReq = webSite.wrapRequest(request);
      Path pagePath = webSite.getRequestedPath(httpReq);
      
      /* This is needed to avoid source code disclosure in virtual sites */
      if (webSite instanceof VirtualWebSite) {
        String uri = httpReq.getRequestURI().toLowerCase();
        
        if (uri.endsWith(".jsp/")) {
          httpRes.sendError(HttpServletResponse.SC_NOT_FOUND);
          return;
        }
        
        if (! pagePath.isContainedIn(webSite.getAdminPath()) &&
            uri.endsWith(".jsp") && !WebUtils.verifyJSP(webSite, pagePath)) {
          httpRes.sendError(HttpServletResponse.SC_FORBIDDEN,
              "Execution of this page is not allowed");
          return;
        }
      }
      
      if (webSite.isDirectory(pagePath)) {
        Path wPath = webSite.findCurrentWelcome(pagePath);
        Configuration conf = webSite.getConfiguration();
        
        if (conf == null || conf.isAlwaysDenyDirectoryListings()) {
          if (wPath != null) {
            redirect(httpReq, httpRes, wPath);
          } else {
            httpRes.sendError(HttpServletResponse.SC_FORBIDDEN,
                "Directory listing denied");
          }
          
          return;
        }
      }
      
      SiteMap siteMap = null;
      PageInfo pageInfo = null;
      boolean isAdminPage = false;
      boolean isGuest = true;
      String pageCharset = null;
      
      if (webSite.getCMSPath() != null) {
        if (pagePath.isContainedIn(webSite.getVirtualSitesPath()) ||
            pagePath.isContainedIn(webSite.getPrivatePath())) {
          httpRes.sendError(HttpServletResponse.SC_NOT_FOUND);
          return;
        }
        
        siteMap = webSite.getSiteMap();
        isAdminPage = pagePath.isContainedIn(webSite.getAdminPath());
        HttpSession session = httpReq.getSession();
        
        if (webSite.getConfiguration().isSearchMovedPages() &&
            !(isAdminPage || webSite.getFile(pagePath).exists())) {
          Path redirPath = siteMap.getRedirMatch(pagePath);
          
          if (redirPath != null) {
            blockRemoteCaching(httpRes);
            httpRes.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            httpRes.setHeader("Location", httpReq.getContextPath() + "/" + redirPath);
            return;
          }
        }
        
        UserInfo userInfo = (session == null) ? null :
          (UserInfo) session.getAttribute("userInfo");
        isGuest = userInfo == null || userInfo.isGuest();

        // See if should redirect to one of the available languages
        if (isGuest && webSite.getConfiguration().isRedirectRoot() &&
            siteMap.getPathInMenu(pagePath).isRoot()) {
          Path redirPath = getPreferredLanguage(httpReq);
          
          if (redirPath != null) {
            WebUtils.setBlockCache(httpReq);
            redirect(httpReq, httpRes, redirPath);
            return;
          }
        }
        
        // Deal with all pages
        if (FileTypes.isPage(pagePath.getLastElement())) {
          // Block direct requests of modules from non authenticated users
          /* if (isGuest && webSite.isInsideModules(pagePath) &&
              pagePath.getLastElement().equalsIgnoreCase(SiteMap.MODULE_INCLUDE_FILE)) {
            httpRes.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
          } */
          
          // disallow access to guests if required by configuration
          if (isGuest && webSite.getConfiguration().isPasswordProtected() &&
              !pagePath.isChildOf(webSite.getAdminPath())) {
            httpRes.sendError(HttpServletResponse.SC_FORBIDDEN,
                "You don't have enough privileges");
            return;
          }
          
          WebUtils.updateLastModifiedTime(httpReq, webSite.getFile(pagePath));
          blockRemoteCaching(httpRes);
          
          // Find a theme for this page
          Path themePath = null;
          String themeParameter = request.getParameter(THEME_FILE_ATTRIBUTE);
          
          if (themeParameter != null) {
            themePath = (Path) siteMap.getThemesMap().get(themeParameter);
          }
          
          if (themePath == null || !webSite.getFile(themePath).exists()) {
            themePath = webSite.getThemePath(pagePath);
          }
          
          if (themePath != null) { // there is a theme for this page
            if (themePath.isContainedIn(webSite.getCustomThemesPath()) &&
                !WebUtils.verifyJSP(webSite, themePath.add("main.jsp"))) {
              if (isAdminPage) {
                webSite.setLastAdminThemeBlock(System.currentTimeMillis());
              }
              
              httpRes.sendError(HttpServletResponse.SC_FORBIDDEN,
                  "Current theme is not allowed");
              return;
            }
            
            request.setAttribute(THEME_PATH_ATTRIBUTE, themePath);
            
            // pages in /admin do not need a decorator to be specified:
            if (!isAdminPage || themeParameter != null) {
              request.setAttribute(THEME_FILE_ATTRIBUTE, "/" +
                  webSite.getServedPath(themePath) + "/" +
                  SiteMap.THEME_DECORATOR);
            }
          }
          
          /* Since a real page has been requested, disable hotlinking prevention
             for this session
          if (session != null && webSite.getConfiguration().isPreventHotlinking() &&
              session.getAttribute(HOTLINKING_ALLOWED) == null && !isAdminPage) {
            session.setAttribute(HOTLINKING_ALLOWED, HOTLINKING_ALLOWED);
          } */
        }
        
        if (webSite.getConfiguration().isPreventHotlinking() &&
            FileTypes.isPreventHotlinking(pagePath.getLastElement()) /*&&
            (session == null || session.getAttribute(HOTLINKING_ALLOWED) == null)*/) {
          String agent = httpReq.getHeader("user-agent");
          
          if (agent == null || agent.toLowerCase().indexOf("java") < 0) {
            try {
              String domain = WebUtils.get2ndLevelDomain(httpReq);
              
              if (domain != null) {
                String referrer = httpReq.getHeader("referer");
                
                try {
                  referrer = new URL(referrer).getHost();
                } catch (Exception ex) {
                  referrer = null;
                }
                
                if (referrer == null || referrer.indexOf(domain) < 0) {
                  httpRes.sendRedirect(httpReq.getContextPath() + "/" +
                      webSite.getAdminPath() + "/hotlinking.jsp?path=" + pagePath);
                  return;
                }
              }
            } catch (MalformedURLException ex) {}
          }
        }
        
        pageInfo = siteMap.getPageInfo(pagePath);
        
        if (pageInfo != null) { // this page is contained in the site map
          if (isGuest) {
            pageInfo.addHit();
          }
          
          if (pageInfo.getCharset() != null) {
            pageCharset = pageInfo.getCharset();
          }
        } else { // not a page in the site map
          // Let's try to apply the right charset to JavaScript lang files
          if (isAdminPage && pagePath.getLastElement().endsWith(".js") &&
              userInfo != null && userInfo.canDo(UserInfo.CAN_BROWSE_FILES)) {
            String script = null;
            
            if (pagePath.isContainedIn(webSite.getAdminScriptsPath().add("tiny_mce"))) {
              script = "TinyMCE";
            } else if (pagePath.isContainedIn(webSite.getAdminScriptsPath().add("jscalendar"))) {
              script = "DHTMLCalendar";
            }
            
            if (script != null) {
              Locale locale = Utils.getLocale(userInfo.getPreferredLocaleCode());
              ResourceBundle bundle =
                  ResourceBundle.getBundle("org/meshcms/webui/Locales", locale);
              String s = bundle.getString(script + "LangCode");
              
              if (!Utils.isNullOrEmpty(s) && pagePath.getLastElement().equals(s + ".js")) {
                s = bundle.getString(script + "LangCharset");
                
                if (!Utils.isNullOrEmpty(s)) {
                  pageCharset = s;
                }
              }
            }
          } // end of JavaScript stuff
        }
      } // end of CMS stuff
      
      String mimeType = sc.getMimeType(pagePath.getLastElement());
      
      if (mimeType == null) {
        mimeType = "text/html";
      }
      
      httpRes.setContentType(mimeType + "; charset=" +
          Utils.noNull(pageCharset, Utils.SYSTEM_CHARSET));
      
      try {
        // Cache management
        if (isGuest && pageInfo != null &&
            httpReq.getMethod().equalsIgnoreCase("get") &&
            Utils.isNullOrEmpty(httpReq.getQueryString()) &&
            webSite.isVisuallyEditable(pagePath)) {
          int cacheType = webSite.getConfiguration().getCacheType();
          
          // Let's see if the browser supports GZIP
          String ae = httpReq.getHeader("Accept-Encoding");
          boolean gzip = ae != null && ae.toLowerCase().indexOf("gzip") > -1;
          InputStream in = null;
          
          if (cacheType == Configuration.IN_MEMORY_CACHE ||
              cacheType == Configuration.MIXED_CACHE) {
            byte[] pageBytes = siteMap.getCached(pageInfo.getPath());
            
            // a cached page too small is suspicious
            if (pageBytes != null && pageBytes.length > 256) {
              in = new ByteArrayInputStream(pageBytes);
            }
          }
          
          if (cacheType == Configuration.ON_DISK_CACHE ||
              (in == null && cacheType == Configuration.MIXED_CACHE)) {
            File cacheFile = WebUtils.getCacheFile(webSite, siteMap, pagePath);
            
            if (cacheFile != null) {
              in = new FileInputStream(cacheFile);
              
              if (cacheType == Configuration.MIXED_CACHE) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Utils.copyStream(in, baos, true);
                byte[] b = baos.toByteArray();
                siteMap.cache(pageInfo.getPath(), b);
                in = new ByteArrayInputStream(b);
              }
            }
          }
          
          // if a valid cached version has been found, use it
          if (in != null) {
            ServletOutputStream sos = response.getOutputStream();
            
            if (gzip) {
              httpRes.setHeader("Content-Encoding", "gzip");
            } else {
              // uncompress the page on the fly for that spider or old browser
              in = new GZIPInputStream(in);
            }
            
            Utils.copyStream(in, sos, false);
            sos.flush();
            return;
          }
          
          // otherwise, if cache is enabled, store the generated page
          if (cacheType != Configuration.NO_CACHE) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStream os = new GZIPOutputStream(baos);
            CacheResponseWrapper wrapper = new CacheResponseWrapper(httpRes, os);
            chain.doFilter(httpReq, wrapper);
            wrapper.finishResponse();
            // os.flush();
            
            /* If WebUtils.setBlockCache has not been called while creating
               the page, it can be cached */
            if (!WebUtils.isCacheBlocked(httpReq)) {
              if (cacheType == Configuration.IN_MEMORY_CACHE ||
                  cacheType == Configuration.MIXED_CACHE) {
                siteMap.cache(pageInfo.getPath(), baos.toByteArray());
              }
              
              if (cacheType == Configuration.ON_DISK_CACHE ||
                  cacheType == Configuration.MIXED_CACHE) {
                File cacheFile = webSite.getRepositoryFile
                    (siteMap.getServedPath(pagePath), CACHE_FILE_NAME);
                cacheFile.getParentFile().mkdirs();
                Utils.writeFully(cacheFile, baos.toByteArray());
              }
            }
            
            return;
          }
        } // end of cache management
        
        chain.doFilter(httpReq, httpRes);
        webSite.updateSiteMap(false); // better here than nowhere :)
      } catch (Exception ex) {
        if (isAdminPage) {
          webSite.setLastAdminThemeBlock(System.currentTimeMillis());
        }
        
        Path wPath = webSite.findCurrentWelcome(pagePath);
        
        if (wPath != null && !wPath.equals(pagePath)) {
          redirect(httpReq, httpRes, wPath);
          return;
        }
        
        sc.log("--------\n\nIMPORTANT: an exception has been caught while serving " +
            httpReq.getRequestURI(), ex);
        
        Configuration c = webSite.getConfiguration();
        
        if (c == null || c.isHideExceptions()) {
          httpRes.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } else {
          throw new ServletException(ex);
        }
      }
      
      return;
    }
    
    // should never be reached
    chain.doFilter(request, response);
  }
  
  private static void redirect(HttpServletRequest httpReq,
      HttpServletResponse httpRes, Path redirPath) throws IOException {
    blockRemoteCaching(httpRes);
    String q = httpReq.getQueryString();
    
    if (Utils.isNullOrEmpty(q)) {
      httpRes.sendRedirect(httpReq.getContextPath() + "/" + redirPath);
    } else {
      httpRes.sendRedirect(httpReq.getContextPath() + "/" + redirPath + '?' + q);
    }
  }
  
  /**
   * Returns the main website instance. It will be created if not already done.
   */
  public static WebSite getRootSite(ServletContext sc, boolean alwaysCreate) {
    WebSite rootSite = (WebSite) sc.getAttribute(ROOT_WEBSITE);
    
    if (rootSite == null || alwaysCreate) {
      File rootFile = new File(sc.getRealPath("/"));
      Path cmsPath = new CMSDirectoryFinder(rootFile, false).getCMSPath();
      boolean multisite = false;
      File sitesDir = new File(rootFile, cmsPath + "/sites");
      
      if (sitesDir.isDirectory()) {
        File[] dirs = sitesDir.listFiles();
        
        for (int i = 0; i < dirs.length; i++) {
          if (dirs[i].isDirectory()) {
            multisite = true;
          }
        }
      }
      
      rootSite = multisite ?
        MainWebSite.create(sc, WebUtils.getWelcomeFiles(sc), rootFile,
          Path.ROOT, cmsPath) :
        WebSite.create(sc, WebUtils.getWelcomeFiles(sc), rootFile,
          Path.ROOT, cmsPath);
      sc.setAttribute(ROOT_WEBSITE, rootSite);
    }
    
    return rootSite;
  }
  
  /**
   * Sets some headers to discourage remote caching of pages.
   */
  public static void blockRemoteCaching(HttpServletResponse httpRes) {
    // HTTP 1.1
    httpRes.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    // HTTP 1.0
    httpRes.setHeader("Pragma", "no-cache");
    // prevents caching at the proxy server
    httpRes.setDateHeader("Expires", -1);
  }
  
  public static Path getPreferredLanguage(HttpServletRequest request) throws IOException {
    WebSite webSite = (WebSite) request.getAttribute(HitFilter.WEBSITE_ATTRIBUTE);
    List available = webSite.getSiteMap().getLangList();
    String[] accepted = WebUtils.getAcceptedLanguages(request);
    SiteMap.CodeLocalePair chosen = null;
    
    if (available != null && available.size() > 0) {
      for (int i = 0; chosen == null && i < accepted.length; i++) {
        Iterator iter = available.iterator();
        
        while (chosen == null && iter.hasNext()) {
          SiteMap.CodeLocalePair clp = (SiteMap.CodeLocalePair) iter.next();
          
          if (clp.getCode().equalsIgnoreCase(accepted[i])) {
            chosen = clp;
          }
        }
      }
      
      if (chosen == null) {
        chosen = (SiteMap.CodeLocalePair) available.get(0);
      }
      
      return new Path(chosen.getCode());
    }
    
    return null;
  }
}
