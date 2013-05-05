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
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.meshcms.util.Path;

public class VirtualWebSite extends WebSite {
  private MainWebSite mainWebSite;

  protected static VirtualWebSite create(MainWebSite mainWebSite, Path rootPath,
      Path cmsPath) {
    VirtualWebSite virtualWebSite = new VirtualWebSite();
    virtualWebSite.init(mainWebSite, rootPath, cmsPath);
    return virtualWebSite;
  }

  protected void init(MainWebSite mainWebSite, Path rootPath, Path cmsPath) {
    this.mainWebSite = mainWebSite;
    init(mainWebSite.getServletContext(), mainWebSite.getWelcomeFileNames(),
        mainWebSite.getFile(rootPath), rootPath, cmsPath);
  }

  public WebSite getWebSite(ServletRequest request) {
    throw new UnsupportedOperationException("This is a virtual website");
  }

  public boolean isVirtual() {
    return true;
  }

  public HttpServletRequest wrapRequest(ServletRequest request) {
    return new MultiSiteRequestWrapper((HttpServletRequest) request, this);
  }

  public String getTypeDescription() {
    return "virtual web site (" + getDirName() + ')';
  }
  
  public String getDirName() {
    return rootPath.getLastElement();
  }

  public Path getRequestedPath(HttpServletRequest request) {
    return ((MultiSiteRequestWrapper) request).getRequestedPath();
  }

  public Path getServedPath(HttpServletRequest request) {
    return ((MultiSiteRequestWrapper) request).getServedPath();
  }

  public Path getServedPath(Path requestedPath) {
    // a null adminPath is handled correctly
    return requestedPath.isContainedIn(adminPath) ?
        mainWebSite.getAdminPath().add(requestedPath.getRelativeTo(adminPath)) :
        rootPath.add(requestedPath);
  }

  public File getFile(Path path) {
    return mainWebSite.getFile(getServedPath(path));
  }

  public MainWebSite getMainWebSite() {
    return mainWebSite;
  }

  /* public String getLink(Path path) {
    return siteMap.getServedPath(path).getAsLink();
  } */

  public void updateSiteMap(boolean force) {
    if (cmsPath != null) {
      super.updateSiteMap(force);
    }
  }
}
