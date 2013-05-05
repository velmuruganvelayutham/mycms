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

import java.util.Properties;
import org.meshcms.util.Path;
import org.meshcms.util.Utils;

/**
 * Contains data about site menu customization and theme mappings.
 */
public class SiteInfo {
  /**
   * Prefix of the title codes.
   */
  public static final String TITLE = "title";

  /**
   * Prefix of the score codes.
   */
  public static final String SCORE = "score";

  /**
   * Prefix of the theme codes.
   */
  public static final String THEME = "theme";

  /**
   * Prefix of the submenu codes.
   */
  public static final String HIDESUBMENU = "hideSubmenu";

  private Properties data;
  private transient WebSite webSite;

  protected SiteInfo() {
    data = new Properties();
  }

  /**
   * Loads configuration from the config file (if found).
   *
   * @return true if the configuration has been loaded, false otherwise
   */
  public static SiteInfo load(WebSite webSite) {
    SiteInfo siteInfo = (SiteInfo)
        webSite.loadFromXML(webSite.getPropertiesFilePath());

    if (siteInfo == null) {
      siteInfo = new SiteInfo();
      siteInfo.setWebSite(webSite);
    } else {
      siteInfo.setWebSite(webSite);
    }

    return siteInfo;
  }

  /**
   * Saves the configuration to file.
   *
   * @return true if the configuration has been saved, false otherwise
   */
  public boolean store() {
    return webSite.storeToXML(this, webSite.getPropertiesFilePath());
  }

  /**
   * Returns the theme to be applied to the given path.
   */
  public String getPageTheme(Path pagePath) {
    return Utils.noNull(data.getProperty(getThemeCode(pagePath)));
  }

  /**
   * Sets the theme to be applied to the given path. If the value is null or
   * empty, the theme is removed.
   */
  public void setPageTheme(Path pagePath, String theme) {
    if (Utils.isNullOrEmpty(theme)) {
      data.remove(getThemeCode(pagePath));
    } else {
      data.setProperty(getThemeCode(pagePath), theme);
    }
  }

  /**
   * Returns the menu title for a page. If the menu configuration does not
   * contain a value for this page, the page title itself is returned.
   */
  public String getPageTitle(PageInfo pageInfo) {
    String customTitle = getPageTitle(pageInfo.getPath());

    if (Utils.isNullOrEmpty(customTitle)) {
      customTitle = pageInfo.getTitle();
    }

    return customTitle;
  }

  /**
   * Returns the menu title for the given path (null if not available).
   */
  public String getPageTitle(Path pagePath) {
    return Utils.noNull(data.getProperty(getTitleCode(pagePath)));
  }

  /**
   * Sets the menu title for the given path. If the value is null or empty,
   * the title is removed.
   */
  public void setPageTitle(Path pagePath, String title) {
    if (Utils.isNullOrEmpty(title)) {
      data.remove(getTitleCode(pagePath));
    } else {
      data.setProperty(getTitleCode(pagePath), title);
    }
  }

  /**
   * Returns the page score for the given path (0 if not available).
   */
  public int getPageScore(Path pagePath) {
    return Utils.parseInt(data.getProperty(getScoreCode(pagePath)), 0);
  }

  /**
   * Returns the page score as a string for the given path. An empty string
   * is returned if the page score is 0.
   */
  public String getPageScoreAsString(Path pagePath) {
    int score = getPageScore(pagePath);
    return score == 0 ? "" : Integer.toString(score);
  }

  /**
   * Sets the page score for the given path.
   */
  public void setPageScore(Path pagePath, String score) {
    setPageScore(pagePath, Utils.parseInt(score, 0));
  }

  /**
   * Sets the page score for the given path. If the score is 0, it is removed
   * since 0 is the default.
   */
  public void setPageScore(Path pagePath, int score) {
    if (score == 0) {
      data.remove(getScoreCode(pagePath));
    } else {
      data.setProperty(getScoreCode(pagePath), Integer.toString(score));
    }
  }

  /**
   * Returns the hide submenu for the given path (false if not available).
   */
  public boolean getHideSubmenu(Path pagePath) {
    return Utils.isTrue(data.getProperty(getHideSubmenuCode(pagePath)));
  }

  /**
   * Returns the hide submenu as a string for the given path. An empty string
   * is returned if the hide submenu is false.
   */
  public String getHideSubmenuAsString(Path pagePath) {
    boolean hide = getHideSubmenu(pagePath);
    return hide == false ? "" : Boolean.toString(hide);
  }

  /**
   * Sets the hide submenu for the given path.
   */
  public void setHideSubmenu(Path pagePath, String hide) {
    setHideSubmenu(pagePath, Utils.isTrue(hide));
  }

  /**
   * Sets the hide submenu for the given path. If the submenu is shown, it is removed
   * since false is the default.
   */
  public void setHideSubmenu(Path pagePath, boolean hide) {
    if (hide == false) {
      data.remove(getHideSubmenuCode(pagePath));
    } else {
      data.setProperty(getHideSubmenuCode(pagePath), Boolean.toString(hide));
    }
  }

  /**
   * Returns the code for the score field of the given path. This code is
   * used in the HTML configuration form and in the config file.
   */
  public static String getScoreCode(Path pagePath) {
    return SCORE + WebUtils.getMenuCode(pagePath);
  }

  /**
   * Returns the code for the title field of the given path. This code is
   * used in the HTML configuration form and in the config file.
   */
  public static String getTitleCode(Path pagePath) {
    return TITLE + WebUtils.getMenuCode(pagePath);
  }

  /**
   * Returns the code for the theme field of the given path. This code is
   * used in the HTML configuration form and in the config file.
   */
  public static String getThemeCode(Path pagePath) {
    return THEME + WebUtils.getMenuCode(pagePath);
  }

  /**
   * Returns the code for the show submenu field of the given path. This code is
   * used in the HTML configuration form and in the config file.
   */
  public static String getHideSubmenuCode(Path pagePath) {
    return HIDESUBMENU + WebUtils.getMenuCode(pagePath);
  }

  /**
   * Sets a generic value. The <code>fieldName</code> parameter is analyzed to
   * guess what value is going to be set. This is used to save all form fields.
   *
   * @return true if the fieldName has been recognized as a valid one, false
   * otherwise
   */
  public boolean setValue(String fieldName, String value) {
    if (fieldName == null) {
      return false;
    }

    if (value != null) {
      value = value.trim();
    }

    if (fieldName.startsWith(TITLE)) {
      if (Utils.isNullOrEmpty(value)) {
        data.remove(fieldName);
      } else {
        data.setProperty(fieldName, Utils.encodeHTML(value));
      }

      return true;
    }

    if (fieldName.startsWith(THEME)) {
      if (Utils.isNullOrEmpty(value)) {
        data.remove(fieldName);
      } else {
        data.setProperty(fieldName, value);
      }

      return true;
    }

    if (fieldName.startsWith(SCORE)) {
      int n = Utils.parseInt(value, 0);

      if (n == 0) {
        data.remove(fieldName);
      } else {
        data.setProperty(fieldName, value);
      }

      return true;
    }

    if (fieldName.startsWith(HIDESUBMENU)) {
        boolean b = Utils.isTrue(value);

        if (b == false) {
          data.remove(fieldName);
        } else {
          data.setProperty(fieldName, "true");
        }

        return true;
    }

    return false;
  }

  protected Path getThemePath(Path pagePath) {
    do {
      String theme = getPageTheme(pagePath);

      if (!Utils.isNullOrEmpty(theme)) {
        if (theme.equals(PageAssembler.EMPTY)) {
          return null;
        }

        Path themePath = (Path) webSite.getSiteMap().getThemesMap().get(theme);

        if (themePath != null) {
          return themePath;
        }
      }

      pagePath = pagePath.getParent();
    } while (!pagePath.isRelative());

    return null;
  }

  /**
   * Returns the path of the page whose theme is inherited.
   */
  public Path getThemeRoot(Path pagePath) {
    do {
      String theme = getPageTheme(pagePath);

      if (!Utils.isNullOrEmpty(theme)) {
        if (webSite.getSiteMap().getThemesMap().get(theme) != null) {
          return pagePath;
        }
      }

      pagePath = pagePath.getParent();
    } while (!pagePath.isRelative());

    return null;
  }

  public WebSite getWebSite() {
    return webSite;
  }

  public void setWebSite(WebSite webSite) {
    this.webSite = webSite;
  }
}
