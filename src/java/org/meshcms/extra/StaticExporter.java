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

package org.meshcms.extra;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import org.meshcms.core.Configuration;
import org.meshcms.core.FileTypes;
import org.meshcms.core.MainWebSite;
import org.meshcms.core.VirtualWebSite;
import org.meshcms.core.WebSite;
import org.meshcms.util.DirectoryParser;
import org.meshcms.util.DirectoryRemover;
import org.meshcms.util.Path;
import org.meshcms.util.Utils;

/**
 * Performs the export of a website in static files (for use with the Apache
 * web server or any other one).
 */
public class StaticExporter extends DirectoryParser {
  public static final String REQUEST_ATTRIBUTE_CHECK = "meshcms-is-exporting";
  public static final String USER_AGENT_HEADER = "User-Agent";
  
  public static final String USER_AGENT = WebSite.APP_NAME + ' ' +
      WebSite.VERSION_ID + " (" +
      System.getProperty("java.version") + ' ' +
      System.getProperty("java.vendor") + ", " +
      System.getProperty("os.name") + ' ' +
      System.getProperty("os.version") + ' ' +
      System.getProperty("os.arch") + ')';

  File staticDir;
  URL contextURL;
  boolean checkDates = true;
  Writer writer;
  WebSite webSite;

  /**
   * Creates an instance.
   *
   * @param staticDir the directory where files should be exported
   */
  public StaticExporter(WebSite webSite, URL contextURL, File staticDir) {
    super();
    setProcessStartDir(true);
    setRecursive(true);
    setInitialDir(webSite.getRootFile());
    setStaticDir(staticDir);
    this.webSite = webSite;
    this.contextURL = contextURL;
  }

  /**
   * Sets the static directory.
   */
  public void setStaticDir(File staticDir) {
    this.staticDir = staticDir;
  }

  /**
   * Returns the static directory.
   */
  public File getStaticDir() {
    return staticDir;
  }

  /**
   * Sets the context URL.
   */
  public void setContextURL(URL contextURL) {
    this.contextURL = contextURL;
  }

  /**
   * Returns the context URL.
   */
  public URL getContextURL() {
    return contextURL;
  }

  /**
   * Sets the date check to on or off. If the date check is on, only newer
   * files are copied. HTML are always recreated regardless of this option.
   * Default is true (recommended).
   */
  public void setCheckDates(boolean checkDates) {
    this.checkDates = checkDates;
  }

  /**
   * Returns the value of the date check option.
   */
  public boolean getCheckDates() {
    return checkDates;
  }

  /**
   * Sets the writer for logging (usually the writer of the web page).
   */
  public void setWriter(Writer writer) {
    this.writer = writer;
  }

  /**
   * Returns the writer (if any).
   */
  public Writer getWriter() {
    return writer;
  }

  protected void postProcess() {
    MainWebSite mainWebSite = null;
    
    if (webSite instanceof VirtualWebSite) {
      mainWebSite = ((VirtualWebSite) webSite).getMainWebSite();
    }
    
    write("");
    StaticExportCopier copier = new StaticExportCopier(staticDir);
    copier.setInitialDir(initialDir);
    copier.setWriter(writer);
    copier.setCheckDates(checkDates);
    copier.process();
    
    write("");
    StaticExportCleaner cleaner = new StaticExportCleaner(initialDir);
    cleaner.setInitialDir(staticDir);
    cleaner.setWriter(writer);
    
    if (mainWebSite != null) {
      cleaner.setProtectedPath(webSite.getAdminPath());
    }
    
    cleaner.process();

    if (mainWebSite != null) {
      File adminStaticDir = webSite.getAdminPath().getFile(staticDir);
      File adminDir = mainWebSite.getFile(mainWebSite.getAdminPath());

      write("");
      StaticExportCopier adminCopier = new StaticExportCopier(adminStaticDir);
      adminCopier.setMkDirs(true);
      adminCopier.setInitialDir(adminDir);
      adminCopier.setWriter(writer);
      adminCopier.setCheckDates(checkDates);
      adminCopier.process();
      
      write("");
      StaticExportCleaner adminCleaner = new StaticExportCleaner(adminDir);
      adminCleaner.setInitialDir(adminStaticDir);
      adminCleaner.setWriter(writer);
      adminCleaner.process();
    }
  }

  protected boolean preProcessDirectory(File file, Path path) {
    File dir = path.getFile(staticDir);

    if (isExportable(path)) {
      dir.mkdirs();
    } else {
      new DirectoryRemover(dir).process();
    }

    return dir.isDirectory();
  }

  protected void processFile(File file, Path path) {
    File staticFile = path.getFile(staticDir);
    String fileName = file.getName();

    try {
      if (FileTypes.isPage(fileName)) {
        long time = System.currentTimeMillis();
        URL url = new URL(contextURL, path.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty(USER_AGENT_HEADER, USER_AGENT);
        InputStream in = connection.getInputStream();
        OutputStream out = new FileOutputStream(staticFile);
        Utils.copyStream(in, out, true);
        connection.disconnect();
        write(path + " page generated in " + (System.currentTimeMillis() - time) + " ms");
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  void write(String message) {
    if (writer != null) {
      try {
        writer.write(message);
        writer.write('\n');
        writer.flush();
      } catch (IOException ex) {}
    }
  }
  
  public boolean isExportable(Path path) {
    if (path == null || path.isRelative()) {
      return false;
    }
    
    if (path.isRoot()) {
      return true;
    }

    if (webSite.getAdminPath() == null || 
        !(path.equals(webSite.getPrivatePath()) ||
          path.equals(webSite.getVirtualSitesPath()))) {
      String level1 = path.getElementAt(0).toLowerCase();
      return !(level1.equals("web-inf") || level1.equals("meta-inf"));
    }

    return false;
  }

  public void process() {
    // Avoid to store generated pages in cache
    Configuration conf = webSite.getConfiguration();
    int cacheType = conf.getCacheType();
    conf.setCacheType(Configuration.NO_CACHE);
    
    write("base url: " + contextURL);

    // Update the site map to make sure everything is up to date
    long time = System.currentTimeMillis();
    webSite.updateSiteMap(true);
    write("site map updated in " + (System.currentTimeMillis() - time) + " ms");

    write("");
    super.process();
    
    // Restore cache
    conf.setCacheType(cacheType);
  }
  
  public static boolean isExportRequest(HttpServletRequest request) {
    // return USER_AGENT.equals(request.getHeader("User-Agent"));
    Boolean bo = (Boolean) request.getAttribute(REQUEST_ATTRIBUTE_CHECK);
    
    if (bo == null) {
      String ua = Utils.noNull(request.getHeader(USER_AGENT_HEADER));
      bo = Boolean.valueOf(ua.startsWith(WebSite.APP_NAME));
      request.setAttribute(REQUEST_ATTRIBUTE_CHECK, bo);
    }
    
    return bo.booleanValue();
  }
}
