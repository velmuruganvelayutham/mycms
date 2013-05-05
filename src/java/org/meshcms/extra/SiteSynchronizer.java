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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import org.meshcms.core.UserInfo;
import org.meshcms.core.WebSite;
import org.meshcms.util.DirectoryParser;
import org.meshcms.util.Path;
import org.meshcms.util.Utils;

public class SiteSynchronizer extends DirectoryParser {
  private WebSite sourceSite;
  private WebSite targetSite;
  private UserInfo targetUser;
  private Writer writer;
  private boolean copySiteInfo;
  private boolean copyConfig;

  public SiteSynchronizer(WebSite sourceSite, WebSite targetSite,
      UserInfo targetUser) {
    this.sourceSite = sourceSite;
    this.targetSite = targetSite;
    this.targetUser = targetUser;
    setInitialDir(sourceSite.getRootFile());
    setRecursive(true);
    setProcessStartDir(true);
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

  private void write(String message) {
    if (writer != null) {
      try {
        writer.write(message);
        writer.write('\n');
        writer.flush();
      } catch (IOException ex) {
      }
    }
  }

  protected boolean preProcessDirectory(File file, Path path) {
    Path targetPath = getTargetPath(path);

    if (targetPath == null) {
      return false;
    }

    File targetDir = targetSite.getFile(targetPath);

    if (targetDir.isFile()) {
      if (targetSite.delete(targetUser, targetPath, false)) {
        write(targetPath + " file deleted");
      } else {
        write(targetPath + " file NOT deleted");
      }
    }

    targetSite.createDir(targetPath);
    File[] targetFiles = targetDir.listFiles();

    if (!path.equals(targetSite.getCMSPath())) {
      for (int i = 0; i < targetFiles.length; i++) {
        File srcFile = new File(file, targetFiles[i].getName());

        if (!((srcFile.isFile() && targetFiles[i].isFile()) ||
            (srcFile.isDirectory() && targetFiles[i].isDirectory()))) {
          Path filePath = targetPath.add(targetFiles[i].getName());

          if (targetSite.delete(targetUser, filePath, true)) {
            write(filePath + " file/folder deleted");
          } else {
            write(filePath + " file/folder NOT deleted");
          }
        }
      }
    }

    return true;
  }

  protected void processFile(File file, Path path) {
    Path targetPath = getTargetPath(path);

    if (targetPath == null) {
      return;
    }

    File targetFile = targetSite.getFile(targetPath);

    if (targetFile.isDirectory()) {
      if (targetSite.delete(targetUser, targetPath, true)) {
        write(targetPath + " folder deleted");
      } else {
        write(targetPath + " folder NOT deleted");
      }
    }

    if (!(targetFile.exists() &&
        file.lastModified() == targetFile.lastModified() &&
        file.length() == targetFile.length())) {
      try {
        FileInputStream fis = new FileInputStream(file);
        targetSite.saveToFile(targetUser, fis, targetPath);
        targetSite.getFile(targetPath).setLastModified(file.lastModified());
        
        write(targetPath + " file copied");
        fis.close();
      } catch (IOException ex) {
        write(targetPath + " file NOT copied");
        targetSite.log(ex.getMessage(), ex);
      }
    }
  }

  private Path getTargetPath(Path sourcePath) {
    if (sourceSite.isSystem(sourcePath)) {
      return null;
    }

    Path targetPath = sourcePath;
    Path sourceCMSPath = sourceSite.getCMSPath();

    if (sourcePath.isContainedIn(sourceCMSPath)) {
      if (!sourcePath.equals(sourceCMSPath)) {
        String elm1 = sourcePath.getElementAt(1);

        if (!(elm1.equals("modules") || elm1.equals("themes"))) {
          targetPath = null;
        }
      }
    }

    return targetPath;
  }

  protected void postProcess() {
    if (isCopySiteInfo()) {
      try {
        Utils.copyFile(sourceSite.getFile(sourceSite.getPropertiesFilePath()),
            targetSite.getFile(targetSite.getPropertiesFilePath()), true, false);
        write("Site map information copied");
      } catch (IOException ex) {
        targetSite.log(ex.getMessage(), ex);
        write("Site map information NOT copied");
      }
    }

    if (isCopyConfig()) {
      try {
        Utils.copyFile(sourceSite.getFile(sourceSite.getConfigFilePath()),
            targetSite.getFile(targetSite.getConfigFilePath()), true, false);
        write("Site configuration copied");
      } catch (IOException ex) {
        targetSite.log(ex.getMessage(), ex);
        write("Site configuration NOT copied");
      }
    }

    targetSite.readConfig();
    targetSite.updateSiteMap(true);
  }

  public boolean isCopySiteInfo() {
    return copySiteInfo;
  }

  public void setCopySiteInfo(boolean copySiteInfo) {
    this.copySiteInfo = copySiteInfo;
  }

  public boolean isCopyConfig() {
    return copyConfig;
  }

  public void setCopyConfig(boolean copyConfig) {
    this.copyConfig = copyConfig;
  }
}
