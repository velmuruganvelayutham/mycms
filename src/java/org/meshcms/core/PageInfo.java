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

import org.meshcms.util.Path;

/**
 * This class describes a web page with all the related info (path, title, hits
 * and so on).
 */
public final class PageInfo {
  public static final String ARTICLE_IMAGE_SUFFIX = "_image.jpg";
  private Path path;
  private String title;
  private int[] stats;
  private int statSum;
  private int lastStatsIndex;
  private WebSite webSite;
  private long lastModified;
  private String charset;
  private String[] keywords;
  private String excerpt;

  /**
   * Creates a page info in the specified {@link WebSite} to describe the page
   * available at the specified path.
   */
  public PageInfo(WebSite webSite, Path path) {
    this.webSite = webSite;
    this.path = path;
    stats = new int[webSite.getStatsLength()];
  }

  /**
   * Returns the path of the page.
   */
  public Path getPath() {
    return path;
  }

  /**
   * Sets the title of the page.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Returns the title of the page.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns the file name of the page.
   */
  public String getName() {
    return path.getLastElement();
  }

  /**
   * Adds a hit to the count.
   */
  public synchronized void addHit() {
    stats[getIndex()]++;
    statSum++;
  }

  /**
   * Returns the hit count for the last day.
   */
  public synchronized int getHits() {
    return stats[getIndex()];
  }

  /**
   * Returns the hit count for a previous day.
   */
  public synchronized int getHits(int daysBefore) {
    int index = getIndex() - daysBefore;
    return stats[index < 0 ? index + stats.length : index];
  }

  /**
   * Returns the total hit count.
   */
  public synchronized int getTotalHits() {
    getIndex(); // to update index before sum
    return statSum;
  }

  protected synchronized int[] getStats() {
    return stats;
  }

  protected synchronized int getLastStatsIndex() {
    return lastStatsIndex;
  }

  protected synchronized void copyStatsFrom(PageInfo other) {
    getIndex(); // to update index before copying
    statSum = other.getTotalHits(); // this one calls other.getIndex()
    stats = other.getStats();
    lastStatsIndex = other.getLastStatsIndex();
  }

  private synchronized int getIndex() {
    int index = webSite.getStatsIndex();

    if (index != lastStatsIndex) {
      lastStatsIndex = index;
      statSum -= stats[index];
      stats[index] = 0;
    }

    return index;
  }

  /**
   * Returns the depth level of the page.
   */
  public int getLevel() {
    return path.getElementCount();
  }

  /**
   * Returns the title of the page (same as {@link #getTitle}).
   */
  public String toString() {
    return title;
  }

  /**
   * Returns the time of the last modification made to the page.
   */
  public long getLastModified() {
    return lastModified;
  }

  /**
   * Sets the time of the last modification made to the page. This value
   * should be set equal to the value of <code>java.io.File.lastModified()</code>.
   */
  public void setLastModified(long lastModified) {
    this.lastModified = lastModified;
  }

  public String getCharset() {
    return charset;
  }

  public void setCharset(String charset) {
    this.charset = charset;
  }

  public String[] getKeywords() {
    return keywords;
  }

  public void setKeywords(String[] keywords) {
    this.keywords = keywords;
  }

  /**
   * Return an excerpt of the page content
   * @return the excerpt
   */
  public String getExcerpt() {
    return excerpt;
  }

  /**
   * Set an excerpt of the page content
   * @param excerpt the excerpt to set
   */
  public void setExcerpt(String excerpt) {
    this.excerpt = excerpt;
  }
}
