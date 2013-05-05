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

import com.opensymphony.module.sitemesh.Page;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import org.meshcms.core.WebUtils;
import org.meshcms.util.Utils;

/**
 * Writes the page title. Please note that since this tag is used within the
 * &lt;head&gt; tag, the field to edit the page title are displayed by
 * {@link PageBody}
 */
public class PageTitle extends AbstractTag {
  private String defaultTitle = "&nbsp;";

  public void setDefault(String defaultTitle) {
    this.defaultTitle = Utils.noNull(defaultTitle);
  }

  public String getDefault() {
    return defaultTitle;
  }

  public String getTitle() {
    String title = null;
    Page page = getPage();

    if (page != null) {
      title = page.getTitle();
    }

    if (Utils.isNullOrEmpty(title)) {
      title = defaultTitle;
    }

    // avoid multiple spaces in titles (they can come from jtidy for example)
    if (!Utils.isNullOrEmpty(title)) {
      title = title.replaceAll("\\s+", " ");
    }

    return title;
  }

  public void writeTag() throws IOException {
    getOut().write(getTitle());
  }

  public void writeEditTag() throws IOException {
    Locale locale = WebUtils.getPageLocale(pageContext);
    ResourceBundle bundle = ResourceBundle.getBundle("org/meshcms/webui/Locales", locale);
    MessageFormat formatter = new MessageFormat("", locale);

    Object[] args = { getTitle() };
    formatter.applyPattern(bundle.getString("editorTitle"));
    getOut().write(formatter.format(args));
  }
}
