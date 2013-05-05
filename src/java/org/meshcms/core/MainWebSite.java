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
import java.util.SortedMap;
import java.util.TreeMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import org.meshcms.util.Path;

public class MainWebSite extends WebSite {
  private SortedMap virtualSitesMap;
  private MultiSiteManager multiSiteManager;

  /**
   * Creates a new main website.
   */
  protected static WebSite create(ServletContext sc,
      String[] welcomeFiles, File rootFile, Path rootPath, Path cmsPath) {
    MainWebSite mainWebSite = new MainWebSite();
    mainWebSite.init(sc, welcomeFiles, rootFile, rootPath, cmsPath);
    return mainWebSite;
  }

  /**
   * Initializes the website. After calling the method of the superclass,
   * initializes the virtual websites.
   */
  protected void init(ServletContext sc, String[] welcomeFiles, File rootFile,
      Path rootPath, Path cmsPath) {
    super.init(sc, welcomeFiles, rootFile, rootPath, cmsPath);

    if (virtualSitesMap == null) {
      virtualSitesMap = new TreeMap();
    }

    if (multiSiteManager == null) {
      multiSiteManager = MultiSiteManager.load(this);
    }

    multiSiteManager.initDomainsMap();
  }

  /**
   * Returns the right website for the given request. Since this is a main
   * website, it will return the website itself or a virtual website, according
   * to the requested host name.
   */
  public WebSite getWebSite(ServletRequest request) {
    return multiSiteManager.getWebSite(request.getServerName());
  }

  public String getTypeDescription() {
    return "main web site";
  }

  /**
   * Returns the virtual website instance related to the given directory name.
   * That instance will be created if not found, and will not fail if the
   * directory does not exist (this is subject to change).
   */
  public VirtualWebSite getVirtualSite(String dirName) {
    VirtualWebSite vws = (VirtualWebSite) virtualSitesMap.get(dirName);

    if (vws == null) {
      Path sitePath = virtualSitesPath.add(dirName);
      File root = getFile(sitePath);
      Path cms = new CMSDirectoryFinder(root, true).getCMSPath();
      vws = VirtualWebSite.create(this, sitePath, cms);
      virtualSitesMap.put(dirName, vws);
    }

    return vws;
  }

  /**
   * Returns the MultiSiteManager instance.
   */
  public MultiSiteManager getMultiSiteManager() {
    return multiSiteManager;
  }

  public void updateSiteMap(boolean force) {
    super.updateSiteMap(force);

    if (multiSiteManager != null) {
      multiSiteManager.initDomainsMap();
    }
  }

  /* public String getHost(String dirName) {
    WebSite site = multiSiteManager.getWebSite(dirName);

    if (site != null) {
      String host = site.getConfiguration().getSiteHost();

      if (!Utils.isNullOrEmpty(host) &&
          multiSiteManager.getWebSite(host).equals(site)) {
        return host;
      }

      if (multiSiteManager.isUseDirsAsDomains()) {
        return dirName;
      }

      StringTokenizer st =
          new StringTokenizer(multiSiteManager.getDomains(dirName), ";:, \t");

      if (st.hasMoreTokens()) {
        return st.nextToken();
      }
    }

    return null;
  } */
}
