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

/*
 * This file is nearly identical to the SessionDecoratorMapper available in the
 * SiteMesh CVS and written by Ricardo Lecheta. It has been kept out of the
 * SiteMesh package to point out that this file is not part of the current
 * SiteMesh official release.
 */
package org.meshcms.core;

import com.opensymphony.module.sitemesh.Config;
import com.opensymphony.module.sitemesh.Decorator;
import com.opensymphony.module.sitemesh.DecoratorMapper;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.mapper.AbstractDecoratorMapper;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>Will look at a request attribute to find the name of an appropriate decorator to use. If the
 * request attribute is not present, the mapper will not do anything and allow the next mapper in the chain
 * to select a decorator.</p>
 * 
 * <p>By default, it will look at the 'decorator' session attribute, however this can be overriden by
 * configuring the mapper with a 'decorator.parameter' property.</p>
 *
 * @author Ricardo Lecheta
 */
public class RequestDecoratorMapper extends AbstractDecoratorMapper {
  private String decoratorParameter = null;

  public void init(Config config, Properties properties, DecoratorMapper parent) throws InstantiationException {
    super.init(config, properties, parent);
    decoratorParameter = properties.getProperty("decorator.parameter", "decorator");
  }

  public Decorator getDecorator(HttpServletRequest request, Page page) {
    Decorator result = null;
    String decorator = (String) request.getAttribute(decoratorParameter);

    if (decorator != null) {
      result = getNamedDecorator(request, decorator);
    }
        
    return result == null ? super.getDecorator(request, page) : result;
  }
}
