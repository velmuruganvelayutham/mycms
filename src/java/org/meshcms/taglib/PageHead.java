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
import java.util.regex.Pattern;
import javax.servlet.jsp.PageContext;
import org.meshcms.core.HitFilter;
import org.meshcms.core.WebUtils;
import org.meshcms.util.Path;
import org.meshcms.util.Utils;

/**
 * Writes the page head. Also adds some init variables when editing. Please note
 * that since this tag is used within the &lt;head&gt; tag, the field to edit
 * the page head are displayed by {@link PageBody}
 */
public class PageHead extends AbstractTag {
  public static final Pattern META_REGEX = Pattern.compile("(?s)(?i)<meta\\s+" +
      "(?:name\\s*=\\s*([\"'])meshcms:module\\1\\s+content\\s*=\\s*([\"'])" +
      "(.+?)\\2|content\\s*=\\s*([\"'])(.+?)\\4\\s+name\\s*=\\s*([\"'])" +
      "meshcms:module\\6)[^>]*>\\n*");

  private String dropStyles;
  private String dropScripts;

  public void writeTag() throws IOException {
    Writer w = getOut();
    w.write(getHeadContent());
    Locale locale = (Locale) pageContext.getAttribute(HitFilter.LOCALE_ATTRIBUTE,
        PageContext.REQUEST_SCOPE);

    if (locale != null) {
      w.write("\n<meta http-equiv=\"Content-Language\" content=\"" + locale + "\" />");
    }
  }

  public void writeEditTag() throws IOException {
    Path linkListPath = new Path(adminRelPath, "tinymce_linklist.jsp");
    Locale locale = WebUtils.getPageLocale(pageContext);
    ResourceBundle bundle = ResourceBundle.getBundle("org/meshcms/webui/Locales", locale);
    String langCode = bundle.getString("TinyMCELangCode");

    if (Utils.isNullOrEmpty(langCode)) {
      langCode = locale.getLanguage();
    }

    Writer w = getOut();
    w.write(getHeadContent());
    Path tinyMCEPath =
        webSite.getFile(webSite.getCMSPath().add("tiny_mce")).exists()
        ? webSite.getCMSPath() : webSite.getAdminScriptsPath();
    w.write("\n<script type='text/javascript' src='" +
      webSite.getLink(tinyMCEPath, pageDirPath) +
      "/tiny_mce/tiny_mce.js'></script>\n");
    w.write("<script type='text/javascript'>\n");
    w.write("// <![CDATA[\n");
    w.write(" var contextPath = \"" + request.getContextPath() + "\";\n");
    w.write(" var adminPath = \"" + webSite.getAdminPath() + "\";\n");
    w.write(" var languageCode = \"" + langCode + "\";\n");
    w.write(" var linkListPath = \"" + linkListPath + "\";\n");
    w.write(" var cssPath = \"" + WebUtils.getThemeCSSPath(request, pageDirPath) + "\";\n");
    w.write("// ]]>\n");
    w.write("</script>\n");
    Path p = webSite.getLink(webSite.getAdminScriptsPath(), pageDirPath);
    w.write("<script type='text/javascript' src='" +
        p.add("/jquery/jquery.min.js") + "'></script>\n");
    w.write("<script type='text/javascript' src='" +
        p.add("/editor.js") + "'></script>\n");
    Path tinyMCEInitPath =
        webSite.getFile(webSite.getCMSPath().add("tinymce_init.js")).exists()
        ? webSite.getCMSPath() : webSite.getAdminScriptsPath();
    w.write("<script type='text/javascript' src='" +
      webSite.getLink(tinyMCEInitPath, pageDirPath).add("tinymce_init.js") + "'></script>");
  }

  private String getHeadContent() {
    String head = getHead();
    head = META_REGEX.matcher(head).replaceAll("");

    if (Utils.isTrue(dropStyles)) {
      head = head.replaceAll("(?i)(?s)<style[^>]*>.*?</style[^>]*>\\n*", "");
      head = head.replaceAll("(?s)(?i)<link[^>]+rel\\s*=\\s*([\"'])stylesheet\\1[^>]*>\\n*", "");
    }

    if (Utils.isTrue(dropScripts)) {
      head = head.replaceAll("(?i)(?s)<script[^>]*>.*?</script[^>]*>\\n*", "");
    }

    return head;
  }

  public String getDropStyles() {
    return dropStyles;
  }

  public void setDropStyles(String dropStyles) {
    this.dropStyles = dropStyles;
  }

  public String getDropScripts() {
    return dropScripts;
  }

  public void setDropScripts(String dropScripts) {
    this.dropScripts = dropScripts;
  }
}
