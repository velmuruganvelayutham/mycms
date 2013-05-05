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
import org.meshcms.util.Utils;

/**
 * Inserts breadcrumbs for the current page.
 */
public final class Breadcrumbs extends AbstractTag {
  public static final String MODE_TITLES = "titles";
  public static final String MODE_LINKS = "links";
  public static final String DEFAULT_SEPARATOR = " ";

  private String separator = DEFAULT_SEPARATOR;
  private String mode;
  private String style;
  private String target;
  private String current = "true";
  private String pre;
  private String post;

  public void setSeparator(String separator) {
    this.separator = Utils.noNull(separator, DEFAULT_SEPARATOR);
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public void setStyle(String style) {
    this.style = style;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public void setPre(String pre) {
    this.pre = pre;
  }

  public void setPost(String post) {
    this.post = post;
  }

  public void writeTag() throws IOException {
    PageInfo[] breadcrumbs = webSite.getSiteMap().getBreadcrumbs(pagePath);
    String[] outs;

    if (mode != null && mode.equals(MODE_LINKS)) {
      outs = webSite.getLinkList(breadcrumbs, pageDirPath, target, style);
    } else {
      outs = webSite.getTitles(breadcrumbs);
    }

    if (Utils.isTrue(current)) {
      int last = 0;

      if (outs == null) {
        outs = new String[1];
      } else {
        last = outs.length;
        String[] temp = new String[last + 1];
        System.arraycopy(outs, 0, temp, 0, last);
        outs = temp;
      }

      PageInfo pageInfo = webSite.getSiteMap().getPageInfo(pagePath);
      outs[last] = (pageInfo == null) ? getPage().getTitle() :
          webSite.getSiteInfo().getPageTitle(pageInfo);
    }

    Writer w = getOut();

    if (outs != null && outs.length > 0) {
      if (pre != null) {
        w.write(pre);
      }

      w.write(Utils.generateList(outs, separator));

      if (post != null) {
        w.write(post);
      }
    } else {
      w.write("&nbsp;");
    }
  }

  public String getSeparator() {
    return separator;
  }

  public String getMode() {
    return mode;
  }

  public String getStyle() {
    return style;
  }

  public String getTarget() {
    return target;
  }

  public String getCurrent() {
    return current;
  }

  public void setCurrent(String current) {
    this.current = current;
  }

  public String getPre() {
    return pre;
  }

  public String getPost() {
    return post;
  }
}
