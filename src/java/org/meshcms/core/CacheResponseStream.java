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

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Writes the page to both the browser and the cache.
 *
 * @see CacheResponseWrapper
 */
public class CacheResponseStream extends ServletOutputStream {
  ServletOutputStream output;
  OutputStream cacheOutput;

  /**
   * Creates a new Stream to write to the original output stream of the response
   * and to the passed output stream.
   *
   * @param response the original response
   * @param cacheOutput the output for the cache
   *
   * @throws IOException if an input or output exception occurred
   */
  public CacheResponseStream(HttpServletResponse response,
      OutputStream cacheOutput) throws IOException {
    super();
    this.cacheOutput = cacheOutput;
    output = response.getOutputStream();
  }

  /**
   * Writes to both streams.
   *
   * @param b the byte to write
   * @throws IOException if an I/O error occurs
   */
  public void write(int b) throws IOException {
    output.write(b);

    try {
      cacheOutput.write(b);
    } catch (IOException ex) {}
  }

  /**
   * Flushes both streams.
   *
   * @throws IOException if an I/O error occurs
   */
  public void flush() throws IOException {
    output.flush();

    try {
      cacheOutput.flush();
    } catch (IOException ex) {}
  }

  /**
   * Closes both streams.
   *
   * @throws IOException if an I/O error occurs
   */
  public void close() throws IOException {
    output.close();

    try {
      cacheOutput.close();
    } catch (IOException ex) {}
  }
}
