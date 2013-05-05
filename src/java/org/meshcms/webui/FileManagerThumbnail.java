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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import org.meshcms.core.AbstractThumbnail;

public class FileManagerThumbnail extends AbstractThumbnail {
  /**
   * Width of the thumbnail.
   */
  public static final int THUMB_WIDTH = 108;

  /**
   * Height of the thumbnail.
   */
  public static final int THUMB_HEIGHT = 120;

  /**
   * Width and height of the image in the thumbnail.
   */
  public static final int THUMB_SIZE = 100;

  private boolean highQuality;

  public String getSuggestedFileName() {
    return highQuality ? "filemanager_hq.jpg" : "filemanager.jpg";
  }

  protected boolean createThumbnail(File imageFile, File thumbnailFile) {
    BufferedImage image;

    try {
      image = ImageIO.read(imageFile);
    } catch (Exception ex) {
      return false;
    }

    if (image == null || image.getWidth() < 1) {
      return false;
    }

    BufferedImage thumb = new BufferedImage(THUMB_WIDTH, THUMB_HEIGHT,
      BufferedImage.TYPE_INT_RGB);
    Graphics2D g = (Graphics2D) thumb.getGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                       RenderingHints.VALUE_ANTIALIAS_ON);

    g.setColor(Color.WHITE);
    g.fillRect(0, 0, THUMB_WIDTH, THUMB_HEIGHT);
    g.setColor(DEFAULT_BORDER_COLOR);
    g.drawRect(0, 0, THUMB_WIDTH - 1, THUMB_WIDTH - 1);
    g.fillRect(0, THUMB_WIDTH, THUMB_WIDTH, THUMB_HEIGHT - THUMB_WIDTH);

    int w = image.getWidth();
    int h = image.getHeight();
    int kb = (int) (imageFile.length() / 1024L) + 1;
    String label = w + "x" + h + ", " + kb + "KB";

    g.setColor(Color.BLACK);
    int fontSize = 12;
    int labelSize;
    Font font;
    FontMetrics fm;

    do {
      font = new Font("sansserif", Font.PLAIN, --fontSize);
      fm = g.getFontMetrics(font);
      labelSize = fm.stringWidth(label);
    } while (labelSize > THUMB_SIZE);

    g.setFont(font);

    int ix, iy, w0, h0;
    ix = iy = (THUMB_WIDTH - THUMB_SIZE) / 2;

    if (w <= THUMB_SIZE && h <= THUMB_SIZE) {
      w0 = w;
      h0 = h;
      ix += (THUMB_SIZE - w0) / 2;
      iy += (THUMB_SIZE - h0) / 2;
    } else if (w > h) {
      w0 = THUMB_SIZE;
      h0 = w0 * h / w;
      iy += (THUMB_SIZE - h0) / 2;
    } else {
      h0 = THUMB_SIZE;
      w0 = h0 * w / h;
      ix += (THUMB_SIZE - w0) / 2;
    }

    w0 = Math.max(w0, 1);
    h0 = Math.max(h0, 1);

    AbstractThumbnail.drawResizedImage(g, image, ix, iy, w0, h0, highQuality);
    image.flush();

    ix = (THUMB_WIDTH - labelSize) / 2;
    iy = THUMB_HEIGHT - fm.getDescent();
    g.drawString(label, ix, iy);

    OutputStream os = null;

    try {
      os = new BufferedOutputStream(new FileOutputStream(thumbnailFile));
      ImageIO.write(thumb, "jpeg", os);
    } catch (IOException ex) {
      ex.printStackTrace();
      return false;
    } finally {
      thumb.flush();
      g.dispose();

      if (os != null) {
        try {
          os.close();
        } catch (IOException ex) {}
      }
    }

    return true;
  }

  /**
   * Returns the quality setting.
   */
  public boolean isHighQuality() {
    return highQuality;
  }

  /**
   * Enables or disables better quality for image resizing.
   */
  public void setHighQuality(boolean highQuality) {
    this.highQuality = highQuality;
  }
}
