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

package org.meshcms.webui;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import org.meshcms.core.MainWebSite;
import org.meshcms.core.UserInfo;
import org.meshcms.core.VirtualWebSite;
import org.meshcms.core.WebSite;
import org.meshcms.core.WebUtils;
import org.meshcms.util.DirectoryParser;
import org.meshcms.util.Path;

/**
 * Used internally to create the JavaScript code needed by xTree to display
 * the folder tree. xTree has been created by <a href="http://webfx.eae.net/">WebFX</a>.
 */
public class FolderXTree extends DirectoryParser {
  WebSite webSite;
  UserInfo userInfo;
  Writer writer;
  String thumbsParam;
  String rootName;

  /**
   * Creates a new instance.
   *
   * @param writer used to write down the needed JavaScript code.
   */
  public FolderXTree(WebSite webSite, UserInfo userInfo, Writer writer,
      String thumbsParam, String rootName) {
    this.webSite = webSite;
    this.userInfo = userInfo;
    this.writer = writer;
    this.thumbsParam = thumbsParam;
    this.rootName = rootName;
    setRecursive(true);
    setSorted(true);
    setInitialDir(webSite.getRootFile());
    setDaemon(true);
    setName("XTree builder");
  }

  protected boolean preProcess() {
    try {
      writer.write("var folder0 = new WebFXTree('" + rootName + "');\n");
      writer.write("folder0.setBehavior('explorer');\n");
      writer.write("folder0.action='showlist.jsp?folder=" + thumbsParam + "';\n");
      writer.write("folder0.target='listframe';\n");
      //writer.write("folder0.icon='images/world.gif';\n");
      //writer.write("folder0.openIcon='images/world.gif';\n");
    } catch (IOException ex) {
      return false;
    }

    return true;
  }

  protected void processFile(File file, Path path) {
    // nothing to do here
  }

  protected boolean preProcessDirectory(File file, Path path) {
    if (path.equals(webSite.getRepositoryPath())) {
      return false;
    }

    boolean include = true;

    try {
      DirectoryInfo di = getDirectoryInfo(webSite, userInfo, path);

      if (di.include) {
        int code = WebUtils.getMenuCode(path);
          writer.write("\nvar folder" + code + " = new WebFXTreeItem(\"" +
              path.getLastElement() + "\");\n");
          writer.write("folder" + WebUtils.getMenuCode(path.getParent()) +
              ".add(folder" + code + ");\n");
          writer.write("folder" + code + ".action=\"showlist.jsp?folder=" +
              path + thumbsParam + "\";\n");
          writer.write("folder" + code + ".target='listframe';\n");
        if (di.iconName != null) {
          writer.write("folder" + code + ".icon='images/" + di.iconName +
              ".png';\n");
          /* writer.write("folder" + code + ".openIcon='images/" + di.iconName +
              "open.gif';\n"); */
        }
      }
    } catch (IOException ex) {
      return false;
    }

    return include;
  }

  /**
   * Returns info about a directory, based on path and permissions.
   */
  public static DirectoryInfo getDirectoryInfo(WebSite webSite,
      UserInfo userInfo, Path dirPath) {
    DirectoryInfo di = new DirectoryInfo();

    if (dirPath.isContainedIn(webSite.getVirtualSitesPath())) {
      if (userInfo.canDo(UserInfo.CAN_DO_ADMINTASKS)) {
        di.include = true;

        if (dirPath.getElementCount() <
            webSite.getVirtualSitesPath().getElementCount() + 2) {
          di.iconName = "folder_star";
        } else if (webSite instanceof MainWebSite) {
          Path siteRoot =
              dirPath.getPartial(webSite.getVirtualSitesPath().getElementCount() + 1);
          VirtualWebSite vws =
              ((MainWebSite) webSite).getVirtualSite(siteRoot.getLastElement());
          return getDirectoryInfo(vws, userInfo, dirPath.getRelativeTo(siteRoot));
        }
      } else {
        di.include = false;
      }
    } else if (webSite.isSystem(dirPath)) {
      di.include = false;
      /* di.iconName = "folder_error";
      di.include = userInfo.canDo(UserInfo.CAN_DO_ADMINTASKS); */
    } else if (dirPath.isContainedIn(webSite.getGeneratedFilesPath())) {
      di.include = false;
    } else if (dirPath.isContainedIn(webSite.getCMSPath())) {
      di.iconName = "folder_brick";
    }

    return di;
  }

  /**
   * Encapsulates inclusion flag and icon for a directory.
   */
  public static class DirectoryInfo {
    public boolean include = true;
    public String iconName = null;
  }
}
