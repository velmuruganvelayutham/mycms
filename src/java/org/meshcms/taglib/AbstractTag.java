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

package org.meshcms.taglib;

import com.opensymphony.module.sitemesh.HTMLPage;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.RequestConstants;
import com.opensymphony.module.sitemesh.util.OutputConverter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import org.meshcms.core.HitFilter;
import org.meshcms.core.ModuleDescriptor;
import org.meshcms.core.PageAssembler;
import org.meshcms.core.UserInfo;
import org.meshcms.core.WebSite;
import org.meshcms.core.WebUtils;
import org.meshcms.util.Path;
import org.meshcms.util.Utils;

/**
 * This class works as a base for all tags of the MeshCMS custom tags library.
 */
public abstract class AbstractTag extends TagSupport implements RequestConstants {
  /**
   * Name of the page context attribute that stores the descriptions of the
   * modules contained in a page.
   */
  public static final String PAGE_MODULES = "page_modules";
  
  public static final Pattern PARAM_REGEX = Pattern.compile("([^:]+):([^:]+):(.*)");
  
  
  WebSite webSite;
  HttpServletRequest request;
  Path pagePath, pageDirPath, adminRelPath;
  UserInfo userInfo;
  boolean isEdit;
  
  /**
   * Initializes some variables, then executes <code>writeTag()</code> if the
   * page is being viewed or <code>writeEditTag()</code> if it is being edited.
   *
   * @return the value returned by {@link #getStartTagReturnValue}
   */
  public int doStartTag() throws JspException {
    request = (HttpServletRequest) pageContext.getRequest();
    webSite = (WebSite) request.getAttribute("webSite");
    pagePath = webSite.getRequestedPath(request);
    pageDirPath = webSite.getDirectory(pagePath);
    adminRelPath = webSite.getLink(webSite.getAdminPath(), pagePath);
    userInfo = (UserInfo)
        pageContext.getAttribute("userInfo", PageContext.SESSION_SCOPE);
    isEdit = HitFilter.ACTION_EDIT.equals(request.getParameter(HitFilter.ACTION_NAME)) &&
        userInfo != null && userInfo.canWrite(webSite, pagePath);
    
    try {
      if (isEdit) {
        writeEditTag();
      } else {
        writeTag();
      }
    } catch (IOException ex) {
      throw new JspException(ex);
      // pageContext.getServletContext().log("Can't write", ex);
    }
    
    return getStartTagReturnValue();
  }
  
  /**
   * Defines the return value of <code>doStartTag()</code>. This method can be
   * overridden by subclasses to change that value. The default implementation
   * returns SKIP_BODY.
   *
   * @see #doStartTag
   */
  public int getStartTagReturnValue() {
    return SKIP_BODY;
  }
  
  /**
   * Writes the contents of the tag. Subclasses will use this method to write
   * to the page.
   */
  public abstract void writeTag() throws IOException, JspException;
  
  /**
   * Writes the contents of the tag when the page is being edited. The default
   * implementation calls <code>writeTag()</code>. Subclasses can override it
   * when they behave differently while editing.
   */
  public void writeEditTag() throws IOException, JspException {
    writeTag();
  }
  
  /**
   * Copied from com.opensymphony.module.sitemesh.taglib.AbstactTag for
   * compatibility with SiteMesh
   */
  protected Page getPage() {
    Page p = (Page) pageContext.getAttribute(PAGE, PageContext.PAGE_SCOPE);
    
    if (p == null) {
      p = (Page) pageContext.getAttribute(PAGE, PageContext.REQUEST_SCOPE);
      
      if (p == null) {
        pageContext.removeAttribute(PAGE, PageContext.PAGE_SCOPE);
      } else {
        pageContext.setAttribute(PAGE, p, PageContext.PAGE_SCOPE);
      }
      
      pageContext.removeAttribute(PAGE, PageContext.REQUEST_SCOPE);
    }
    
    if (p == null) {
      // No page? Weird! Better to block the cache at least.
      WebUtils.setBlockCache(request);
    }
    
    return p;
  }
  
  /**
   * Copied from com.opensymphony.module.sitemesh.taglib.AbstactTag for
   * compatibility with SiteMesh
   */
  protected Writer getOut() {
    return OutputConverter.getWriter(pageContext.getOut());
  }
  
  ModuleDescriptor getModuleDescriptor(String location, String name) {
    ModuleDescriptor md;
    
    if (name == null) {
      Map pageModules = (Map) pageContext.getAttribute(PAGE_MODULES);
      
      if (pageModules == null) {
        pageModules = new HashMap();
        String[] modules = Utils.tokenize
            (getPage().getProperty(PageAssembler.MODULES_PARAM), ";");
        
        if (modules != null) {
          for (int i = 0; i < modules.length; i++) {
            ModuleDescriptor md0 = new ModuleDescriptor(modules[i]);
            pageModules.put(md0.getLocation(), md0);
          }
        }
        
        String head = getHead();
        Matcher m = PageHead.META_REGEX.matcher(head);
        
        while (m.find()) {
          String value = m.group(3);
          
          if (Utils.isNullOrEmpty(value)) {
            value = m.group(5);
          }
          
          if (!Utils.isNullOrEmpty(value)) {
            Matcher sm = PARAM_REGEX.matcher(value);
            
            if (sm.matches()) {
              String loc = Utils.noNull(sm.group(1)).trim();
              String prm = Utils.noNull(sm.group(2)).trim();
              String val = sm.group(3);
              
              ModuleDescriptor md0 = (ModuleDescriptor) pageModules.get(loc);
              
              if (md0 == null) {
                md0 = new ModuleDescriptor();
                md0.setLocation(loc);
                pageModules.put(loc, md0);
              }
              
              if (prm.equals(ModuleDescriptor.TEMPLATE_ID)) {
                md0.setTemplate(val);
              } else if (prm.equals(ModuleDescriptor.ARGUMENT_ID)) {
                md0.setArgument(val);
              } else if (prm.equals(ModuleDescriptor.TITLE_ID)) {
                md0.setTitle(val);
              } else {
                md0.setAdvancedParam(prm, val);
              }
            }
          }
        }
        
        Iterator iter = pageModules.keySet().iterator();
        
        while (iter.hasNext()) {
          Object key = iter.next();
          ModuleDescriptor md0 = (ModuleDescriptor) pageModules.get(key);
          
          if (!md0.isValid()) {
            pageModules.remove(key);
          }
        }
        
        pageContext.setAttribute(PAGE_MODULES, pageModules, PageContext.PAGE_SCOPE);
      }
      
      md = (ModuleDescriptor) pageModules.get(location);
    } else {
      md = new ModuleDescriptor(name);
      md.setLocation(location);
    }
    
    return md;
  }

  public String getHead() {
    return Utils.noNull(((HTMLPage) getPage()).getHead());
  }
  
  public String getMailFormAddress() {
    String value = null;
    String head = getHead();
    Matcher m = MailForm.META_REGEX.matcher(head);
    
    if (m.find()) {
      value = m.group(3);
      
      if (Utils.isNullOrEmpty(value)) {
        value = m.group(5);
      }
    }  else {
      value = getPage().getProperty(PageAssembler.EMAIL_PARAM);
    }
    
    return value;
  }
}
