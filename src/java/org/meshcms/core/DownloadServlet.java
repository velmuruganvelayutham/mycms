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

package org.meshcms.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.meshcms.util.Path;
import org.meshcms.util.Utils;

/**
 * A simple servlet to allow to download a file regardless of its type.
 */
public final class DownloadServlet extends HttpServlet {
  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   *
   * @param request servlet request
   * @param response servlet response
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    WebSite webSite = (WebSite) request.getAttribute("webSite");
    Path path = new Path(request.getPathInfo());

    if (webSite.isSystem(path)) {
      /* Only authenticated users can download system files */
      UserInfo userInfo = (UserInfo)
        request.getSession(true).getAttribute("userInfo");

      if (userInfo == null || userInfo.isGuest()) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
    }

    File file = webSite.getFile(path);

    if (!file.exists()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND,
          "File not found on server");
      return;
    }

    String fileName = request.getParameter("filename");

    if (Utils.isNullOrEmpty(fileName)) {
      fileName = path.getLastElement();
    }

    try {
      InputStream is = new FileInputStream(file);
      response.setContentType("application/x-download");
      response.setHeader("Content-Disposition", "attachment; filename=\"" +
          fileName + "\"");
      response.setHeader("Content-Length", Long.toString(file.length()));
      Utils.copyStream(is, response.getOutputStream(), false);
    } catch (IOException ex) {
      if (!response.isCommitted()) {
        response.resetBuffer();
        response.sendError(HttpServletResponse.SC_NOT_FOUND,
            "File not found on server");
      }
    }
  }
}
