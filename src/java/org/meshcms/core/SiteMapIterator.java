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

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.meshcms.util.Path;

/**
 * Iterator for the site map.
 */
public class SiteMapIterator implements Iterator {
  private boolean skipHiddenSubPages;
  
  private SiteMap siteMap;
  private SiteInfo siteInfo;
  private Iterator iter;
  private PageInfo nextPage;
  private boolean nextPageChecked;
  
  /**
   * Creates an iterator for the full site map of the given website.
   */
  public SiteMapIterator(WebSite webSite) {
    this(webSite, Path.ROOT);
  }
  
  /**
   * Creates an iterator for the full a submap of the given website, starting
   * from the specified root path.
   */
  public SiteMapIterator(WebSite webSite, Path root) {
    siteMap = webSite.getSiteMap();
    siteInfo = webSite.getSiteInfo();
    iter = siteMap.getPagesList(root).iterator();
  }
  
  public boolean hasNext() {
    return findNextPage();
  }
  
  public Object next() {
    if (!findNextPage()) {
      throw new NoSuchElementException();
    }
    
    return getNextPage();
  }
  
  public void remove() {
    throw new UnsupportedOperationException("Site map is readonly");
  }
  
  /**
   * Returns the next page (same as {@link #next}, but returns null when there
   * are no more pages.
   */
  public PageInfo getNextPage() {
    findNextPage();
    nextPageChecked = false;
    return nextPage;
  }
  
  private boolean findNextPage() {
    if (!nextPageChecked) {
      if (skipHiddenSubPages && nextPage != null &&
          siteInfo.getHideSubmenu(nextPage.getPath())) {
        int level = nextPage.getLevel();
        nextPage = null;
        
        while (nextPage == null && iter.hasNext()) {
          nextPage = (PageInfo) iter.next();
          
          if (nextPage.getLevel() > level) {
            nextPage = null;
          }
        }
      } else {
        if (iter.hasNext()) {
          nextPage = (PageInfo) iter.next();
        } else {
          nextPage = null;
        }
      }
      
      nextPageChecked = true;
    }
    
    return nextPage != null;
  }
  
  public boolean isSkipHiddenSubPages() {
    return skipHiddenSubPages;
  }
  
  public void setSkipHiddenSubPages(boolean skipHiddenSubPages) {
    this.skipHiddenSubPages = skipHiddenSubPages;
  }
}
