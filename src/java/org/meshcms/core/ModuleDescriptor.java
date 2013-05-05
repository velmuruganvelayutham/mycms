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

import java.io.File;
import java.text.DateFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import org.meshcms.taglib.Module;
import org.meshcms.util.Path;
import org.meshcms.util.Utils;

/**
 * Stores the description of a module when read from the page.
 */
public class ModuleDescriptor {
  /**
   * Location parameter.
   */
  public static final String LOCATION_ID = "m_loc";

  /**
   * Argument parameter.
   */
  public static final String ARGUMENT_ID = "m_arg";

  /**
   * Template parameter.
   */
  public static final String TEMPLATE_ID = "m_tpl";

  /**
   * Advanced parameters.
   */
  public static final String PARAMETERS_ID = "m_apm";
  
  public static final String TITLE_ID = "m_ttl";
  
  private String location;
  private String template;
  private String argument;
  private String title;
  private Properties advancedParams;

  private Path pagePath;
  private Path modulePath;
  private String dateFormat;
  private String style;

  /**
   * Creates a new empty instance.
   */
  public ModuleDescriptor() {
    //
  }

  /**
   * Creates a new instance and calls {@link #init}.
   */
  public ModuleDescriptor(String data) {
    init(data);
  }

  /**
   * Parses the given String to get location, template and argument.
   */
  public void init(String data) {
    if (data.indexOf(LOCATION_ID) < 0) { // old module definition
      String[] values = Utils.tokenize(data, ":");
      advancedParams = new Properties();

      if (values != null) {
        switch (values.length) {
          case 1: // path only
            setLocation("");
            setTemplate("include.jsp");
            setArgument(values[0]);
            break;
          case 2: // template and path
            setLocation("");
            setTemplate(values[0]);
            setArgument(values[1]);
            break;
          case 3: // location, template and path
            setLocation(values[0]);
            setTemplate(values[1]);
            setArgument(values[2]);
        }
      }
    } else { // new module definition
      parseParameters(data);
    }
  }

  /**
   * Parses the given string using the new format (version 3.0).
   */
  public void parseParameters(String data) {
    advancedParams = new Properties();
    StringTokenizer st = new StringTokenizer(data, ",");
    String value, param;

    while (st.hasMoreTokens()) {
      value = st.nextToken().trim();
      int eqIdx = value.indexOf('=');

      if (eqIdx != -1) {
        param = value.substring(0, eqIdx).trim();
        value = value.substring(eqIdx + 1).trim();

        if (param.equals(LOCATION_ID)) {
          setLocation(value);
        } else if (param.equals(TEMPLATE_ID)) {
          setTemplate(value);
        } else if (param.equals(ARGUMENT_ID)) {
          setArgument(value);
        } else if (param.equals(TITLE_ID)) {
          setTitle(value);
        } else {
          advancedParams.setProperty(param, value);
        }
      }
    }
  }

  /**
   * Checks if this module descriptor has been initialized correctly. This is
   * true if both location and template are not null.
   */
  public boolean isValid() {
    return location != null && template != null;
  }

  /**
   * Returns the name of the module location.
   *
   * @see #setLocation
   */
  public String getLocation() {
    return location;
  }

  /**
   * Sets the name of the module location. Each in a page needs a
   * location name which is unique within the page itself.
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Returns the name of the module template.
   */
  public String getTemplate() {
    return template;
  }

  /**
   * Sets the name of the module template.
   */
  public void setTemplate(String template) {
    this.template = template.endsWith(".jsp") ?
        // old modules were in the form module_name.jsp
        template.substring(0, template.length() - 4) : template;
  }

  /**
   * Returns the name of the module argument.
   *
   * @see #setArgument(String)
   */
  public String getArgument() {
    return argument;
  }

  /**
   * Sets the name of the module argument. The argument is set to null if that
   * String is equal to {@link PageAssembler#EMPTY}.
   */
  public void setArgument(String s) {
    argument = PageAssembler.EMPTY.equals(s) ? null : s;
  }

  /**
   * Returns the advanced parameters as a <code>Properties</code> object.
   */
  public Properties getAdvancedParams() {
    return advancedParams;
  }

  /**
   * Returns the value of the requested advanced parameter.
   */
  public String getAdvancedParam(String paramName, String defaultValue) {
    return advancedParams == null ? defaultValue :
     advancedParams.getProperty(paramName, defaultValue);
  }
  
  public void setAdvancedParam(String paramName, String value) {
    if (advancedParams == null) {
      advancedParams = new Properties();
    }
    
    advancedParams.setProperty(paramName, value);
  }

  /**
   * Sets the advanced parameters values.
   */
  public void setAdvancedParams(Properties advancedParams) {
    this.advancedParams = advancedParams;
  }

  /**
   * Returns the path of the page that contains the module.
   */
  public Path getPagePath() {
    return pagePath;
  }

  /**
   * Sets the path of the page that contains the module.
   */
  public void setPagePath(Path pagePath) {
    this.pagePath = pagePath;
  }

  /**
   * Returns the path of the module.
   */
  public Path getModulePath() {
    return modulePath;
  }

  public Path getModuleDataPath(WebSite webSite) {
    return webSite.getModuleDataPath().add(modulePath.getLastElement());
  }

  /**
   * Sets the path of the module.
   */
  public void setModulePath(Path modulePath) {
    this.modulePath = modulePath;
  }

  /**
   * Returns the date format of the module.
   */
  public String getDateFormat() {
    return dateFormat;
  }

  /**
   * Sets the date format of the module.
   */
  public void setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
  }

  /**
   * Returns the CSS style to be applied to the module.
   */
  public String getStyle() {
    return style;
  }

  /**
   * Sets the CSS style to be applied to the module.
   */
  public void setStyle(String style) {
    this.style = style;
  }

  /**
   * Returns the files to be passed to the module.
   *
   * @param allowCurrentDir if true and the argument parameter is null, the
   * files included in the same folder of the page are returned
   */
  public File[] getModuleFiles(WebSite webSite, boolean allowCurrentDir) {
    Path argPath = getModuleArgumentPath(false);

    if (argPath == null && allowCurrentDir) {
      argPath = getModuleArgumentDirectoryPath(webSite, true);
    }

    if (argPath != null && !webSite.isSystem(argPath)) {
      File moduleFile = webSite.getFile(argPath);
      File[] files = null;

      if (moduleFile.isDirectory()) {
        files = moduleFile.listFiles();
      } else if (moduleFile.exists()) {
        files = new File[1];
        files[0] = moduleFile;
      }

      return files;
    }

    return null;
  }

  /**
   * Returns the path passed as argument.
   *
   * @param allowCurrentPath if true and the argument parameter is null, the
   * page path is returned
   */
  public Path getModuleArgumentPath(boolean allowCurrentPath) {
    Path argPath = null;

    if (argument != null) {
      argPath = new Path(argument);
    } else if (allowCurrentPath) {
      argPath = pagePath;
    }

    return argPath;
  }

  /**
   * Returns the folder path to be used as argument for the module.
   *
   * @param allowCurrentPath if true and the argument parameter is null, the
   * path of the page is returned
   */
  public Path getModuleArgumentDirectoryPath(WebSite webSite,
      boolean allowCurrentPath) {
    Path argPath = getModuleArgumentPath(allowCurrentPath);
    return (argPath == null || webSite.isSystem(argPath)) ? null :
        webSite.getDirectory(argPath);
  }

  /**
   * Convenience method to get full HTML class attribute (e.g.
   * <code> class=&quot;stylename&quot;</code>. The value is searched in the
   * given advanced parameter or, alternatively, in the
   * value of the &quot;style&quot; tag attribute. If both are unavailable,
   * an empty string is returned.
   */
  public String getFullCSSAttribute(String paramName) {
    String css = getAdvancedParam(paramName, style);
    return Utils.isNullOrEmpty(css) ? "" : " class=\"" + css + "\"";
  }

  public String getCSSAttribute(String paramName) {
    return Utils.noNull(getAdvancedParam(paramName, style));
  }

  /**
   * Returns format to be used to display the date. The value is searched in the
   * given advanced parameter or, alternatively, in the
   * value of the &quot;date&quot; tag attribute. If both are unavailable,
   * null is returned.
   */
  public DateFormat getDateFormat(Locale locale, String paramName) {
    String paramValue = getAdvancedParam(paramName, dateFormat);
    DateFormat df = null;

    if (Module.DATE_NORMAL.equals(paramValue)) {
      df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
    } else if (Module.DATE_FULL.equals(paramValue)) {
      df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT,
          locale);
    }

    return df;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = PageAssembler.EMPTY.equals(title) ? null : title;
  }
}
