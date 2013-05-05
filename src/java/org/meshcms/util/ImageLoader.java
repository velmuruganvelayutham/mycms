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

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.util.Hashtable;

/**
 * Loads an image from a file without using the javax.imageio package. That
 * package creates some performance problems with JPEG files that contain
 * color profile information. This class works fine in a headless environment.
 */
public class ImageLoader implements ImageConsumer {
  boolean completed, notStarted = true;
  Image imgImage;
  int[] imgPixels;
  int width, height;

  /**
   * Creates an image loader for the given file path.
   *
   * @param imageFilePath the image file path
   */
  public ImageLoader(String imageFilePath) {
    imgImage = Toolkit.getDefaultToolkit().getImage(imageFilePath);
  }

  /**
   * Checks if the images has loaded.
   * 
   * @return true if the image has been loaded completely.
   */
  public boolean check() {
    if (notStarted) {
      imgImage.getSource().startProduction(this);
      notStarted = false;
    }

    return completed;
  }

  /**
   * Returns the width of the image.
   */
  public int getWidth() {
    return width;
  }

  /**
   * Returns the height of the image.
   */
  public int getHeight() {
    return height;
  }

  public void setDimensions(int width, int height) {
    this.width = width;
    this.height = height;
    imgPixels = new int[width * height];
  }

  public void setProperties(Hashtable props) {
    // empty
  }

  public void setColorModel(ColorModel model) {
    // empty
  }

  public void setHints(int hintflags) {
    // empty
  }

  public void setPixels(int x, int y, int w, int h, ColorModel model,
                        byte pixels[], int off, int scansize) {
    int palette = 1;
    int sx;

    if (model instanceof IndexColorModel) {
      palette = ((IndexColorModel) model).getMapSize();
    }

    for (int i = 0; i < h; i++) {
      sx = off;

      for (int j = 0; j < w; j++) {
        int idx = pixels[sx++];

        while (idx < 0) {
          idx += palette;
        }

        imgPixels[(i + y) * width + j + x] = model.getRGB(idx % palette);
      }

      off += scansize;
    }
  }

  public void setPixels(int x, int y, int w, int h, ColorModel model,
                        int pixels[], int off, int scansize) {
    int sx;

    for (int i = 0; i < h; i++) {
      sx = off;

      for (int j = 0; j < w; j++) {
        imgPixels[(i + y) * width + j + x] = model.getRGB(pixels[sx++]);
      }

      off += scansize;
    }
  }

  public void imageComplete(int status) {
    completed = true;
    imgImage.getSource().removeConsumer(this);
    imgImage.flush();
    imgImage = null;
  }

  /**
   * Returns a new image.
   * 
   * @return the image.
   */
  public Image getImage() {
    waitForImage();

    return Toolkit.getDefaultToolkit().createImage(
      new MemoryImageSource(width, height, imgPixels, 0, width)
    );
  }

  /**
   * Returns the image as an array of ARGB pixels.
   *
   * @return an array of pixels
   */
  public int[] getPixels() {
    waitForImage();
    return imgPixels;
  }

  /**
   * Waits until the image has been loaded completely.
   */
  public void waitForImage() {
    while (!check()) {
      try {
        Thread.sleep(500L);
      } catch (InterruptedException ex) {}
    }
  }
}
