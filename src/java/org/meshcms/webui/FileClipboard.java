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

package org.meshcms.webui;

import org.meshcms.util.Path;
import org.meshcms.util.Utils;

/**
 * A simple class to store path and names of files selected with the cut and
 * copy features of the file manager.
 */
public class FileClipboard {
  private Path dirPath;
  private String[] fileNames;
  private boolean cut;

  /**
   * Creates a new clipboard (usually one clipboard per session should be used).
   */
  public FileClipboard() {
  }

  /**
   * Sets the content of the clipboard.
   *
   * @param dirPath the path of the directory where the files are located
   * @param names a comma-separated list of the file names
   * @param cut true for the "cut" operation, false for the "copy"
   */
  public void setContent(Path dirPath, String names, boolean cut) {
    this.dirPath = dirPath;
    fileNames = Utils.tokenize(names, ",");
    this.cut = cut;
  }

  /**
   * Retrieves the content of the clipboard.
   *
   * @return an array of file paths
   */
  public Path[] getContent() {
    if (dirPath == null || fileNames == null || fileNames.length == 0) {
      return null;
    }

    Path[] paths = new Path[fileNames.length];

    for (int i = 0; i < paths.length; i++) {
      paths[i] = dirPath.add(fileNames[i]);
    }

    return paths;
  }

  /**
   * Marks the clipboard as empty.
   */
  public void clear() {
    dirPath = null;
    fileNames = null;
    cut = false;
  }

  /**
   * Returns the number of files in the clipboard.
   */
  public int countFiles() {
    return fileNames == null ? 0 : fileNames.length;
  }

  /**
   * Returns the path of the directory where the files are located
   */
  public Path getDirPath() {
    return dirPath;
  }

  /**
   * Returns the file names.
   */
  public String[] getFileNames() {
    return fileNames;
  }

  /**
   * Returns true for the "cut" operation, false for the "copy"
   */
  public boolean isCut() {
    return cut;
  }
}
