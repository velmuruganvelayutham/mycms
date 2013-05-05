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

package org.meshcms.taglib;

import java.io.IOException;
import java.io.Writer;
import org.meshcms.core.PageInfo;
import org.meshcms.core.SiteInfo;
import org.meshcms.core.SiteMap;
import org.meshcms.core.SiteMapIterator;
import org.meshcms.util.Path;
import org.meshcms.util.Utils;

/**
 * Creates a simple navigation menu.
 */
public final class SimpleMenu extends AbstractTag {
  private String path;
  private String space = "8";
  private String bullet = "&bull;";
  private String style;
  private String expand;
  private boolean allowHiding = false;

  public void setPath(String path) {
    this.path = path;
  }

  public void setBullet(String bullet) {
    if (bullet != null) {
      this.bullet = bullet;
    }
  }

  public void setStyle(String style) {
    this.style = style;
  }

  public void writeTag() throws IOException {
    SiteMap siteMap = webSite.getSiteMap();
    SiteInfo siteInfo = webSite.getSiteInfo();
    Path rootPath = (path == null) ? siteInfo.getThemeRoot(pagePath) : new Path(path);
    Path pathInMenu = webSite.getSiteMap().getPathInMenu(pagePath);
    int baseLevel = rootPath.getElementCount() + 1;
    int spc = Utils.parseInt(space, 8);
    SiteMapIterator iter = new SiteMapIterator(webSite, rootPath);
    iter.setSkipHiddenSubPages(allowHiding);
    Writer outWriter = getOut();

    while (iter.hasNext()) {
      PageInfo current = (PageInfo) iter.next();
      Path currentPath = current.getPath();
      Path parentPath = currentPath.getParent();

      if (parentPath.isRelative() || pathInMenu.isContainedIn(parentPath)) {
        if (Utils.isTrue(expand) ||
            pathInMenu.isContainedIn(currentPath) ||
            currentPath.getElementCount() == baseLevel ||
            currentPath.getElementCount() >= pathInMenu.getElementCount()) {
          outWriter.write("<div style=\"padding-left: " +
            (spc * Math.max(current.getLevel() - baseLevel, 0)) + "px;\">");

          if (style != null) {
            outWriter.write("<div class=\"" + style + "\">");
          }

          outWriter.write(bullet + "&nbsp;");

          if (!isEdit && current.getPath().equals(pathInMenu)) {
            outWriter.write(siteInfo.getPageTitle(current));
          } else {
            outWriter.write("<a href=\"" + webSite.getLink(current, pageDirPath) +
              "\">" + siteInfo.getPageTitle(current) + "</a>");
          }

          if (style != null) {
            outWriter.write("</div>");
          }

          outWriter.write("</div>\n");
        }
      }
    }
  }

  public String getPath() {
    return path;
  }

  public String getSpace() {
    return space;
  }

  public void setSpace(String space) {
    this.space = space;
  }

  public String getBullet() {
    return bullet;
  }

  public String getStyle() {
    return style;
  }

  public String getExpand() {
    return expand;
  }

  public void setExpand(String expand) {
    this.expand = expand;
  }

  public boolean getAllowHiding() {
    return allowHiding;
  }

  public void setAllowHiding(boolean allowHiding) {
    this.allowHiding = allowHiding;
  }
}
