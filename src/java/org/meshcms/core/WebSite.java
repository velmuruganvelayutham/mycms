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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.meshcms.util.DirectoryRemover;
import org.meshcms.util.Path;
import org.meshcms.util.Utils;
import org.meshcms.util.XStreamPathConverter;
import org.meshcms.util.ZipArchiver;

public class WebSite {
  public static final String APP_NAME = "MeshCMS";
  public static final String VERSION_ID = "3.6";

  /**
   * A prefix to be used for every backup file.
   */
  public static final String BACKUP_PREFIX = "_bak_";

  /**
   * A prefix to be used for every backup dir.
   */
  public static final String BACKUP_DIR_PREFIX = "_dirbak_";

  /**
   * A prefix to be used for every temporary file created in the repository.
   */
  public static final String TEMP_PREFIX = "_tmp_";

  /**
   * Name of the default admin theme folder.
   */
  public static final String ADMIN_THEME = "theme";

  public static final String CMS_ID_FILE = "meshcms_id";

  public static final String ADMIN_ID_FILE = "meshcms_admin_id";

  protected ServletContext sc;
  protected String[] welcomeFiles;

  protected File rootFile;
  protected long lastAdminThemeBlock;
  protected long statsZero;
  protected int statsLength;
  protected Configuration configuration;
  protected SiteInfo siteInfo;
  protected SiteMap siteMap;
  protected Path rootPath;

  protected Path cmsPath;
  protected Path adminPath;
  protected Path adminThemePath;
  protected Path adminModulesPath;
  protected Path adminThemesPath;
  protected Path adminScriptsPath;
  protected Path privatePath;
  protected Path usersPath;
  protected Path virtualSitesPath;
  protected Path repositoryPath;
  protected Path customModulesPath;
  protected Path generatedFilesPath;
  protected Path moduleDataPath;
  protected Path customThemesPath;

  protected Path configFilePath;
  protected Path propertiesFilePath;
  protected Path sitesFilePath;

  protected WebSite() {
  }

  protected static WebSite create(ServletContext sc,
      String[] welcomeFiles, File rootFile, Path rootPath, Path cmsPath) {
    WebSite webSite = new WebSite();
    webSite.init(sc, welcomeFiles, rootFile, rootPath, cmsPath);
    return webSite;
  }

  protected void init(ServletContext sc, String[] welcomeFiles, File rootFile,
      Path rootPath, Path cmsPath) {
    this.sc = sc;
    this.welcomeFiles = welcomeFiles;
    this.rootFile = rootFile;
    this.rootPath = rootPath;
    this.cmsPath = cmsPath;

    // PLEASE NOTE: the initialization order is important.
    if (cmsPath != null) {
      adminPath = cmsPath.add("admin");
      adminThemePath = adminPath.add(ADMIN_THEME);
      adminModulesPath = adminPath.add("modules");
      adminThemesPath = adminPath.add("themes");
      adminScriptsPath = adminPath.add("scripts");
      privatePath = cmsPath.add("private");
      createDir(usersPath = privatePath.add("users"));
      virtualSitesPath = cmsPath.add("sites");
      createDir(repositoryPath = privatePath.add("repository"));
      createDir(customModulesPath = cmsPath.add("modules"));
      createDir(generatedFilesPath = cmsPath.add("generated"));
      createDir(moduleDataPath = cmsPath.add("moduledata"));
      createDir(customThemesPath = cmsPath.add("themes"));
      configFilePath = privatePath.add("configuration.xml");
      propertiesFilePath = privatePath.add("siteinfo.xml");
      sitesFilePath = privatePath.add("sites.xml");

      readConfig();
      statsLength = configuration.getStatsLength();
      updateSiteMap(true);
    }
  }

  public void readConfig() {
    configuration = Configuration.load(this);
    siteInfo = SiteInfo.load(this);
  }

  protected ServletContext getServletContext() {
    return sc;
  }

  /**
   * Creates another instance of <code>SiteMap</code>. If <code>force</code>
   * is true, a new site map is always created and the method
   * returns after the new site map is completed. If it is false, a new site map
   * is created only if the current one is too old. In this case, the site map
   * is created asynchronously and the method returns immediately. The
   * repository will be cleaned too.
   *
   * @param force it to force the SiteMap creation.
   */
  public void updateSiteMap(boolean force) {
    if (force) {
      new SiteMap(this).process();
    } else if (System.currentTimeMillis() - siteMap.getLastModified() >
               configuration.getUpdateIntervalMillis()) {
    	new SiteMap(this).start();
      new DirectoryCleaner(getFile(repositoryPath),
          configuration.getBackupLifeMillis()).start();
      new DirectoryCleaner(getFile(generatedFilesPath),
          configuration.getBackupLifeMillis()).start();
      new DirectoryCleaner(getFile(moduleDataPath)).start();
    }
  }

  void setSiteMap(SiteMap siteMap) {
    this.siteMap = siteMap;
  }

  /**
   * Returns the instance of the <code>SiteMap</code> that is currently manage
   * the site map. Since this object can be replaced with a new one at any
   * moment, a class that wants to use it should store it in a local variable
   * and use it for all the operation/method.
   *
   * @return the current instance of SiteMap
   */
  public SiteMap getSiteMap() {
    return siteMap;
  }

  /**
   * Returns the current configuration of the web application.
   */
  public Configuration getConfiguration() {
    return configuration;
  }

  /**
   * Sets the <code>SiteInfo</code> object related to this website.
   *
   * @param siteInfo the site info for this website
   */
  void setSiteInfo(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }

  /**
   * Returns the instance of the <code>SiteInfo</code> class that is managing
   * the site information.
   *
   * @see SiteInfo
   */
  public SiteInfo getSiteInfo() {
    return siteInfo;
  }

  /**
   * Returns the index of the current day in the array of stats included in any
   * PageInfo instance.
   */
  public int getStatsIndex() {
    long now = System.currentTimeMillis();
    long days = (now - statsZero) / Configuration.LENGTH_OF_DAY;

    if (days >= statsLength) {
      statsZero = now;
      return 0;
    } else {
      return (int) days;
    }
  }

  /**
   * Returns the length of stats (hit counts) measured in days.
   */
  public int getStatsLength() {
    return statsLength;
  }

  /**
   * Creates a new directory.
   *
   * @param user the user that requests the creation of the directory
   * @param dirPath the path of the new directory
   *
   * @return true if the directory has been created or already
   * existed, false otherwise
   */
  public boolean createDirectory(UserInfo user, Path dirPath) {
    File newDir = getFile(dirPath);

    if (newDir.isDirectory()) {
      return true;
    }

    return user != null && user.canWrite(this, dirPath) && newDir.mkdir();
  }

  /**
   * Creates a new file. If the extension of the file denotes a web page, the
   * basic template is copied into the file, otherwise an empty file is created.
   *
   * @param user the user that requests the file creation
   * @param filePath the path of the new file
   *
   * @return true if the new file has been created or already existed,
   * false otherwise
   */
  public boolean createFile(UserInfo user, Path filePath) {
    if (user == null || !user.canWrite(this, filePath)) {
      return false;
    }

    File newFile = getFile(filePath);

    if (newFile.exists()) {
      return !newFile.isDirectory();
    }

    try {
      if (FileTypes.isPage(filePath.getLastElement())) {
        if (newFile.exists()) {
          return false;
        }

        return saveToFile(user, getHTMLTemplate(null), filePath);
      } else {
        return newFile.createNewFile();
      }
    } catch (IOException ex) {
      sc.log("Can't create file " + newFile, ex);
    }

    return false;
  }

  /**
   * Copies a file to another file in the same directory. An existing file won't
   * be overwritten.
   *
   * @param user the user that requests the operation
   * @param filePath the path of the old file
   * @param newName the name of the new file
   *
   * @return true if the new file has been copied, false otherwise
   */
  public boolean copyFile(UserInfo user, Path filePath, String newName) {
    return copyFile(user, filePath, filePath.getParent().add(newName));
  }

  /**
   * Copies a file (or directory) to another file (or directory).
   * Existing files won't be overwritten.
   *
   * @param user the user that requests the operation
   * @param oldPath the location of the existing file
   * @param newPath the location of the new copy of the file
   *
   * @return true if the new file has been copied, false otherwise
   */
  public boolean copyFile(UserInfo user, Path oldPath, Path newPath) {
    File oldFile = getFile(oldPath);

    if (!oldFile.exists()) {
      return false;
    }

    File newFile = getFile(newPath);

    if (user == null || !user.canWrite(this, newPath)) {
      return false;
    }

    if (oldFile.isDirectory()) {
      return Utils.copyDirectory(oldFile, newFile, false, false, false);
    } else {
    try {
      return Utils.copyFile(oldFile, newFile, false, false);
    } catch (IOException ex) {
      sc.log("Can't copy file " + oldFile + " to file " + newFile, ex);
      }
    }

    return false;
  }

  /**
   * Renames a file.
   *
   * @param user the user that requests the operation
   * @param filePath the path of the file
   * @param newName the name of the new file
   *
   * @return true if the new file has been renamed, false otherwise
   */
  public boolean rename(UserInfo user, Path filePath, String newName) {
    return move(user, filePath, filePath.getParent().add(newName));
  }

  /**
   * Moves (or renames) a file.
   *
   * @param user the user that requests the operation
   * @param oldPath the current location of the file
   * @param newPath the new location of the file
   *
   * @return true if the new file has been moved, false otherwise
   */
  public boolean move(UserInfo user, Path oldPath, Path newPath) {
    File oldFile = getFile(oldPath);

    if (!oldFile.exists()) {
      return false;
    }

    if (newPath.isContainedIn(oldPath)) {
      return false;
    }

    if (user == null ||
        !(user.canWrite(this, oldPath) && user.canWrite(this, newPath))) {
      return false;
    }

    File newFile = getFile(newPath);

    if (Utils.forceRenameTo(oldFile, newFile, false)) {
      return true;
    } else {
      sc.log("Can't move file " + oldFile + " to file " + newFile);
    }

    return false;
  }

  /**
   * Deletes a file or directory.
   *
   * @param user the user that requests the operation
   * @param filePath the path of the file
   * @param deleteNonEmptyDirectories if true, non-empty directories will be
   * deleted too
   *
   * @return true if the file has been deleted, false otherwise
   */
  public boolean delete(UserInfo user, Path filePath,
      boolean deleteNonEmptyDirectories) {
    if (user == null || !user.canWrite(this, filePath)) {
      return false;
    }

    // avoid deletion of mandatory CMS items
    if (cmsPath.isContainedIn(filePath) ||
        filePath.isChildOf(cmsPath) ||
        filePath.isContainedIn(adminPath) ||
        filePath.isChildOf(privatePath)) {
      return false;
    }

    File file = getFile(filePath);

    if (!file.exists()) {
      return false;
    }

    if (file.isDirectory()) {
      /* First try to delete a directory as if it were empty. If not, and if
         deletion of non-empty directories is allowed, backup and delete with backupDir */
      return file.delete() ||
          (deleteNonEmptyDirectories && backupDir(user, filePath));
    } else {
      return backupFile(user, filePath);
    }
  }

  /**
   * Sets the last modified date of the file to the current time.
   *
   * @param user the user that requests the operation
   * @param filePath the path of the file
   *
   * @return true if the date has been changed, false otherwise
   */
  public boolean touch(UserInfo user, Path filePath) {
    return setFileTime(user, filePath, System.currentTimeMillis());
  }

  public boolean setFileTime(UserInfo user, Path filePath, long time) {
    if (user == null || !user.canWrite(this, filePath)) {
      return false;
    }

    File file = getFile(filePath);

    if (!file.exists()) {
      return false;
    }

    file.setLastModified(time);
    return true;
  }

  /**
   * Stores an object into a file. Supported objects are:
   *
   * <ul>
   *  <li>byte arrays</li>
   *  <li>input streams</li>
   *  <li><code>org.apache.commons.fileupload.FileItem</code>
   *      (uploaded files)</li>
   *  <li>generic objects. The <code>toString()</code> method is used in this
   *      cases. This is compatible with many kinds of objects: strings,
   *      string buffers and so on.</li>
   * </ul>
   *
   * @param user the user that requests the operation
   * @param saveThis the object to be stored in the file
   * @param filePath the path of the file to be written. If the file exists, it
   * will be backed up and overwritten
   *
   * @return true if the operation has been completed successfully,
   * false otherwise
   */
  public boolean saveToFile(UserInfo user, Object saveThis, Path filePath) {
    if (user == null || !user.canWrite(this, filePath)) {
      return false;
    }

    File file = getFile(filePath);
    File dir = file.getParentFile();
    dir.mkdirs();
    File tempFile = null;

    String fileName = file.getName();
    int dot = fileName.lastIndexOf('.');
    String fileExt = (dot == -1) ? ".bak" : fileName.substring(dot);

    if (file.exists()) {
      tempFile = getRepositoryFile(filePath, TEMP_PREFIX +
          System.currentTimeMillis() + fileExt);
    }

    File writeTo = tempFile == null ? file : tempFile;

    if (saveThis instanceof byte[]) {
      byte[] b = (byte[]) saveThis;
      FileOutputStream fos = null;

      try {
        fos = new FileOutputStream(writeTo);
        fos.write(b);
      } catch (IOException ex) {
        sc.log("Can't write byte array to file " + writeTo, ex);
        return false;
      } finally {
        if (fos != null) {
          try {
            fos.close();
          } catch (IOException ex) {
            sc.log("Can't close file " + writeTo, ex);
          }
        }
      }
    } else if (saveThis instanceof InputStream) {
      try {
        InputStream is = (InputStream) saveThis;
        Utils.copyStream(is, new FileOutputStream(writeTo), true);
        is.close();
      } catch (Exception ex) {
        sc.log("Can't write stream to file " + writeTo, ex);
        return false;
      }
    } else if (saveThis instanceof FileItem) {
      try {
        ((FileItem) saveThis).write(writeTo);
      } catch (Exception ex) {
        sc.log("Can't write uploaded file to file " + writeTo, ex);
        return false;
      }
    } else {
      Writer writer = null;

      try {
        writer = new BufferedWriter(new OutputStreamWriter
            (new FileOutputStream(writeTo), Utils.SYSTEM_CHARSET));
        writer.write(saveThis.toString());
      } catch (IOException ex) {
        sc.log("Can't write generic object to file " + writeTo, ex);
        return false;
      } finally {
        if (writer != null) {
          try {
            writer.close();
          } catch (IOException ex) {
            sc.log("Can't close file " + writeTo, ex);
          }
        }
      }
    }

    if (tempFile != null) {
      if (!backupFile(user, filePath)) {
        return false;
      }

      if (!Utils.forceRenameTo(tempFile, file, true)) {
        sc.log("Can't rename temporary file " + tempFile + " to file " + file);
        return false;
      }
    }

    return true;
  }

  /**
   * Returns the correct file in the repository. For example, if you need to
   * create a temporary copy of /somedir/index.html, you could use
   * <code>filePath = /somedir/index.html</code> and
   * <code>fileName = tmp.html</code>.
   *
   * @param filePath the path where to search
   * @param fileName the file name
   *
   * @return the searched file
   */
  public File getRepositoryFile(Path filePath, String fileName) {
    File repoDir = getFile(repositoryPath.add(filePath));
    repoDir.mkdirs();
    return new File(repoDir, fileName);
  }

  private boolean backupFile(UserInfo user, Path filePath) {
    /* permissions already checked in methods that call this one
    if (user == null || !user.canWrite(this, filePath)) {
      return false;
    }
    */

    File file = getFile(filePath);
    String fileName = file.getName();
    int dot = fileName.lastIndexOf('.');
    String fileExt = (dot == -1) ? ".bak" : fileName.substring(dot);

    File bakFile = getRepositoryFile(filePath, BACKUP_PREFIX +
        user.getUsername() + "_" +
        WebUtils.numericDateFormatter.format(new Date()) + fileExt);

    if (Utils.forceRenameTo(file, bakFile, true)) {
      return true;
    }

    sc.log("Can't backup path " + filePath);
    return false;
  }

  private boolean backupDir(UserInfo user, Path dirPath) {
    /* permissions to be checked by the caller */

    File dir = getFile(dirPath);
    File bakFile = getRepositoryFile(dirPath, BACKUP_DIR_PREFIX +
        user.getUsername() + "_" +
        WebUtils.numericDateFormatter.format(new Date()) + ".zip");
    OutputStream os;

    try {
      os = new BufferedOutputStream(new FileOutputStream(bakFile));
    } catch (FileNotFoundException ex) {
      sc.log("can't create stream on " + bakFile, ex);
      return false;
    }

    new ZipArchiver(dir, os).process();

    try {
      os.close();
    } catch (IOException ex) {
      sc.log("Can't close file " + bakFile, ex);
      return false;
    }

    DirectoryRemover dr = new DirectoryRemover(dir);
    dr.process();
    return dr.getResult();
  }

  public File createDir(Path path) {
    File dir = getFile(path);
    dir.mkdirs();
    return dir.isDirectory() ? dir : null;
  }

  /**
   * Returns the file object for a given path in the web application. The file
   * is not checked for existance.
   *
   * @param path the path representation of the file
   *
   * @return the file object for this path, or null if it's not found
   */
  public File getFile(Path path) {
    return (path == null || path.isRelative()) ? null :
        new File(rootFile, path.toString());
  }

  public File getRootFile() {
    return rootFile;
  }

  /**
   * Returns the site root path.
   */
  public Path getRootPath() {
    return rootPath;
  }

  /**
   * Returns the <code>Path</code> of a file in the context.
   *
   * @see #getFile
   */
  public Path getPath(File file) {
    return new Path(Utils.getRelativePath(rootFile, file, "/"));
  }

  public Path getRequestedPath(HttpServletRequest request) {
    return new Path(request.getServletPath());
  }

  public Path getServedPath(HttpServletRequest request) {
    return new Path(request.getServletPath());
  }

  public Path getServedPath(Path requestedPath) {
    return requestedPath;
  }

  /**
   * Checks if the given path is a directory in the file system.
   *
   * @param path the Path to check
   *
   * @return true if the path is a directory.
   */
  public boolean isDirectory(Path path) {
    return getFile(path).isDirectory();
  }

  /**
   * Returns the directory that contains the given path. This is different from
   * {@link org.meshcms.util.Path#getParent}, since if the path is known to
   * be a directory in the web application, the path itself is returned.
   *
   * @param path the Path to check
   *
   * @return a directory(that contains the given path) as a Path object.
   */
  public Path getDirectory(Path path) {
    // PageInfo pageInfo = getPageInfo(path);
    // return pageInfo.isDirectory() ? path : pageInfo.getPath().getParent();
    if (path == null) {
      return null;
    }

    if (getFile(path).isDirectory()) {
      return path;
    }

    path = path.getParent();

    if (!path.isRelative() && getFile(path).isDirectory()) {
      return path;
    }

    return null;
  }

  public String[] getLinkList(PageInfo[] pages, Path pagePath, String target,
      String style) {
    if (pages == null) {
      return null;
    }

    String[] links = new String[pages.length];
    target = Utils.isNullOrEmpty(target) ? "" : " target=\"" + target + "\"";
    style = Utils.isNullOrEmpty(style) ? "" : " class=\"" + style + "\"";

    for (int i = 0; i < pages.length; i++) {
      if (pages[i] == null) {
        links[i] = "...";
      } else {
        links[i] = "<a href=\"" + getLink(pages[i], pagePath) +
          "\"" + target + style + ">" + siteInfo.getPageTitle(pages[i]) + "</a>";
      }
    }

    return links;
  }

  public String getAbsoluteLink(Path path) {
    return getFile(path).isDirectory() ? path.getAsLink() + '/' : path.getAsLink();
  }

  public String getAbsoluteLink(PageInfo pageInfo) {
    return getAbsoluteLink(pageInfo.getPath());
  }

  public Path getLink(Path path, Path pagePath) {
    Path link = path.getRelativeTo(getDirectory(pagePath));

    if (link.getElementCount() == 0 && isDirectory(path)) {
      Path welcome = getSiteMap().getCurrentWelcome(path);

      if (welcome != null) {
        link = welcome.getRelativeTo(getDirectory(pagePath));
      }
    }

    return link;
  }

  public Path getLink(PageInfo pageInfo, Path pagePath) {
    return getLink(pageInfo.getPath(), pagePath);
  }

  /**
   * Returns an array of menu titles for the given pages.
   * {@link SiteInfo} is used to get the titles.
   *
   * @param pages an array of pages to be processed
   *
   * @return the array of titles for the given pages.
   */
  public String[] getTitles(PageInfo[] pages) {
    if (pages == null) {
      return null;
    }

    String[] titles = new String[pages.length];

    for (int i = 0; i < pages.length; i++) {
      titles[i] = siteInfo.getPageTitle(pages[i]);
    }

    return titles;
  }

  /**
   * Determines if the given path is a system directory, or is contained in a
   * system directory. System directories are:
   *
   * <ul>
   *  <li>the WEB-INF directory (/WEB-INF)</li>
   *  <li>the META-INF directory (/META-INF)</li>
   *  <li>the standard CGI-BIN directory (/cgi-bin)</li>
   *  <li>the MeshCMS admin directory (if <code>checkAdmin</code> is true</li>
   * </ul>
   *
   * @param path the given path(directory) to be checked
   *
   * @return true if it is a system directory, false otherwise
   */
  public boolean isSystem(Path path) {
    if (path == null || path.isRoot()) {
      return false;
    }

    if (path.isRelative()) {
      return true;
    }

    if (adminPath != null && (path.isContainedIn(adminPath) ||
        path.isContainedIn(privatePath) || path.equals(cmsPath.add(CMS_ID_FILE)))) {
      return true;
    }

    String level1 = path.getElementAt(0).toLowerCase();

    return level1.equals("web-inf") || level1.equals("meta-inf") ||
        level1.equals("cgi-bin");
  }

  /**
   * Returns true if the extension of the path is known to denote a type of
   * file that can be edited using the wysiwyg editor.
   */
  public boolean isVisuallyEditable(Path path) {
    return Utils.searchString(configuration.getVisualExtensions(),
        Utils.getExtension(path, false), true) != -1;
  }

  /**
   * Returns the path of the module file with the given name.
   */
  public Path getModulePath(String moduleName) {
    if (moduleName.endsWith(".jsp")) {
      // old module names were in the form module_name.jsp
      moduleName = moduleName.substring(0, moduleName.length() - 4);
    }

    return (Path) siteMap.getModulesMap().get(moduleName);
  }

  /**
   * Returns the path of the admin directory.
   */
  public Path getAdminPath() {
    return adminPath;
  }

  public Path getCMSPath() {
    return cmsPath;
  }

  public boolean isVirtual() {
    return false;
  }

  /**
   * Returns the complete tag used by pages in the admin folder. This way those
   * pages can set to be themed according to the site preferences (i.e. using
   * a custom theme or the default admin theme).
   */
  public String getAdminMetaThemeTag() {
    Path themePath = getThemePath(adminPath);
    return "<meta name=\"decorator\" content=\"/" + getServedPath(themePath) +
        "/" + SiteMap.THEME_DECORATOR + "\" />";
  }

  public String getDummyMetaThemeTag() {
    return "<meta name=\"decorator\" content=\"/" + adminPath + "/theme/dummy.jsp\" />";
  }

  /**
   * Returns a string containing a basic HTML page.
   *
   * @param pageTitle the content of the &lt;title&gt; tag (if null, the title
   * will be &quot;New Page&quot;)
   *
   * @return a basic "empty" HTML page
   */
  public String getHTMLTemplate(String pageTitle) throws IOException {
    String text = Utils.readFully(getFile(getAdminPath().add("template.html")));

    if (pageTitle != null) {
      int idx = text.indexOf("New Page");

      if (idx >= 0) {
        text = text.substring(0, idx) + pageTitle + text.substring(idx + 8);
      }
    }

    return text;
  }

  public boolean isInsideModules(Path path) {
    return path.isContainedIn(customModulesPath) ||
        path.isContainedIn(adminModulesPath);
  }

  public boolean isInsideThemes(Path path) {
    return path.isContainedIn(customThemesPath) ||
        path.isContainedIn(adminThemesPath);
  }

  /**
   * Logs a string by calling <code>ServletContext.log(s)</code>
   */
  protected void log(String s) {
    sc.log(s);
  }

  /**
   * Logs an exception by calling
   * <code>ServletContext.log(message, throwable)</code>
   */
  public void log(String message, Throwable throwable) {
    sc.log(message, throwable);
  }

  /**
   * Checks if the file name is one of the welcome files.
   *
   * @param fileName the file name to check
   * @return true if the given file name is known to be a welcome file name.
   */
  public boolean isWelcomeFileName(String fileName) {
    return Utils.searchString(welcomeFiles, fileName, false) != -1;
  }

  /**
   * Returns the array of welcome file names.
   *
   * @return an array of welcome file names for the current web application.
   * Values are fetched from the web.xml file.
   */
  public String[] getWelcomeFileNames() {
    return welcomeFiles;
  }

  /**
   * Returns the current welcome file path for the given folder. If there is no
   * welcome file in that folder, this method returns null.
   *
   * @param dirPath the folder where to search the welcome file
   *
   * @return the welcome file as a Path object of null if not found.
   */
  public Path findCurrentWelcome(Path dirPath) {
    File dirFile = getFile(dirPath);

    if (dirFile.isDirectory()) {
      for (int i = 0; i < welcomeFiles.length; i++) {
        Path wPath = dirPath.add(welcomeFiles[i]);

        if (getFile(wPath).exists()) {
          return wPath;
        }
      }
    }

    return null;
  }

  public WebSite getWebSite(ServletRequest request) {
    return this;
  }

  public HttpServletRequest wrapRequest(ServletRequest request) {
    return (HttpServletRequest) request;
  }

  public String getTypeDescription() {
    return "single web site";
  }

  public String toString() {
    return "Type: " + getTypeDescription() + "; path: /" + rootPath + "; CMS: " +
        (cmsPath == null ? "disabled" : "enabled on path /" + cmsPath);
  }

  public Object loadFromXML(Path path) {
    File file = getFile(path);

    if (file.exists()) {
      InputStream is = null;

      try {
        is = new BufferedInputStream(new FileInputStream(file));
        XStream xStream = new XStream(new DomDriver());
        XStreamPathConverter pConv = new XStreamPathConverter();
        pConv.setPrependSlash(true);
        xStream.registerConverter(pConv);

        try {
          return xStream.fromXML(new InputStreamReader(is, "UTF-8"));
        } catch (StreamException ex) {
          return xStream.fromXML(is);
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      } finally {
        if (is != null) {
          try {
            is.close();
          } catch (IOException ex) {
            sc.log("Can't close file " + file, ex);
          }
        }
      }
    }

    return null;
  }

  public boolean storeToXML(Object o, Path path) {
    File file = getFile(path);
    OutputStreamWriter osw = null;

    try {
      osw = new OutputStreamWriter
          (new BufferedOutputStream(new FileOutputStream(file)), "UTF-8");
      XStream xStream = new XStream(new DomDriver());
      XStreamPathConverter pConv = new XStreamPathConverter();
      pConv.setPrependSlash(true);
      xStream.registerConverter(pConv);
      xStream.toXML(o, osw);
      return true;
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      if (osw != null) {
        try {
          osw.close();
        } catch (IOException ex) {
          sc.log("Can't close file " + file, ex);
        }
      }
    }

    return false;
  }

  /**
   * Returns the path of the theme to be applied to the given path. This depends
   * on the stored values and on the option to use the default theme for the
   * admin pages. This method returns null if no theme is found.
   *
   * @param pagePath the path of the page who's theme is searched.
   *
   * @return the theme of this page as a Path object
   */
  public Path getThemePath(Path pagePath) {
    Path themePath = siteInfo.getThemePath(pagePath);

    if (pagePath.isContainedIn(adminPath)) {
      if (configuration.isUseAdminTheme() || themePath == null ||
          System.currentTimeMillis() - lastAdminThemeBlock < 300000L || // 5 minutes
          !getFile(themePath.add(SiteMap.THEME_DECORATOR)).exists()) {
        themePath = adminThemePath;
      }
    }

    return themePath;
  }

  public Path getAdminThemePath() {
    return adminThemePath;
  }

  public Path getAdminModulesPath() {
    return adminModulesPath;
  }

  public Path getModuleDataPath() {
    return moduleDataPath;
  }

  public Path getAdminThemesPath() {
    return adminThemesPath;
  }

  public Path getAdminScriptsPath() {
    return adminScriptsPath;
  }

  public Path getPrivatePath() {
    return privatePath;
  }

  public Path getUsersPath() {
    return usersPath;
  }

  public Path getVirtualSitesPath() {
    return virtualSitesPath;
  }

  public Path getRepositoryPath() {
    return repositoryPath;
  }

  public Path getCustomModulesPath() {
    return customModulesPath;
  }

  public Path getGeneratedFilesPath() {
    return generatedFilesPath;
  }

  public Path getCustomThemesPath() {
    return customThemesPath;
  }

  public Path getConfigFilePath() {
    return configFilePath;
  }

  public Path getPropertiesFilePath() {
    return propertiesFilePath;
  }

  public Path getSitesFilePath() {
    return sitesFilePath;
  }

  public long getLastAdminThemeBlock() {
    return lastAdminThemeBlock;
  }

  public void setLastAdminThemeBlock(long lastAdminThemeBlock) {
    this.lastAdminThemeBlock = lastAdminThemeBlock;
  }
}
