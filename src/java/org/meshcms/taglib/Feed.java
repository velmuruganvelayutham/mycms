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
import java.util.regex.Pattern;
import org.meshcms.util.Utils;

public class Feed extends AbstractTag {
  private String root;
  private String pathRegex;
  private int maxItems = 10;

  public void writeTag() throws IOException {
    String rootPath = Utils.noNull(root,
        webSite.getSiteMap().getPathInMenu(pagePath).toString());

    if (Utils.isNullOrEmpty(pathRegex) ||
        Pattern.matches(pathRegex, pagePath.toString())) {
      Writer w = getOut();
      w.write("<link rel=\"alternate\" type=\"application/rss+xml\" title=\"" +
          webSite.getConfiguration().getSiteName().replace('"', '\'') +
          "\" href=\"" + adminRelPath.add("rss.jsp") + "?root=" + rootPath +
          "&max=" + maxItems + "\"/>");
    }
  }

  public String getPathRegex() {
    return pathRegex;
  }

  public void setPathRegex(String pathRegex) {
    this.pathRegex = pathRegex;
  }

  public String getRoot() {
    return root;
  }

  public void setRoot(String root) {
    this.root = root;
  }

  public int getMaxItems() {
    return maxItems;
  }

  public void setMaxItems(int maxItems) {
    this.maxItems = maxItems;
  }
}
