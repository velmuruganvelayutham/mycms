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

package org.meshcms.core;

import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.meshcms.util.Utils;

/**
 * Rebuilds a page from its components.
 */
public final class PageAssembler {
  /**
   * Generic string used to indicate an empty value.
   */
  public static final String EMPTY = "(none)";
  
  /**
   * The name of the properties for the modules to include in the page.
   */
  public static final String MODULES_PARAM = "meshcmsmodules";
  
  /**
   * The name of the properties for the mail recipient of the page.
   */
  public static final String EMAIL_PARAM = "meshcmsmail";
  
  public static final Pattern NAME_VALUE_REGEX =
      Pattern.compile("\\s*+([^,=]+)\\s*+=\\s*+([^,=]+)\\s*+");
  
  private String title;
  private String head;
  private String body;
  private String email;
  // private StringBuffer htmlTag = new StringBuffer();
  private StringBuffer bodyTag = new StringBuffer();
  private Properties mod_templates, mod_args, mod_params, mod_titles;
  
  public PageAssembler() {
    mod_templates = new Properties();
    mod_args = new Properties();
    mod_params = new Properties();
    mod_titles = new Properties();
  }
  
  /**
   * Adds a property to the page.
   */
  public void addProperty(String name, String value) {
    value = Utils.noNull(value).trim();
    
    if (name.equals("pagetitle")) {
      title = value;
    } else if (name.equals("meshcmshead")) {
      head = value;
    } else if (name.equals("meshcmsbody")) {
      body = value;
    } else if (name.equals(EMAIL_PARAM)) {
      email = value;
    } else if (name.startsWith(ModuleDescriptor.TEMPLATE_ID)) {
      if (!value.equals(EMPTY)) { // EMPTY is the value when "no module" is selected
        // name is something like sel_location: we need to set location->value
        // value is the name of the module template
        mod_templates.setProperty(name.substring(ModuleDescriptor.TEMPLATE_ID.length()), value);
      }
    } else if (name.startsWith(ModuleDescriptor.ARGUMENT_ID)) {
      if (!Utils.isNullOrEmpty(value)) {
        // name is something like arg_location: we need to set location->value
        // value is the name of the module argument (i.e. the selected file/folder)
        mod_args.setProperty(name.substring(ModuleDescriptor.ARGUMENT_ID.length()),
            Utils.encodeHTML(value));
      }
    } else if (name.startsWith(ModuleDescriptor.PARAMETERS_ID)) {
      mod_params.setProperty(name.substring(ModuleDescriptor.PARAMETERS_ID.length()),
          Utils.encodeHTML(value));
    } else if (name.startsWith(ModuleDescriptor.TITLE_ID)) {
      mod_titles.setProperty(name.substring(ModuleDescriptor.TITLE_ID.length()),
          Utils.encodeHTML(value));
    } else if (name.startsWith("body.")) {
      bodyTag.append(' ').append(name.substring(5)).append("=\"").append(Utils.encodeHTML(value)).append('\"');
    /*
      } else if (name.startsWith("meta.")) {
        //
      } else if (name.startsWith("page.")) {
        //
      } else if (name.startsWith("mce_editor")) {
        //
      } else {
        htmlTag.append(' ').append(name).append("=\"").append(value).append('\"');
     */
    }
  }
  
  /**
   * Returns the complete page.
   */
  public String getPage() {
    StringBuffer sb = new StringBuffer("<html");
    // sb.append(htmlTag);
    
    Enumeration locations = mod_templates.keys();

    sb.append(">\n<head>\n<title>");
    sb.append(Utils.noNull(title));
    sb.append("</title>\n");
    sb.append(Utils.noNull(head));
    
    if (Utils.checkAddress(email)) { // we have an e-mail address
      sb.append("<meta name=\"meshcms:mailform\" content=\"");
      sb.append(email).append("\" />\n");
    }
    
    while (locations.hasMoreElements()) {
      String loc = locations.nextElement().toString();
      String template = mod_templates.getProperty(loc);
      
      if (!Utils.isNullOrEmpty(template)) {
        String argument = mod_args.getProperty(loc);
        
        if (Utils.isNullOrEmpty(argument)) {
          argument = EMPTY;
        }
        
        String mTitle = mod_titles.getProperty(loc);
        
        if (Utils.isNullOrEmpty(mTitle)) {
          mTitle = EMPTY;
        }
        
        String params = mod_params.getProperty(loc);
        
        appendMeta(sb, loc, ModuleDescriptor.TEMPLATE_ID, template);
        appendMeta(sb, loc, ModuleDescriptor.ARGUMENT_ID, argument);
        appendMeta(sb, loc, ModuleDescriptor.TITLE_ID, mTitle);
        
        Matcher m = NAME_VALUE_REGEX.matcher(Utils.noNull(params));
        
        while (m.find()) {
          appendMeta(sb, loc, m.group(1), m.group(2));
        }
      }
    }
    
    sb.append("</head>\n<body");
    sb.append(bodyTag);
    sb.append(">\n");
    sb.append(Utils.noNull(body));
    sb.append("\n</body>\n</html>\n");
    
    return sb.toString();
  }
  
  private static void appendMeta(StringBuffer sb, String location, String name,
      String value) {
    sb.append("<meta name=\"meshcms:module\" content=\"");
    sb.append(location).append(':').append(name).append(':').append(value);
    sb.append("\" />\n");
  }
}
