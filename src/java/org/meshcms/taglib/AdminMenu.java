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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.servlet.jsp.PageContext;
import org.meshcms.core.FileTypes;
import org.meshcms.core.HitFilter;
import org.meshcms.core.UserInfo;
import org.meshcms.core.WebUtils;
import org.meshcms.util.Path;
import org.meshcms.util.Utils;

/**
 * Creates a menu with the following items:
 *
 * <ul>
 *  <li>Login,</li>
 *  <li>Control Panel,</li>
 *  <li>Edit Page,</li>
 *  <li>Edit As Plain Text,</li>
 *  <li>New Page,</li>
 *  <li>New Child Page.</li>
 * </ul>
 *
 * Items are included when appropriate.
 */
public final class AdminMenu extends AbstractTag {
  public static final String MODE_NORMAL = "normal";
  public static final String MODE_HIDDEN = "hidden";

  private String mode;
  private String separator = " ";
  private String style;

  public void setMode(String mode) {
    this.mode = mode;
  }

  public void setSeparator(String separator) {
    if (separator != null) {
      this.separator = separator;
    }
  }

  public void setStyle(String style) {
    this.style = style;
  }

  public void writeTag() throws IOException {
    UserInfo user = (UserInfo) pageContext.getAttribute("userInfo",
      PageContext.SESSION_SCOPE);
    Locale locale = WebUtils.getPageLocale(pageContext);
    ResourceBundle bundle = ResourceBundle.getBundle("org/meshcms/webui/Locales", locale);

    String a1 = "<a" +
      (Utils.isNullOrEmpty(style) ? "" : " class=\"" + style + "\"") +
      " href=\"";
    String a2 = "\">";
    String a3 = "</a>";
    List l = new ArrayList();

    if (user == null || user.isGuest()) {
      if ((mode == null || mode.equals(MODE_NORMAL)) &&
          !org.meshcms.extra.StaticExporter.isExportRequest(request)) {
        l.add(a1 + adminRelPath.add("login.jsp") + a2 + bundle.getString("adminLogin") + a3);
      }
    } else {
      l.add(a1 + adminRelPath.add("index.jsp") + a2 + bundle.getString("homeTitle") + a3);

      if (!isEdit) {
        if (user.canWrite(webSite, pagePath)) {
          if (webSite.isVisuallyEditable(pagePath)) {
            l.add(a1 + request.getRequestURI() + '?' + HitFilter.ACTION_NAME +
                '=' + HitFilter.ACTION_EDIT + a2 +
                bundle.getString("adminEditPage") + a3);
          }

          if (FileTypes.isPage(pagePath.getLastElement())) {
            l.add(a1 + adminRelPath.add("editsrc.jsp") + "?path=" + pagePath + a2 +
                bundle.getString("adminEditText") + a3);

            l.add(a1 + adminRelPath.add("editsrc.jsp") + "?path=" + pagePath + "&amp;tidy=true" +
                a2 + bundle.getString("adminTidy") + a3);

            Path pathInMenu = webSite.getSiteMap().getPathInMenu(pagePath);

            if (!pathInMenu.isRoot()) {
              Path parentPath = pathInMenu.getParent();

              if (user.canWrite(webSite, parentPath)) {
                l.add(a1 + adminRelPath.add("createpage.jsp") + "?popup=false&amp;path=" +
                    parentPath + a2 + bundle.getString("adminNewPage") + a3);
              }
            }

            if (webSite.isDirectory(pathInMenu)) {
              l.add(a1 + adminRelPath.add("createpage.jsp") + "?popup=false&amp;path=" +
                  pathInMenu + a2 + bundle.getString("adminNewChildPage") + a3);
            }
          }
        }
      }
    }

    getOut().write(l.size() > 0 ? Utils.generateList(l, separator) : "&nbsp;");
  }

  public String getMode() {
    return mode;
  }

  public String getSeparator() {
    return separator;
  }

  public String getStyle() {
    return style;
  }
}
