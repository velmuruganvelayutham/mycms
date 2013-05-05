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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FileSearch extends DirectoryParser {
  private Pattern regex;
  private List results;

  public FileSearch(File dir, String regex) {
    this.regex = Pattern.compile(regex);
    setInitialDir(dir);
    setRecursive(true);
    setSorted(true);
    results = new ArrayList();
  }

  protected void processFile(File file, Path path) {
    if (regex.matcher(file.getName()).matches()) {
      results.add(path);
    }
  }
  
  public Path[] getResults() {
    return (Path[]) results.toArray(new Path[results.size()]);
  }
}
