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

import com.opensymphony.module.sitemesh.HTMLPage;
import java.io.IOException;
import java.io.Writer;
import org.meshcms.core.PageAssembler;

/**
 * This tag must include all others in a theme file. This is required to enclose
 * all other tags in an HTML form while editing the page. Usually this tag is
 * the first child of <code>&lt;body&gt;</code>:
 *
 * <pre> &lt;body&gt;&lt;cms:editor&gt;
 *     <em>... (html and other MeshCMS tags) ...</em>
 * &lt;/cms:editor&gt;&lt;/body&gt;</pre>
 *
 * <em>Important note:</em> since a form can't be enclosed in another form, you
 * must surround any other form in your theme with a &lt;cms:ifnotediting&gt;
 * tag:
 *
 * <pre> &lt;body&gt;&lt;cms:editor&gt;
 *     <em>... (html and other MeshCMS tags) ...</em>
 *         &lt;cms:ifnotediting&gt;&lt;form action='youraction'&gt;
 *             <em>... your form ...</em>
 *         &lt;/form&gt;&lt;/cms:ifnotediting&gt;
 *     <em>... (html and other MeshCMS tags) ...</em>
 * &lt;/cms:editor&gt;&lt;/body&gt;</pre>
 *
 * This way your form won't be displayed while editing.
 */
public class Editor extends AbstractTag {
  public int doEndTag() {
    if (isEdit) {
      try {
        getOut().write("</form>");
      } catch (IOException ex) {
        pageContext.getServletContext().log("Can't write", ex);
      }
    }

    return EVAL_PAGE;
  }

  public void writeTag() throws IOException {
    // nothing to do here
  }

  public void writeEditTag() throws IOException {
    Writer w = getOut();
    w.write("<form id='editor' name='editor' action=\"" +
        adminRelPath.add("savepage.jsp") + "\" method='post'>\n");

    HTMLPage htmlPage = (HTMLPage) getPage();
    String[] keys = htmlPage.getPropertyKeys();

    for (int i = 0; i < keys.length; i++) {
      if (!keys[i].equals(PageAssembler.EMAIL_PARAM) &&
          !keys[i].equals(PageAssembler.MODULES_PARAM) &&
          !keys[i].equals("title")) {
        w.write("<input type='hidden' name='" + keys[i] + "' value=\"" +
            htmlPage.getProperty(keys[i]) + "\" />\n");
      }
    }

    w.write("<input type='hidden' name='pagepath' value=\"" +
      pagePath + "\" />");
  }

  public int getStartTagReturnValue() {
    return EVAL_BODY_INCLUDE;
  }
}
