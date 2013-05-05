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

import java.io.File;
import org.meshcms.util.DirectoryParser;
import org.meshcms.util.Path;

/**
 * Finds the CMS directory by searching for a sentinel file.
 */
public class CMSDirectoryFinder extends DirectoryParser {
  private Path cmsPath;
  private boolean virtualSite;
  private String idFileName;

  /**
   * Creates a new instance to search for the CMS directory using the
   * given root.
   *
   * @param siteRoot initial directory to start the processing.
   * @param virtualSite used to determine what file to search (virtual sites
   * do not contain an admin directory, so a different file is searched).
   */
  public CMSDirectoryFinder(File siteRoot, boolean virtualSite) {
    setRecursive(true);
    setInitialDir(siteRoot);
    this.virtualSite = virtualSite;
    idFileName = virtualSite ? WebSite.CMS_ID_FILE : WebSite.ADMIN_ID_FILE;
  }

  protected void processFile(File file, Path path) {
    //
  }

  protected boolean preProcessDirectory(File file, Path path) {
    if (cmsPath != null) {
      return false;
    }

    File vFile = new File(file, idFileName);

    if (vFile.exists()) {
      cmsPath = virtualSite ? path : path.getParent();
      return false;
    }

    return true;
  }

  /**
   * Performs the search and returns the result.
   *
   * @return the CMS Path
   */
  public Path getCMSPath() {
    if (cmsPath == null) {
      process();
    }

    return cmsPath;
  }
}
