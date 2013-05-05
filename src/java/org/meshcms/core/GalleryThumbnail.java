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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;

public class GalleryThumbnail extends AbstractThumbnail {
  /**
   * Width and height of the thumbnail.
   */
  public static final int THUMB_SIZE = 96;

  /**
   * Width and height of the image in the thumbnail.
   */
  public static final int THUMB_IMAGE_SIZE = 94;

  private boolean highQuality;

  public String getSuggestedFileName() {
    return highQuality ? "meshcms_hq_gallery.jpg" : "meshcms_gallery.jpg";
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

    BufferedImage thumb = new BufferedImage(THUMB_SIZE, THUMB_SIZE,
      BufferedImage.TYPE_INT_RGB);
    Graphics2D g = (Graphics2D) thumb.getGraphics();
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, THUMB_SIZE, THUMB_SIZE);

    int w = image.getWidth();
    int h = image.getHeight();
    int ix, iy, w0, h0;
    ix = iy = (THUMB_SIZE - THUMB_IMAGE_SIZE) / 2;

    if (w <= THUMB_IMAGE_SIZE && h <= THUMB_IMAGE_SIZE) {
      w0 = w;
      h0 = h;
      ix += (THUMB_IMAGE_SIZE - w0) / 2;
      iy += (THUMB_IMAGE_SIZE - h0) / 2;
    } else if (w > h) {
      w0 = THUMB_IMAGE_SIZE;
      h0 = w0 * h / w;
      iy += (THUMB_IMAGE_SIZE - h0) / 2;
    } else {
      h0 = THUMB_IMAGE_SIZE;
      w0 = h0 * w / h;
      ix += (THUMB_IMAGE_SIZE - w0) / 2;
    }

    w0 = Math.max(w0, 1);
    h0 = Math.max(h0, 1);

    AbstractThumbnail.drawResizedImage(g, image, ix, iy, w0, h0, highQuality);
    image.flush();
    g.setColor(DEFAULT_BORDER_COLOR);
    g.drawRect(0, 0, THUMB_SIZE - 1, THUMB_SIZE - 1);

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
