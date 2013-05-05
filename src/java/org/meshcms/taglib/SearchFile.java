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

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import org.meshcms.core.HitFilter;
import org.meshcms.core.WebUtils;
import org.meshcms.util.Path;
import org.meshcms.util.Utils;

/**
 * Writes the path of the theme folder. Often used to access files included in
 * that folder.
 */
public final class SearchFile extends AbstractTag {
  private String name;
  private String defaultName;
  private String include;

  public void writeTag() throws IOException, JspException {
    if (!Utils.isNullOrEmpty(name)) {
      Path found = null;
      Path currentPath = webSite.getDirectory(pagePath);

      while (found == null && !currentPath.isRelative()) {
        Path p = currentPath.add(name);
        currentPath = currentPath.getParent();

        if (webSite.getFile(p).exists()) {
          found = p;
        }
      }

      if (found == null && defaultName != null) {
        Path themePath = (Path) request.getAttribute(HitFilter.THEME_PATH_ATTRIBUTE);

        if (themePath != null) {
          themePath = themePath.add(defaultName);

          if (webSite.getFile(themePath).exists()) {
            found = themePath;
          }
        }
      }

      if (found != null) {
        if (Utils.isTrue(include)) {
          File f = webSite.getFile(found);
          
          try {
            pageContext.include("/" + webSite.getServedPath(webSite.getPath(f)));
            WebUtils.updateLastModifiedTime(request, f);
          } catch (ServletException ex) {
            throw new JspException(ex);
          }
        } else {
          getOut().write(webSite.getLink(found, pageDirPath).toString());
        }
      }
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDefaultName() {
    return defaultName;
  }

  public void setDefaultName(String defaultName) {
    this.defaultName = defaultName;
  }

  public String getInclude() {
    return include;
  }

  public void setInclude(String include) {
    this.include = include;
  }
}
