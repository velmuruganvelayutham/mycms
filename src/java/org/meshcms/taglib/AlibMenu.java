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
import org.meshcms.core.SiteMapIterator;
import org.meshcms.core.WebUtils;
import org.meshcms.util.Path;
import org.meshcms.util.Utils;

/**
 * Creates a navigation menu, using a unnumbered list.
 */
public final class AlibMenu extends AbstractTag {
  public static final String PART_HEAD = "head";
  public static final String PART_BODY = "body";
  public static final String HORIZONTAL = "horizontal";
  public static final String VERTICAL = "vertical";

  private String indentation = "  ";
  private String indentBuffer = "";

  private String orientation;
  private String part;
  private String path;
  private String current;
  private String currentPathStyle = "selected";
  private String allowHiding = "true";

  public void writeTag() throws IOException {
    Writer outWriter = getOut();
    boolean horizontal = orientation != null && orientation.equals(HORIZONTAL);

    if (part != null && part.equals(PART_HEAD)) {
      Path sp = webSite.getAdminScriptsPath().add("alib");
      String menuType = horizontal ? HORIZONTAL : VERTICAL;
      outWriter.write("<script src='" +
          webSite.getLink(sp.add("alib.common/script.js"), pageDirPath) +
          "' type='text/javascript'></script>\n");
      outWriter.write("<script src='" +
          webSite.getLink(sp.add("menu." + menuType + "/script.js"), pageDirPath) +
          "' type='text/javascript'></script>\n");
      outWriter.write("<link type='text/css' href='" +
          WebUtils.getThemeFolderPath(request, pageDirPath) + "/alib.css' rel='stylesheet' />\n");
    } else {
      SiteInfo siteInfo = webSite.getSiteInfo();
      Path rootPath = (path == null) ? siteInfo.getThemeRoot(pagePath) : new Path(path);
      Path pathInMenu = webSite.getSiteMap().getPathInMenu(pagePath);
      int baseLevel = rootPath.getElementCount() + 1;
      int lastLevel = rootPath.getElementCount();
      SiteMapIterator iter = new SiteMapIterator(webSite, rootPath);
      iter.setSkipHiddenSubPages(Utils.isTrue(allowHiding));
      boolean liUsed = false;
      boolean firstUl = true;

      while (iter.hasNext()) {
        PageInfo currentPageInfo = (PageInfo) iter.next();
        Path currentPath = currentPageInfo.getPath();
        int level = Math.max(baseLevel, currentPageInfo.getLevel());

        for (int i = lastLevel; i < level; i++) {
          if (firstUl) {
            writeIndented(outWriter, "<ul class=\"" +
                (horizontal ? "hmenu" : "vmenu") + "\">", i);
            firstUl = false;
          } else {
            writeIndented(outWriter, "<ul>", i);
          }

          writeIndented(outWriter, "<li>", i + 1);
          liUsed = false;
        }

        for (int i = lastLevel - 1; i >= level; i--) {
          if (liUsed) {
            outWriter.write("</li>");
            liUsed = false;
          } else {
            writeIndented(outWriter, "</li>", i + 1);
          }

          writeIndented(outWriter, "</ul>", i);
        }

        if (liUsed) {
          outWriter.write("</li>");
          writeIndented(outWriter, "<li>", level);
        }

        for ( int i = lastLevel - 1; i >= level; i--) {
            writeIndented(outWriter, "</li>", i);
            writeIndented(outWriter, "<li>", i);
        }

        if ( ! Utils.isNullOrEmpty(currentPathStyle)
                        && ( currentPageInfo.getLevel() >= baseLevel
                               && pathInMenu.isContainedIn(currentPath)
                     || currentPageInfo.getPath().equals(pathInMenu)
                   ) ) {
          outWriter.write("<a href=\"" + webSite.getLink(currentPageInfo, pageDirPath) +
            "\" class='" + currentPathStyle + "'>" +
            siteInfo.getPageTitle(currentPageInfo) + "</a>");
        } else {
          outWriter.write("<a href=\"" + webSite.getLink(currentPageInfo, pageDirPath) +"\">" +
            siteInfo.getPageTitle(currentPageInfo) + "</a>");
        }

        liUsed = true;
        lastLevel = level;
      }

      for (int i = lastLevel - 1; i >= rootPath.getElementCount(); i--) {
        writeIndented(outWriter, "</li>", i + 1);
        writeIndented(outWriter, "</ul>", i);
      }
    }
  }

  private void writeIndented(Writer w, String s, int level) throws IOException {
    while (indentBuffer.length() < indentation.length() * level) {
      indentBuffer += indentation;
    }

    w.write('\n');
    w.write(indentBuffer, 0, indentation.length() * level);
    w.write(s);
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getCurrent() {
    return current;
  }

  public void setCurrent(String current) {
    this.current = current;
  }

  public String getOrientation() {
    return orientation;
  }

  public void setOrientation(String orientation) {
    this.orientation = orientation;
  }

  public String getPart() {
    return part;
  }

  public void setPart(String part) {
    this.part = part;
  }

  public String getCurrentPathStyle() {
    return currentPathStyle;
  }

  public void setCurrentPathStyle(String currentPathStyle) {
    this.currentPathStyle = currentPathStyle;
  }

  public String getAllowHiding() {
    return allowHiding;
  }

  public void setAllowHiding(String allowHiding) {
    this.allowHiding = allowHiding;
  }
}
