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

/**
 * Allows to sort in reversed ordering. If a Comparator is not supplied, the
 * order will be the reversed natural ordering.
 */
public class ReverseComparator implements Comparator, Serializable {
  private Comparator comparator;

  /**
   * Creates a new instance of ReverseComparator without specifying a base
   * Comparator
   */
  public ReverseComparator() {
  }

  /**
   * Creates a new instance of ReverseComparator to reverse the specified
   * Comparator
   */
  public ReverseComparator(Comparator comparator) {
    this.comparator = comparator;
  }

  public int compare(Object o1, Object o2) {
    if (comparator == null) {
      return ((Comparable) o2).compareTo(o1);
    } else {
      return comparator.compare(o2, o1);
    }
  }
}
