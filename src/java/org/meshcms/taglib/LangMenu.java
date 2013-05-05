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
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.servlet.jsp.JspException;
import org.meshcms.core.PageInfo;
import org.meshcms.core.SiteMap;
import org.meshcms.core.WebUtils;
import org.meshcms.util.Path;
import org.meshcms.util.Utils;

public class LangMenu extends AbstractTag {
  private static ResourceBundle flagsBundle;
  
  private String separator = " ";
  private String pre;
  private String post;
  private String flags;
  private String names = "true";
  
  public void setSeparator(String separator) {
    if (separator != null) {
      this.separator = separator;
    }
  }
  
  public String getSeparator() {
    return separator;
  }
  
  public void writeTag() throws IOException, JspException {
    SiteMap siteMap = webSite.getSiteMap();
    
    boolean notTranslatable =
        pagePath.isRoot() || siteMap.getPageInfo(pagePath) == null;
    
    List langList = siteMap.getLangList();
    
    if (langList.size() > 1) {
      Iterator iter = langList.iterator();
      boolean putSeparator = false;
      Writer w = getOut();
      
      if (pre != null) {
        w.write(pre);
      }
      
      Path baseIconPath = Utils.isTrue(flags) ? webSite.getAdminPath().add("flags") : null;
      
      while (iter.hasNext()) {
        if (putSeparator) {
          w.write(separator);
        }
        
        putSeparator = true;
        SiteMap.CodeLocalePair lang = (SiteMap.CodeLocalePair) iter.next();
        String langCode = lang.getCode();
        String localeName = Utils.encodeHTML(lang.getName());
        Path link = null;
        String onClick = null;
        
        if (notTranslatable) {
          link = webSite.getLink(new Path(langCode), pageDirPath);
        } else {
          if (!langCode.equalsIgnoreCase(pagePath.getElementAt(0))) {
            Path path = siteMap.getServedPath(pagePath.replace(0, langCode));

            if (!webSite.getFile(path).isFile()) {
              if (userInfo != null && userInfo.canWrite(webSite, path)) {
                PageInfo ppi = siteMap.getParentPageInfo(pagePath);

                if (ppi != null && ppi.getLevel() > 0) {
                  Path pPath = ppi.getPath().replace(0, langCode);

                  if (siteMap.getPageInfo(pPath) != null) {
                    ResourceBundle bundle =
                        ResourceBundle.getBundle("org/meshcms/webui/Locales",
                        WebUtils.getPageLocale(pageContext));
                    String msg = Utils.replace(bundle.getString("confirmTranslation"),
                        '\'', "\\'");
                    
                    onClick = "if (confirm('" + msg +"')) {location.href='" +
                        adminRelPath + "/createpage.jsp?popup=false&newdir=false&fullpath=" +
                        path + "';return false}";
                  }
                }
              }

              path = new Path(langCode);
            }

            link = webSite.getLink(path, pageDirPath);
          }
        }
        
        if (link != null) {
          w.write("<a href=\"" + link + "\"");
          
          if (onClick != null) {
            w.write(" onclick=\"" + onClick + "\"");
          }
          
          w.write(">");
        }
        
        if (Utils.isTrue(flags)) {
          if (flagsBundle == null) {
            flagsBundle = ResourceBundle.getBundle("org/meshcms/webui/Flags");
          }
          
          try {
            Path iconPath = baseIconPath.add(flagsBundle.getString(langCode) + ".png");
            w.write("<img src='" + webSite.getLink(iconPath, pageDirPath) +
                "' alt='" + localeName + "' title='" + localeName + "'/>");
          } catch (Exception ex) {
            ex.printStackTrace();
          }
          
          if (Utils.isTrue(names)) {
            w.write(" ");
          }
        }
        
        if (Utils.isTrue(names)) {
          w.write(localeName);
        }
        
        if (link != null) {
          w.write("</a>");
        }
      }
      
      if (post != null) {
        w.write(post);
      }
    }
  }
  
  public String getPre() {
    return pre;
  }
  
  public void setPre(String pre) {
    this.pre = pre;
  }
  
  public String getPost() {
    return post;
  }
  
  public void setPost(String post) {
    this.post = post;
  }
  
  public String isFlags() {
    return flags;
  }
  
  public void setFlags(String flags) {
    this.flags = flags;
  }

  public String getNames() {
    return names;
  }

  public void setNames(String names) {
    this.names = names;
  }
}
