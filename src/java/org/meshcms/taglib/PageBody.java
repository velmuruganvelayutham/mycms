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
import java.util.Locale;
import java.util.ResourceBundle;
import org.meshcms.core.Configuration;
import org.meshcms.core.HitFilter;
import org.meshcms.core.WebUtils;
import org.meshcms.util.Utils;
import org.meshcms.webui.Help;

/**
 * Writes the page body or the main part of the page editor.
 */
public class PageBody extends AbstractTag {
  public static final int MINIMUM_PAGE_SIZE = 32;
  public void writeTag() throws IOException {
    String body = getPage().getBody();
    
    // Let's prevent caching of pages with a "small body"
    if (body.length() < MINIMUM_PAGE_SIZE) {
      WebUtils.setBlockCache(request);
    }
    
    if (webSite.getConfiguration().isReplaceThumbnails()) {
      body = WebUtils.replaceThumbnails(webSite, body, request.getContextPath(), pagePath);
    }
    
    Writer w = getOut();

    if (!(userInfo == null || userInfo.isGuest()) &&
        webSite.getConfiguration().isRedirectRoot() &&
        webSite.getSiteMap().getPathInMenu(pagePath).isRoot() &&
        HitFilter.getPreferredLanguage(request) != null) {
      Locale pl = WebUtils.getPageLocale(pageContext);
      ResourceBundle bundle =
          ResourceBundle.getBundle("org/meshcms/webui/Locales", pl);
      w.write("<p class='meshcmsinfo'>" + bundle.getString("pageRedirectionAlert") + "</p>");
    }
    
    w.write(body);
  }
  
  public void writeEditTag() throws IOException {
    Locale locale = WebUtils.getPageLocale(pageContext);
    ResourceBundle bundle = ResourceBundle.getBundle("org/meshcms/webui/Locales", locale);
    
    Writer w = getOut();
    
    w.write("<div align='right'>" +
        Help.icon(webSite, pagePath, Help.EDIT_PAGE, userInfo) + "</div>\n");
    
    w.write("<fieldset class='meshcmseditor'>\n");
    w.write("<legend>" + bundle.getString("editorMainSection") + "</legend>\n");
    w.write("<div class='meshcmsfieldlabel'><label for='pagetitle'>" +
        bundle.getString("editorPageTitle") + "</label></div>\n");
    w.write("<div class='meshcmsfield'><input type='text' id='pagetitle' name='pagetitle' value=\"" +
        Utils.noNull(getPage().getTitle()) +
        "\" style='width: 80%;' /></div>\n");
    
    w.write("<div class='meshcmsfieldlabel'><img alt=\"\" src=\"" +
        adminRelPath.add("/filemanager/images/bullet_toggle_plus.png") +
        "\" id='togglehead' onclick=\"javascript:editor_toggleHideShow('meshcmshead','togglehead');\" />\n");
    w.write("<label for='meshcmshead'>" + bundle.getString("editorPageHead") + "</label></div>\n");
    
    String head = getHead();
    head = PageHead.META_REGEX.matcher(head).replaceAll("");
    head = MailForm.META_REGEX.matcher(head).replaceAll("");
    
    w.write("<div class='meshcmsfield'><textarea id='meshcmshead' name='meshcmshead' rows='5' cols='80' style='height: 5em; width: 100%; display: none;'>" +
        head + "</textarea></div>\n");
    
    w.write("<div class='meshcmsfieldlabel'><label for='meshcmsbody'>" +
        bundle.getString("editorPageBody") + "</label></div>\n");
    w.write("<div class='meshcmsfield'><textarea id='meshcmsbody' name='meshcmsbody' rows='25' cols='80' style='height: 30em; width: 100%;'>");
    w.write(Utils.encodeHTML(getPage().getBody(), true));
    w.write("</textarea></div>\n");
    w.write("<div class='meshcmsfield'>\n");
    
    switch (webSite.getConfiguration().getTidy()) {
      case Configuration.TIDY_ASK:
        w.write("<input type='checkbox' id='tidy' name='tidy' value='true' />\n");
        w.write("<label for='tidy'>" + bundle.getString("editorTidy") + "</label>\n");
        break;
      case Configuration.TIDY_YES:
        w.write("<input type='hidden' id='tidy' name='tidy' value='true' />\n");
        break;
    }
    
    w.write("<input type='checkbox' id='keepFileDate' name='keepFileDate' value='true' />\n");
    w.write("<label for='keepFileDate'>" + bundle.getString("editorKeepFileDate") + "</label>\n");
    w.write("</div>\n");
    
    w.write("<div class='meshcmsbuttons'><input type='submit' value='" +
        bundle.getString("genericSave") + "' /></div>\n");
    w.write("</fieldset>");
  }
}
