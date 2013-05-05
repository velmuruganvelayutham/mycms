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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Locale;

/**
 * Compares locales to sort them according to their display name.
 */
public class LocaleComparator implements Comparator, Serializable {
  private Locale locale;

  /**
   * Creates a new instance using the system default locale to get display names.
   */
  public LocaleComparator() {
    locale = null;
  }

  /**
   * Creates a new instance using the given locale to get display names.
   */
  public LocaleComparator(Locale locale) {
    setLocale(locale);
  }

  /**
   * Sets the locale used to get the display names, so one can sort the locales
   * according to a specific language.
   */
  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  /**
   * Returns the locale used to get the display names.
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Compares two locales.
   */
  public int compare(Object o1, Object o2) {
    try {
      Locale l1 = (Locale) o1;
      Locale l2 = (Locale) o2;

      if (locale == null) {
        return l1.getDisplayName().compareTo(l2.getDisplayName());
      } else {
        return l1.getDisplayName(locale).compareTo(l2.getDisplayName(locale));
      }
    } catch (Exception ex) {}

    return 0;
  }
}
