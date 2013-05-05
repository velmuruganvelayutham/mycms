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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.meshcms.util.Path;

public class MultiSiteRequestWrapper extends HttpServletRequestWrapper {
  Path requestedPath;
  Path servedPath;
  
  public MultiSiteRequestWrapper(HttpServletRequest request,
      VirtualWebSite webSite) {
    super(request);
    requestedPath = new Path(request.getServletPath());
    servedPath = webSite.getServedPath(requestedPath);
  }
  
  public String getServletPath() {
    return servedPath.getAsLink();
  }
  
  public Path getRequestedPath() {
    return requestedPath;
  }

  public Path getServedPath() {
    return servedPath;
  }
}
