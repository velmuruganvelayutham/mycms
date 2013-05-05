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

package org.meshcms.util;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

/**
 * Allows to save instances of {@link Path} using XStream.
 */
public class XStreamPathConverter extends AbstractSingleValueConverter {
  private boolean prependSlash;

  public Object fromString(String string) {
    return new Path(string);
  }

  public boolean canConvert(Class aClass) {
    return aClass.equals(Path.class);
  }

  public String toString(Object obj) {
    return prependSlash ? ((Path) obj).getAsLink() : obj.toString();
  }

  /**
   * Returns the current type of string (with or without prepended slash).
   */
  public boolean isPrependSlash() {
    return prependSlash;
  }

  /**
   * Defines the type of string that will be used to save (with or without
   * prepended slash).
   *
   * @param prependSlash if to prepend or not the slah
   */
  public void setPrependSlash(boolean prependSlash) {
    this.prependSlash = prependSlash;
  }
}
