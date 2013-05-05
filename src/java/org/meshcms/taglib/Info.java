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
import org.meshcms.core.Configuration;
import org.meshcms.core.WebSite;
import org.meshcms.core.WebUtils;
import org.meshcms.util.Utils;

/**
 * Writes some site and system data.
 */
public final class Info extends AbstractTag {
  public void writeTag() throws IOException {
    Configuration c = webSite.getConfiguration();
    String result = null;

    if (id != null) {
      id = id.toLowerCase();

      if (id.equals("host") || id.equals("domain")) {
        result = c.getSiteHost();
      } else if (id.equals("description")) {
        result = c.getSiteDescription();
      } else if (id.equals("keywords")) {
        result = c.getSiteKeywords();
      } else if (id.equals("author")) {
        result = c.getSiteAuthor();
      } else if (id.equals("authorurl")) {
        result = c.getSiteAuthorURL();
      } else if (id.equals("meshcms")) {
        result = WebSite.APP_NAME + " " + WebSite.VERSION_ID;
      } else if (id.equals("charset")) {
        result = Utils.SYSTEM_CHARSET;
      } else if (id.equals("lang")) {
        result = WebUtils.getPageLocale(pageContext).toString();
      }
    }

    if (result == null) {
      result = Utils.encodeHTML(Utils.noNull(c.getSiteName()));
    }

    getOut().write(result);
  }
}
