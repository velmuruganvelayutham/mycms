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
import java.util.Locale;
import javax.servlet.jsp.PageContext;
import org.meshcms.core.HitFilter;
import org.meshcms.util.Utils;

/**
 * Writes the context path as returned by
 * <code>HttpServletRequest.getContextPath()</code>.
 */
public final class SetLocale extends AbstractTag {
  private String value;
  private String defaultValue;
  private String redirectRoot;

  public void writeTag() throws IOException {
    /* if (Utils.isTrue(redirectRoot) &&
        webSite.getSiteMap().getPathInMenu(pagePath).isRoot()) {
      if (setRedirectToLanguage(request,
          (HttpServletResponse) pageContext.getResponse())) {
        return;
      }
    } */

    Locale locale = null;

    if (value != null) {
      locale = Utils.getLocale(value);
    } else if (!pagePath.isRoot()) {
      for (int i = pagePath.getElementCount() - 1; locale == null && i >= 0; i--) {
        locale = Utils.getLocale(pagePath.getElementAt(i));
      }
    }

    if (locale == null) {
      locale = Utils.getLocale(defaultValue);
    }

    if (locale != null) {
      pageContext.setAttribute(HitFilter.LOCALE_ATTRIBUTE, locale,
          PageContext.REQUEST_SCOPE);
    }
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }
}
