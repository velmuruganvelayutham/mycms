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

import java.util.SortedMap;
import java.util.TreeMap;
import org.meshcms.util.Utils;

/**
 * Contains data about file types and extension.
 */
public final class FileTypes {
  public static final SortedMap EXT_MAP = new TreeMap();

  /**
   * Default icon for unknown file types.
   */
  public static final String DEFAULT_ICON = "page_white.png";

  /**
   * Default icon for folders.
   */
  public static final String DIR_ICON = "folder.png";

  /**
   * Denotes an unknown file type.
   */
  public static final TypeInfo UNKNOWN;

  /**
   * Denotes a directory.
   */
  public static final TypeInfo DIRECTORY;

  /**
   * Id of static HTML files.
   */
  public static final int HTML_ID = 1;

  /**
   * Id of server-side HTML files (e.g. JSPs).
   */
  public static final int SERVERSIDE_ID = 2;

  static {
    UNKNOWN = new TypeInfo();
    UNKNOWN.id = -1;
    UNKNOWN.description = "Unknown";
    UNKNOWN.compressible = false;
    UNKNOWN.iconFile = DEFAULT_ICON;
    UNKNOWN.preventHotlinking = false;

    DIRECTORY = new TypeInfo();
    DIRECTORY.id = 0;
    DIRECTORY.description = "Folder";
    DIRECTORY.compressible = false;
    DIRECTORY.iconFile = DIR_ICON;
    DIRECTORY.preventHotlinking = false;

    TypeInfo info = new TypeInfo();
    info.id = HTML_ID;
    info.description = "Static Page";
    info.compressible = true;
    info.preventHotlinking = false;
    info.iconFile = "page_white_world.png";
    EXT_MAP.put("htm", info);
    EXT_MAP.put("html", info);
    EXT_MAP.put("xhtml", info);

    info = new TypeInfo();
    info.id = SERVERSIDE_ID;
    info.description = "Server-Side Page";
    info.compressible = true;
    info.preventHotlinking = false;
    info.iconFile = "page_white_world.png";
    EXT_MAP.put("asp", info);
    EXT_MAP.put("cgi", info);
    EXT_MAP.put("jsp", info);
    EXT_MAP.put("php", info);
    EXT_MAP.put("pl", info);
    EXT_MAP.put("shtml", info);

    info = new TypeInfo();
    info.id = 3;
    info.description = "Web Image";
    info.compressible = false;
    info.preventHotlinking = true;
    info.iconFile = "page_white_camera.png";
    EXT_MAP.put("gif", info);
    EXT_MAP.put("jpeg", info);
    EXT_MAP.put("jpg", info);
    EXT_MAP.put("mng", info);
    EXT_MAP.put("png", info);

    info = new TypeInfo();
    info.id = 4;
    info.description = "Java File";
    info.compressible = true;
    info.preventHotlinking = false;
    info.iconFile = "page_white_cup.png";
    EXT_MAP.put("class", info);
    EXT_MAP.put("java", info);

    info = new TypeInfo();
    info.id = 5;
    info.description = "Java Archive";
    info.compressible = false;
    info.preventHotlinking = false;
    info.iconFile = "page_white_cup.png";
    EXT_MAP.put("ear", info);
    EXT_MAP.put("jar", info);
    EXT_MAP.put("war", info);

    info = new TypeInfo();
    info.id = 6;
    info.description = "XML File";
    info.compressible = true;
    info.preventHotlinking = false;
    info.iconFile = "page_white_code.png";
    EXT_MAP.put("dtd", info);
    EXT_MAP.put("tld", info);
    EXT_MAP.put("xml", info);
    EXT_MAP.put("xsl", info);

    info = new TypeInfo();
    info.id = 7;
    info.description = "Audio File";
    info.compressible = false;
    info.preventHotlinking = true;
    info.iconFile = "page_white_cd.png";
    EXT_MAP.put("au", info);
    EXT_MAP.put("mp3", info);
    EXT_MAP.put("ogg", info);
    EXT_MAP.put("wav", info);
    EXT_MAP.put("wma", info);

    info = new TypeInfo();
    info.id = 8;
    info.description = "Video File";
    info.compressible = false;
    info.preventHotlinking = true;
    info.iconFile = "page_white_dvd.png";
    EXT_MAP.put("avi", info);
    EXT_MAP.put("flv", info);
    EXT_MAP.put("mov", info);
    EXT_MAP.put("mpeg", info);
    EXT_MAP.put("mpg", info);
    EXT_MAP.put("wmv", info);

    info = new TypeInfo();
    info.id = 9;
    info.description = "Archive";
    info.compressible = false;
    info.preventHotlinking = true;
    info.iconFile = "page_white_compressed.png";
    EXT_MAP.put("7z", info);
    EXT_MAP.put("bz2", info);
    EXT_MAP.put("gz", info);
    EXT_MAP.put("rar", info);
    EXT_MAP.put("rpm", info);
    EXT_MAP.put("tar", info);
    EXT_MAP.put("tgz", info);
    EXT_MAP.put("z", info);
    EXT_MAP.put("zip", info);

    info = new TypeInfo();
    info.id = 10;
    info.description = "Plain Text File";
    info.compressible = true;
    info.preventHotlinking = false;
    info.iconFile = "page_white_text.png";
    EXT_MAP.put("log", info);
    EXT_MAP.put("txt", info);

    info = new TypeInfo();
    info.id = 11;
    info.description = "Style Sheet File";
    info.compressible = true;
    info.preventHotlinking = false;
    info.iconFile = "page_white_swoosh.png";
    EXT_MAP.put("css", info);

    info = new TypeInfo();
    info.id = 12;
    info.description = "Script File";
    info.compressible = true;
    info.preventHotlinking = false;
    info.iconFile = "page_white_lightning.png";
    EXT_MAP.put("js", info);

    info = new TypeInfo();
    info.id = 13;
    info.description = "Executable File";
    info.compressible = false;
    info.preventHotlinking = true;
    info.iconFile = "page_white_gear.png";
    EXT_MAP.put("bin", info);
    EXT_MAP.put("exe", info);

    info = new TypeInfo();
    info.id = 14;
    info.description = "Word Document";
    info.compressible = true;
    info.preventHotlinking = true;
    info.iconFile = "page_white_word.png";
    EXT_MAP.put("doc", info);
    EXT_MAP.put("rtf", info);

    info = new TypeInfo();
    info.id = 15;
    info.description = "Excel Document";
    info.compressible = true;
    info.preventHotlinking = true;
    info.iconFile = "page_white_excel.png";
    EXT_MAP.put("xls", info);

    info = new TypeInfo();
    info.id = 16;
    info.description = "PowerPoint Document";
    info.compressible = true;
    info.preventHotlinking = true;
    info.iconFile = "page_white_powerpoint.png";
    EXT_MAP.put("pps", info);
    EXT_MAP.put("ppt", info);

    info = new TypeInfo();
    info.id = 17;
    info.description = "PDF Document";
    info.compressible = false;
    info.preventHotlinking = true;
    info.iconFile = "page_white_acrobat.png";
    EXT_MAP.put("pdf", info);

    info = new TypeInfo();
    info.id = 18;
    info.description = "Image";
    info.compressible = false;
    info.preventHotlinking = true;
    info.iconFile = "page_white_paint.png";
    EXT_MAP.put("bmp", info);
    EXT_MAP.put("psd", info);
    EXT_MAP.put("tga", info);
    EXT_MAP.put("tif", info);
    EXT_MAP.put("tiff", info);

    info = new TypeInfo();
    info.id = 19;
    info.description = "Icon";
    info.compressible = true;
    info.preventHotlinking = false;
    info.iconFile = "page_white_picture.png";
    EXT_MAP.put("ico", info);

    info = new TypeInfo();
    info.id = 20;
    info.description = "Flash File";
    info.compressible = false;
    info.preventHotlinking = true;
    info.iconFile = "page_white_flash.png";
    EXT_MAP.put("swf", info);

    info = new TypeInfo();
    info.id = 21;
    info.description = "Vector Image File";
    info.compressible = true;
    info.preventHotlinking = true;
    info.iconFile = "page_white_vector.png";
    EXT_MAP.put("svg", info);
  }

  static TypeInfo getInfo(String extension) {
    TypeInfo info = (TypeInfo) EXT_MAP.get(extension);
    return (info == null) ? UNKNOWN : info;
  }

  /**
   * Returns true if the type of the file is the same as the given extension.
   * For example, isLike("button.gif", "jpg") returns true
   * since gif and jpg are both images.
   */
  public static boolean isLike(String fileName, String extension) {
    return getInfo(Utils.getExtension(fileName, false)).id == getInfo(extension).id;
  }

  /**
   * Returns the description of the type of the file.
   */
  public static String getDescription(String fileName) {
    return getInfo(Utils.getExtension(fileName, false)).description;
  }

  /**
   * Returns the name of the icon file for the type of the given file.
   */
  public static String getIconFile(String fileName) {
    return getInfo(Utils.getExtension(fileName, false)).iconFile;
  }

  /**
   * Returns true if the file is supposed to be compressible. For example, text
   * files are compressible, while ZIP files are not.
   */
  public static boolean isCompressible(String fileName) {
    return getInfo(Utils.getExtension(fileName, false)).compressible;
  }

  /**
   * Returns true if the file should be referred from a page to be accessed.
   */
  public static boolean isPreventHotlinking(String fileName) {
    return getInfo(Utils.getExtension(fileName, false)).preventHotlinking;
  }

  /**
   * Returns true if the file is a page (static or server-side).
   */
  public static boolean isPage(String fileName) {
    int id = getInfo(Utils.getExtension(fileName, false)).id;
    return id == HTML_ID || id == SERVERSIDE_ID;
  }

  public static class TypeInfo {
    int id;
    String description;
    String iconFile;
    boolean compressible;
    boolean preventHotlinking;
  }
}
