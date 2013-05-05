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
import java.io.Writer;
import org.meshcms.core.ModuleDescriptor;

/**
 * This tag print the title of the specified module location, if that title
 * has been set when editing the page.
 */
public class ModuleTitle extends AbstractTag {
  private String location = "";
  private String pre;
  private String post;
  
  public void setLocation(String location) {
    this.location = location;
  }
  
  public String getLocation() {
    return location;
  }
  
  public void writeTag() throws IOException {
    String title = null;
    ModuleDescriptor md = getModuleDescriptor(location, null);
    
    if (md != null) {
      title = md.getTitle();
    }
    
    if (title != null) {
      Writer w = getOut();
      
      if (pre != null) {
        w.write(pre);
      }
      
      w.write(title);
      
      if (post != null) {
        w.write(post);
      }
    }
  }
  
  public String getPre() {
    return pre;
  }
  
  public void setPre(String pre) {
    this.pre = pre;
  }
  
  public String getPost() {
    return post;
  }
  
  public void setPost(String post) {
    this.post = post;
  }
}
