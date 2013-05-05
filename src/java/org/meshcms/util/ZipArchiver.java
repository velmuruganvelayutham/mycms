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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Creates a ZIP file from a directory or file.
 */
public class ZipArchiver extends DirectoryParser {
  private ZipOutputStream zout;
  private byte[] buf;

  /**
   * Instantiates the archiver for the given file and output stream.
   *
   * @param contents the file to be archieved
   * @param out the OutputStream to write the archive to.
   */
  public ZipArchiver(File contents, OutputStream out) {
    zout = new ZipOutputStream(out);
    setInitialDir(contents);
    setRecursive(true);
    buf = new byte[Utils.BUFFER_SIZE];
  }

  protected boolean preProcessDirectory(File file, Path path) {
    try {
      ZipEntry ze = new ZipEntry(path + "/");
      ze.setTime(file.lastModified());
      zout.putNextEntry(ze);
      zout.closeEntry();
    } catch (IOException ex) {
      ex.printStackTrace();
      return false;
    }

    return true;
  }

  protected void processFile(File file, Path path) {
    try {
      ZipEntry ze = new ZipEntry(path.isRoot() ? file.getName() : path.toString());
      ze.setTime(file.lastModified());
      ze.setSize(file.length());
      zout.putNextEntry(ze);
      FileInputStream fis = new FileInputStream(file);
      int len;

      while((len = fis.read(buf)) != -1) {
        zout.write(buf, 0, len);
      }

      fis.close();
      zout.closeEntry();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  protected void postProcess() {
    try {
      zout.finish();
      zout.flush();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
