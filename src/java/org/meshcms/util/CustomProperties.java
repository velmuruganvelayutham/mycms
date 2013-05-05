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

import java.util.Properties;

/**
 * Convenience class to use java.util.Properties to store values that are not
 * strings. All additional getters require a default value, but you can safely
 * use null for non-primitive types.
 */
public class CustomProperties extends Properties {
  public int getProperty(String key, int defaultValue) {
    try {
      defaultValue = Integer.parseInt(getProperty(key));
    } catch (Exception ex) {
      //
    }
    
    return defaultValue;
  }
  
  public void setProperty(String key, int value) {
    setProperty(key, Integer.toString(value));
  }

  public boolean getProperty(String key, boolean defaultValue) {
    try {
      defaultValue = Utils.isTrue(key, true);
    } catch (Exception ex) {
      //
    }
    
    return defaultValue;
  }
  
  public void setProperty(String key, boolean value) {
    setProperty(key, Boolean.toString(value));
  }

  public String[] getProperty(String key, String[] defaultValue, char separator) {
    String value = getProperty(key);
    
    if (value != null) {
      defaultValue = Utils.tokenize(value, Character.toString(separator));
    }
    
    return defaultValue;
  }
  
  public void setProperty(String key, String[] value, char separator) {
    setProperty(key, Utils.generateList(value, Character.toString(separator)));
  }
}
