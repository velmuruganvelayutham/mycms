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
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import org.meshcms.core.ModuleDescriptor;
import org.meshcms.core.PageAssembler;
import org.meshcms.core.SiteMap;
import org.meshcms.core.WebUtils;
import org.meshcms.util.Path;
import org.meshcms.util.Utils;

/**
 * Creates a mail form if a recipient has been specified for the page being
 * viewed. If the editor is active, this tag writes the form field needed to
 * specify the recipient address.
 */
public final class MailForm extends AbstractTag {
  public static final Pattern META_REGEX = Pattern.compile("(?s)(?i)<meta\\s+" +
      "(?:name\\s*=\\s*([\"'])meshcms:mailform\\1\\s+content\\s*=\\s*([\"'])" +
      "(.+?)\\2|content\\s*=\\s*([\"'])(.+?)\\4\\s+name\\s*=\\s*([\"'])" +
      "meshcms:mailform\\6)[^>]*>\\n*");
  
  public void writeTag() throws IOException, JspException {
    String email = getMailFormAddress();
    
    if (Utils.checkAddress(email)) {
      try {
        Path mailModulePath = webSite.getAdminModulesPath().add("mail");
        String location = "meshcmsmailformtag";
        ModuleDescriptor md = new ModuleDescriptor();
        md.setLocation(location);
        md.setArgument(email);
        md.setModulePath(mailModulePath);
        md.setPagePath(pagePath);
        String moduleCode = "meshcmsmodule_" + location;
        request.setAttribute(moduleCode, md);
        pageContext.include("/" + webSite.getServedPath(mailModulePath) + "/" +
            SiteMap.MODULE_INCLUDE_FILE + "?modulecode=" + moduleCode);
      } catch (ServletException ex) {
        throw new JspException(ex);
      }
    } else {
      getOut().write("&nbsp;");
    }
  }
  
  public void writeEditTag() throws IOException, JspException {
    final String uniqueHash = Integer.toString(new Object().hashCode());
    final String tagIdPrefix = "meshcmsmodule_mail_"+ uniqueHash +"_";
    final String idCont = tagIdPrefix +"cont";
    final String idElem = tagIdPrefix +"elem";
    final String idIcon = tagIdPrefix +"icon";
    final boolean isEditorModulesCollapsed = webSite.getConfiguration().isEditorModulesCollapsed();
    
    Locale locale = WebUtils.getPageLocale(pageContext);
    ResourceBundle bundle = ResourceBundle.getBundle("org/meshcms/webui/Locales", locale);
    String email = getMailFormAddress();
    Writer w = getOut();
    
    if (isEditorModulesCollapsed) {
      MessageFormat formatter = new MessageFormat("", locale);
      w.write("<div id=\""+ idCont +"\" class='meshcmsfieldlabel' " +
          " style=\"cursor:pointer;\" onclick=\"javascript:editor_moduleShow('"+ idCont +"','"+ idElem +"','"+ idIcon +"');\">" +
          "<img alt=\"\" src=\"" + adminRelPath.add("/filemanager/images/bullet_toggle_plus.png") + "\" id=\""+ idIcon +"\" />\n");
      Object[] args = { bundle.getString("editorMailTitle"), email != null ? bundle.getString("editorMailTitle") : bundle.getString("editorNoTemplate"),
      Utils.noNull(email), "" };
      formatter.applyPattern(bundle.getString("editorModuleLocExt"));
      w.write("<label for=\""+ idElem +"\">"+ formatter.format(args) +"</label>");
      w.write("</div>");
    }
    
    w.write("<fieldset  "+ (isEditorModulesCollapsed ? "style=\"display:none;\"" : "") +
        "class='meshcmseditor' id=\""+ idElem +"\">\n");
    w.write("<legend>" + bundle.getString("editorMailTitle") + "</legend>\n");
    w.write("<div class='meshcmsfieldlabel'>" + bundle.getString("editorMail") + "</div>\n");
    w.write("<div class='meshcmsfield'><input type='text' id='" +
        PageAssembler.EMAIL_PARAM + "' name='" +
        PageAssembler.EMAIL_PARAM + "' value=\"" +
        Utils.noNull(email) + "\" style='width: 80%;' /></div>\n");
    w.write("</fieldset>");
  }
}
