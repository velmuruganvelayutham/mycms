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
import java.io.PrintWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import org.meshcms.core.ModuleDescriptor;
import org.meshcms.core.PageAssembler;
import org.meshcms.core.SiteMap;
import org.meshcms.core.WebUtils;
import org.meshcms.util.Path;
import org.meshcms.util.Utils;
import org.meshcms.webui.Help;

/**
 * Writes a module or the needed fields to edit it.
 */
public final class Module extends AbstractTag {
  public static final String DATE_NONE = "none";
  public static final String DATE_NORMAL = "normal";
  public static final String DATE_FULL = "full";

  private String name;
  private String date;
  private String style;
  private String location = "";
  private String alt = "&nbsp;";
  private String parameters;

  public void setName(String name) {
    this.name = name;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public void setStyle(String style) {
    this.style = style;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public void setAlt(String alt) {
    this.alt = alt;
  }

  public void writeTag() throws IOException, JspException {
    if (date == null) {
      date = DATE_NONE;
    }

    ModuleDescriptor md = getModuleDescriptor(location, name);
    boolean showAlt = true;

    if (md != null) {
      if (parameters != null) {
        md.parseParameters(parameters);
      }

      Path modulePath = webSite.getModulePath(md.getTemplate());

      if (modulePath != null) {
        md.setPagePath(pagePath);
        md.setModulePath(modulePath);
        md.setDateFormat(date);
        md.setStyle(style);

        String moduleCode = "meshcmsmodule_" + location;
        request.setAttribute(moduleCode, md);

        Path jspPath = modulePath.add(SiteMap.MODULE_INCLUDE_FILE);

        if (WebUtils.verifyJSP(webSite, jspPath)) {
          try {
            pageContext.include("/" + webSite.getServedPath(jspPath) +
                "?modulecode=" + moduleCode);
            showAlt = false;
          } catch (ServletException ex) {
            WebUtils.setBlockCache(request);
            webSite.log("Exception while including module " + modulePath, ex);

            if (!webSite.getConfiguration().isHideExceptions()) {
              Writer w = getOut();
              PrintWriter pw = new PrintWriter(w);
              w.write("<pre class='meshcmserror'>\n");
              ex.printStackTrace(pw);

              Throwable t = ex.getRootCause();

              if (t != null) {
                t.printStackTrace(pw);
              }

              w.write("</pre>");
              showAlt = false;
            }
          }
        } else if (!(userInfo == null || userInfo.isGuest())) {
          getOut().write("Module not allowed");
          showAlt = false;
        }
      }
    }

    if (showAlt) {
      getOut().write(alt);
    }
  }

  public void writeEditTag() throws IOException, JspException {
    String uniqueHash = Integer.toString(new Object().hashCode());
    String tagIdPrefix = "meshcmsmodule_"+ location +"_"+ uniqueHash +"_";
    String idCont = tagIdPrefix +"cont";
    String idElem = tagIdPrefix +"elem";
    String idIcon = tagIdPrefix +"icon";
    boolean isEditorModulesCollapsed = webSite.getConfiguration().isEditorModulesCollapsed();

    String template = null;
    String argPath = null;
    String advParms = null;

    ModuleDescriptor md = getModuleDescriptor(location, name);

    if (md != null) {
      template = md.getTemplate();
      argPath  = md.getArgument();
      advParms = Utils.listProperties(md.getAdvancedParams(), ", ");
    }

    Locale locale = WebUtils.getPageLocale(pageContext);
    ResourceBundle bundle = ResourceBundle.getBundle("org/meshcms/webui/Locales", locale);
    MessageFormat formatter = new MessageFormat("", locale);

    Writer w = getOut();

    Object[] args = {
      location,
      template != null ? Utils.beautify(template,true) : bundle.getString("editorNoTemplate"),
      Utils.limitedLength(argPath, 80),
      Utils.limitedLength(advParms, 80)
    };

    if (isEditorModulesCollapsed) {
      w.write("<div id=\"" + idCont + "\" class='meshcmsfieldlabel' " +
          " style=\"cursor:pointer;position:relative;\" onclick=\"javascript:editor_moduleShow('" +
          idCont + "','" + idElem + "','" + idIcon + "');\">" +
          "<img alt=\"\" src=\"" + adminRelPath.add("filemanager/images/bullet_toggle_plus.png") + "\" id=\"" + idIcon + "\" />\n");
      formatter.applyPattern(bundle.getString("editorModuleLocExt"));
      w.write("<label for=\"" + idElem + "\">" + formatter.format(args) + "</label>");
      w.write("</div>");
    }

    w.write("<fieldset "+ (isEditorModulesCollapsed ? "style=\"display:none;\"" : "") +
        " id=\""+ idElem +"\" class='meshcmseditor' >\n");
    formatter.applyPattern(bundle.getString("editorModuleLoc"));
    w.write(" <legend>" + formatter.format(args) + "</legend>\n");

    if (name != null) {
      w.write(bundle.getString("editorFixedModule"));

      if (argPath != null) {
        w.write("<img alt=\"\" src='" + adminRelPath.add("filemanager/images/application_view_detail.png") + "' title='" +
            bundle.getString("editorBrowseModule") +
            "' onclick=\"javascript:window.open('" +
            adminRelPath.add("filemanager/index.jsp") + "?folder=" +
            Utils.escapeSingleQuotes(argPath) +
            "', '_blank').focus();\" style='vertical-align:middle;' />\n");
      }
    } else {
      w.write(" <div class='meshcmsfieldlabel'><label for='" +
          ModuleDescriptor.TITLE_ID + location + "'>" +
          bundle.getString("editorModuleTitle") + "</label></div>\n");
      w.write(" <div class='meshcmsfield'><input type='text' id='" +
          ModuleDescriptor.TITLE_ID + location + "' name='" +
          ModuleDescriptor.TITLE_ID + location + "' value=\"" +
          (md == null ? "" : Utils.noNull(md.getTitle())) +
          "\" style='width: 80%;' /></div>\n");

      w.write(" <div class='meshcmsfieldlabel'><label for='" +
          ModuleDescriptor.TEMPLATE_ID + location + "'>" +
          bundle.getString("editorModuleTemplate") + "</label></div>\n");
      w.write(" <div class='meshcmsfield'>\n  <select name='" +
          ModuleDescriptor.TEMPLATE_ID + location + "' id='" +
          ModuleDescriptor.TEMPLATE_ID + location + "'>\n");
      w.write("   <option value='" + PageAssembler.EMPTY + "'>" +
          bundle.getString("editorNoTemplate") + "</option>\n");

      String[] mtNames = webSite.getSiteMap().getModuleNames();

      for (int i = 0; i < mtNames.length; i++) {
        w.write("   <option value='" + mtNames[i] + "'");

        if (md != null && mtNames[i].equals(template)) {
          w.write(" selected='selected'");
        }

        w.write(">" + Utils.beautify(Utils.removeExtension(mtNames[i]), true) + "</option>\n");
      }

      w.write("  </select>&nbsp;");
      w.write(Help.icon(webSite, pagePath, Help.MODULES, userInfo,
          "module_'+document.getElementById('" + ModuleDescriptor.TEMPLATE_ID +
          location + "').value+'", true));
      w.write("\n </div>\n");
      w.write(" <div class='meshcmsfieldlabel'><label for='" +
          ModuleDescriptor.ARGUMENT_ID + location + "'>" +
          bundle.getString("editorModuleArgument") + "</label></div>\n");
      w.write(" <div class='meshcmsfield'><input type='text' id='" +
          ModuleDescriptor.ARGUMENT_ID + location + "' name='" +
          ModuleDescriptor.ARGUMENT_ID + location + "' value=\"" +
          (md == null || argPath == null ? "" : argPath) +
          "\" style='width: 80%;' /><img alt=\"\" src='" +
          adminRelPath.add("/filemanager/images/application_view_detail.png") + "' title='" + bundle.getString("genericBrowse") +
          "' onclick=\"javascript:editor_openFileManager('" +
          ModuleDescriptor.ARGUMENT_ID + location + "');\" style='vertical-align:middle;' /></div>\n");

      w.write(" <div class='meshcmsfieldlabel'><label for='" +
          ModuleDescriptor.PARAMETERS_ID + location + "'>" +
          bundle.getString("editorModuleParameters") + "</label></div>\n");
      w.write(" <div class='meshcmsfield'><input type='text' id='" +
          ModuleDescriptor.PARAMETERS_ID + location + "' name='" +
          ModuleDescriptor.PARAMETERS_ID + location + "' value=\"" +
          (md == null || md.getAdvancedParams() == null ? "" :
            Utils.listProperties(md.getAdvancedParams(), ", ")) +
          "\" style='width: 80%;' /></div>\n");
    }

    w.write("</fieldset>");
  }

  public String getName() {
    return name;
  }

  public String getDate() {
    return date;
  }

  public String getStyle() {
    return style;
  }

  public String getLocation() {
    return location;
  }

  public String getAlt() {
    return alt;
  }

  public String getParameters() {
    return parameters;
  }

  public void setParameters(String parameters) {
    this.parameters = parameters;
  }
}
