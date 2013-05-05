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
package org.meshcms.webui;

import java.text.NumberFormat;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.ProgressListener;

public class UploadProgressListener implements ProgressListener {

  public static final String UPLOAD_PROGRESS_LISTENER =
      "UPLOAD_PROGRESS_LISTENER";

  public static NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance();

  private double progress;

  private boolean completed;

  public UploadProgressListener(HttpSession session) {
    session.setAttribute(UPLOAD_PROGRESS_LISTENER, this);
    PERCENT_FORMAT.setMinimumFractionDigits(0);
    PERCENT_FORMAT.setMaximumFractionDigits(0);
  }

  public void update(long bytesRead, long contentLength, int items) {
    progress = (double) bytesRead / (double) contentLength;
    completed = bytesRead == contentLength;
  }

  public double getProgress() {
    return progress;
  }

  public String getPercent() {
    return PERCENT_FORMAT.format(progress);
  }

  public boolean isCompleted() {
    return completed;
  }
}
