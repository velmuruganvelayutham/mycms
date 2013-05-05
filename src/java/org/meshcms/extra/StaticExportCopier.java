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
import java.io.IOException;
import java.io.Writer;
import org.meshcms.core.FileTypes;
import org.meshcms.core.WebSite;
import org.meshcms.util.DirectoryParser;
import org.meshcms.util.Path;
import org.meshcms.util.Utils;

/**
 * Cleans the directory used to export static files. This is done by removing
 * all files that are not available in the dynamic version of the site. Empty
 * directories are also removed. This class is used by {@link StaticExporter}.
 */
public class StaticExportCopier extends DirectoryParser {
  File destinationRoot;
  private boolean checkDates;
  private boolean mkDirs;
  Writer writer;

  /**
   * Creates an instance for the given context root
   */
  public StaticExportCopier(File destinationRoot) {
    super();
    this.destinationRoot = destinationRoot;
    setRecursive(true);
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

  protected boolean preProcessDirectory(File file, Path path) {
    File destDir = path.getFile(destinationRoot);
    
    if (mkDirs) {
      destDir.mkdirs();
    }
    
    return destDir.isDirectory();
  }

  protected void processFile(File file, Path path) {
    if (!(FileTypes.isPage(file.getName()) || 
        file.getName().equals(WebSite.CMS_ID_FILE) ||
        file.getName().equals(WebSite.ADMIN_ID_FILE))) {
      File copy = path.getFile(destinationRoot);
      
      if (!(checkDates && copy.exists() &&
          file.lastModified() <= copy.lastModified())) {
        try {
          Utils.copyFile(file, copy, true, false);
          write(path + " file copied");
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
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

  public boolean isCheckDates() {
    return checkDates;
  }

  public void setCheckDates(boolean checkDates) {
    this.checkDates = checkDates;
  }

  public boolean isMkDirs() {
    return mkDirs;
  }

  public void setMkDirs(boolean mkDirs) {
    this.mkDirs = mkDirs;
  }
}
