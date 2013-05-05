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
import org.meshcms.util.Utils;

/**
 * This tag allows its contents to be written only if a recipient has
 * <em>not</em> been specified for the page.
 */
public class IfNotMailForm extends AbstractTag {
  public void writeTag() throws IOException {
    // nothing to do here
  }

  public int getStartTagReturnValue() {
    return (Utils.checkAddress(getMailFormAddress()) && !isEdit) ?
        SKIP_BODY : EVAL_BODY_INCLUDE;
  }
}
