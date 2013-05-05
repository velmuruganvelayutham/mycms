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
 * This tag allows its contents to be written only when the current page is
 * being edited.
 */
public class IfNotIndexed extends AbstractTag {
  public void writeTag() throws IOException {
    // nothing to do here
  }
  
  public int getStartTagReturnValue() {
    return webSite.getSiteMap().getPageInfo(pagePath) == null ?
      EVAL_BODY_INCLUDE : SKIP_BODY;
  }
}
