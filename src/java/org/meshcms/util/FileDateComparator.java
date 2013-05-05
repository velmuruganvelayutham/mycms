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

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Sorts files by date (newest files first).
 *
 * @author Luciano Vernaschi
 */
public final class FileDateComparator implements Comparator, Serializable {

  private final boolean forwards;

  public FileDateComparator() {
    forwards = true;
  }

  public FileDateComparator(boolean forwards) {
    this.forwards = forwards;
  }

  public int compare(Object o1, Object o2) {
    try {
      long f1 = ((File) o1).lastModified();
      long f2 = ((File) o2).lastModified();
      if (forwards) {
        if (f1 > f2) {
          return -1;
        } else if (f1 < f2) {
          return 1;
        }
      } else {
        if (f2 > f1) {
          return -1;
        } else if (f2 < f1) {
          return 1;
        }
      }
    } catch (ClassCastException ex) {}
    return 0;
  }
}
