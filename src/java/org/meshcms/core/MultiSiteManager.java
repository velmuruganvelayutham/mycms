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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class MultiSiteManager implements Serializable {
  private transient MainWebSite mainWebSite;
  private transient SortedMap domainsMap;
  private transient int siteCount;

  private boolean manageTripleWs;
  private boolean useDirsAsDomains;
  private String mainWebSiteDomains;
  private Properties domains;
  private List jspBlocks;

  private MultiSiteManager() {
    domains = new Properties();
    jspBlocks = new ArrayList();
    manageTripleWs = true;
    useDirsAsDomains = true;
  }
  
  public List getJSPBlocks() {
    if (jspBlocks == null) {
      jspBlocks = new ArrayList();
    }
    
    return jspBlocks;
  }
  
  public boolean isJSPBlocked(VirtualWebSite webSite) {
    return jspBlocks != null && jspBlocks.contains(webSite.getDirName());
  }

  public boolean isManageTripleWs() {
    return manageTripleWs;
  }

  public void setManageTripleWs(boolean manageTripleWs) {
    this.manageTripleWs = manageTripleWs;
  }

  public boolean isUseDirsAsDomains() {
    return useDirsAsDomains;
  }

  public void setUseDirsAsDomains(boolean useDirsAsDomains) {
    this.useDirsAsDomains = useDirsAsDomains;
  }

  public void setDomains(String dir, String domainNames) {
    if (domainNames != null) {
      domains.setProperty(dir, domainNames);
    }
  }

  public String getDomains(String dir) {
    return domains.getProperty(dir.toLowerCase());
  }

  public int initDomainsMap() {
    domainsMap = new TreeMap();
    File[] dirs = mainWebSite.getFile(mainWebSite.getVirtualSitesPath()).listFiles();
    siteCount = 0;

    for (int i = 0; i < dirs.length; i++) {
      if (dirs[i].isDirectory()) {
        siteCount++;
        String dirName = dirs[i].getName().toLowerCase();
        VirtualWebSite webSite = mainWebSite.getVirtualSite(dirName);
        parseDomains(domainsMap, webSite, getDomains(dirName));

        if (useDirsAsDomains) {
          parseDomains(domainsMap, webSite, dirName);
        }
      }
    }

    if (!parseDomains(domainsMap, mainWebSite, mainWebSiteDomains)) {
      /* no valid domains in mainWebSiteDomains, so set it to null for
         getWebSite() to work correctly */
      mainWebSiteDomains = null;
    }

    return domainsMap.size();
  }

  private boolean parseDomains(SortedMap map, WebSite webSite, String domainNames) {
    boolean result = false;

    if (domainNames != null) {
      StringTokenizer st = new StringTokenizer(domainNames, ";:, \t");

      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        map.put(token, webSite);
        result = true;

        if (manageTripleWs) {
          map.put("www." + token, webSite);
        }
      }
    }

    return result;
  }

  public WebSite getWebSite(String domain) {
    WebSite webSite = (WebSite) domainsMap.get(domain.toLowerCase());

    if (webSite == null && mainWebSiteDomains == null) {
      webSite = mainWebSite;
    }

    return webSite;
  }
  
  public int getSiteCount() {
    return siteCount;
  }

  public String getMainWebSiteDomains() {
    return mainWebSiteDomains;
  }

  public void setMainWebSiteDomains(String mainWebSiteDomains) {
    this.mainWebSiteDomains = mainWebSiteDomains;
  }

  public static MultiSiteManager load(MainWebSite mainWebSite) {
    MultiSiteManager m = null;

    try {
      m = (MultiSiteManager) mainWebSite.loadFromXML
          (mainWebSite.getSitesFilePath());
      m.setMainWebSite(mainWebSite);
    } catch (Exception ex) {}

    if (m == null) {
      m = new MultiSiteManager();
      m.setMainWebSite(mainWebSite);
    }

    return m;
  }

  public boolean store(MainWebSite mainWebSite) {
    return mainWebSite.storeToXML(this, mainWebSite.getSitesFilePath());
  }

  public MainWebSite getMainWebSite() {
    return mainWebSite;
  }

  public void setMainWebSite(MainWebSite mainWebSite) {
    this.mainWebSite = mainWebSite;
  }
}
