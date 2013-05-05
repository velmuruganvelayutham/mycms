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

package org.meshcms.util;

import java.io.File;

/**
 * Deletes a directory even if not empty.
 */
public class DirectoryRemover extends DirectoryParser {
  private boolean result;

  public DirectoryRemover(File dir) {
    setInitialDir(dir);
    setRecursive(true);
    setProcessStartDir(true);
    result = true;
  }

  protected void postProcessDirectory(File file, Path path) {
    if (!Utils.forceDelete(file)) {
      result = false;
    }
  }

  protected void processFile(File file, Path path) {
    if (!Utils.forceDelete(file)) {
      result = false;
    }
  }

  /**
   * This method can be called after processing to know whether the directory
   * has been fully deleted or not.
   *
   * @return <code>true</code> if the directory has been fully deleted, <code>false</code> otherwise.
   */
  public boolean getResult() {
    return result;
  }
}
