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
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * Creates a thumbnail by simply resizing the image. The way the thumbnail is
 * created can be controlled with some parameters (see the various setters
 * for details).
 */
public class ResizedThumbnail extends AbstractThumbnail {
  /**
   * Used to scale the image maintaining proportions.
   */
  public static final String MODE_SCALE = "scale";

  /**
   * Used to scale the image and crop it to have a thumbnail of the required size.
   */
  public static final String MODE_CROP = "crop";

  /**
   * Like {@link #MODE_SCALE}, but adds a padding to reach the required size.
   */
  public static final String MODE_PADDING = "padding";

  /**
   * Used to resize the image without maintaining proportions.
   */
  public static final String MODE_STRETCH = "stretch";

  /**
   * Default padding color.
   */
  public static final String WHITE = "ffffff";

  /**
   * Default thumbnail size.
   */
  public static final int DEFAULT_SIZE = 100;

  private static Matcher hexColorMatcher =
      Pattern.compile("[ABCDEFabcdef\\d]{6}").matcher("");

  private int width = -1;
  private int height = -1;
  private boolean highQuality = false;
  private String mode = MODE_SCALE;
  private String color = WHITE;

  public String getSuggestedFileName() {
    return "resized" + ("" + width + height + highQuality + mode +
        color).hashCode() + ".jpg";
  }

  protected boolean createThumbnail(File imageFile, File thumbnailFile) {
    BufferedImage image;

    try {
      image = ImageIO.read(imageFile);
    } catch (Exception ex) {
      return false;
    }

    if (image == null) {
      return false;
    }

    int w = image.getWidth();
    int h = image.getHeight();

    if (w <= 0 || h <= 0) {
      return false;
    }

    int reqW = width;
    int reqH = height;
    int w0 = w;
    int h0 = h;

    if (reqW < 1 && reqH < 1) {
      reqW = reqH = DEFAULT_SIZE;
    } else if (reqW < 1) {
      reqW = w * reqH / h;
    } else if (reqH < 1) {
      reqH = h * reqW / w;
    }

    if (reqW > w && reqH > h) {
      reqW = w;
      reqH = h;
    }

    if (mode.equals(MODE_CROP)) {
      if (w * reqH < h * reqW) {
        w0 = reqW;
        h0 = w0 * h / w;
      } else {
        h0 = reqH;
        w0 = w * h0 / h;
      }
    } else if (mode.equals(MODE_STRETCH)) {
      w0 = reqW;
      h0 = reqH;
    } else {
      if (w * reqH < h * reqW) {
        h0 = reqH;
        w0 = w * h0 / h;
      } else {
        w0 = reqW;
        h0 = w0 * h / w;
      }

      if (!mode.equals(MODE_PADDING)) {
        reqW = w0;
        reqH = h0;
      }
    }

    BufferedImage thumb = new BufferedImage(reqW, reqH, BufferedImage.TYPE_INT_RGB);
    Graphics g = thumb.getGraphics();
    g.setColor(new Color(Integer.parseInt(color, 16)));
    g.fillRect(0, 0, reqW, reqH);
    AbstractThumbnail.drawResizedImage(g, image, (reqW - w0) / 2,
        (reqH - h0) / 2, w0, h0, highQuality);
    image.flush();

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

  public int getWidth() {
    return width;
  }

  /**
   * Sets the maximum image width.
   */
  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  /**
   * Sets the maximum image height.
   */
  public void setHeight(int height) {
    this.height = height;
  }

  public String getMode() {
    return mode;
  }

  /**
   * Sets the scaling mode. Possible values are {@link #MODE_SCALE} (default),
   * {@link #MODE_CROP}, {@link #MODE_PADDING} and {@link #MODE_STRETCH}.
   */
  public void setMode(String mode) {
    if (mode == null) {
      this.mode = MODE_SCALE;
    } else {
      mode = mode.trim().toLowerCase();

      if (mode.equals(MODE_CROP) || mode.equals(MODE_PADDING) ||
          mode.equals(MODE_SCALE) || mode.equals(MODE_STRETCH)) {
        this.mode = mode;
      } else {
        throw new IllegalArgumentException("Unknown mode: " + mode);
      }
    }
  }

  public String getColor() {
    return color;
  }

  /**
   * Sets the padding color (used only when mode is {@link #MODE_PADDING}).
   * The color must be supplied in hexadecimal format, with or without a #
   * sign (e.g. #ffcc00 or 123ABC).
   */
  public void setColor(String color) {
    if (color == null) {
      this.color = WHITE;
    } else {
      if (hexColorMatcher.reset(color).find()) {
        this.color = hexColorMatcher.group();
      } else {
        throw new IllegalArgumentException("Unknown color: " + color);
      }
    }
  }
}
