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

/**
 * Writes some user data (username, e-mail or user details).
 *
 * @see org.meshcms.core.UserInfo
 */
public final class User extends AbstractTag {
  public void writeTag() throws IOException {
    String result = null;

    if (id != null) {
      id = id.toLowerCase();

      if (id.equals("username")) {
        result = userInfo.getUsername();
      } else if (id.equals("email") || id.equals("e-mail")) {
        result = userInfo.getEmail();
      } else {
        result = userInfo.getValue(id);
      }
    }

    if (result == null) {
      result = userInfo.getDisplayName();
    }

    getOut().write(result);
  }
}
