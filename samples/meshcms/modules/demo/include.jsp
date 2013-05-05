<%--
 Copyright 2004-2009 Luciano Vernaschi
 
 This file is part of MeshCMS.
 
 MeshCMS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 MeshCMS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with MeshCMS.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ page import="java.io.*" %>
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.meshcms.core.*" %>

<%-- get the website instance --%>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />

<%--
  Advanced parameters for this module:
  - css = (name of a css class)
  - date = none (default) | normal | full
  - message = (an optional message, just to show how to use advanced parameters)
--%>

<%
  /* get the module descriptor (contains all the info we need about this module */
  String moduleCode = request.getParameter("modulecode");
  ModuleDescriptor md = null;

  if (moduleCode != null) {
    md = (ModuleDescriptor) request.getAttribute(moduleCode);
  }

  /* if md is null, this module has not been called correctly */
  if (md == null) {
    /* send an error if possible (maybe the module page has been called directly) */
    if (!response.isCommitted()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    return;
  }

  out.println("<div" + md.getFullCSSAttribute("css") + ">");
  out.println("<div style='border: 1px solid silver; padding: 5px;'>");

  if (md.getArgument() == null) {
    out.println("  <p>No argument has been passed, will use the directory that contains the page</p>");
  }

  /* get the files to be processed. Note that the argument is not required to
     be a path (it can be any string), so getModuleFiles can return null */
  File[] files = md.getModuleFiles(webSite, true);

  /* display the list of files into a div that uses the style if it is provided */
  out.println("<p>");

  if (files != null && files.length > 0) {
    Arrays.sort(files);
    DateFormat df = md.getDateFormat(WebUtils.getPageLocale(pageContext), "date");
    out.println("  <div>List of files:</div>\n<ul>");

    for (int i = 0; i < files.length; i++) {
      /* update the last modified time for the page */
      WebUtils.updateLastModifiedTime(request, files[i]);
      out.print("  <li>" + files[i].getName());

      if (df != null) {
        /* the module was requested to show the dates */
        out.print(" (" + df.format(new Date(files[i].lastModified())) + ")");
      }

      out.println("</li>");
    }

    out.println("</ul>");
  } else {
    out.println("  <div><em>no files in &quot;/" +
        md.getModuleArgumentDirectoryPath(webSite, true) + "&quot;</em></div>");
  }

  out.println("</p>");

  /* if a message has been provided, show it */
  String msg = md.getAdvancedParam("message", null);

  if (msg != null) {
    out.println("  <p>Custom message (passed by an advanced parameter): &quot;" +
        msg + "&quot;</p>");
  }



  out.println("</div>");
  out.println("</div>");
%>
