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

import java.util.Stack;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

public abstract class XMLTagStack {
  protected Stack tagStack;
  protected StringBuffer textBuffer;
  protected boolean newTag;
  
  public XMLTagStack() {
    tagStack = new Stack();
  }
  
  public abstract XMLTagStack addCDATA(String textData);
  
  public abstract XMLTagStack addText(String textData);
  
  public abstract String getCurrentTagName();
  
  public abstract XMLTagStack performOpenTag(String tagName);
  
  public abstract XMLTagStack setAttribute(String name, String value);
  
  protected abstract void performCloseTag();
  
  public XMLTagStack openTag(String tagName) {
    newTag = true;
    return performOpenTag(tagName);
  }
  
  public XMLTagStack closeTag() {
    return closeTag(null);
  }
  
  public XMLTagStack closeTag(String tagName) {
    if (tagStack.empty()) {
      throw new IllegalStateException("No tag to close");
    }
    
    if (tagName != null) {
      String currentTagName = getCurrentTagName();
      
      if (!currentTagName.equals(tagName)) {
        throw new IllegalStateException("Current tag is " + currentTagName +
            ", not " + tagName);
      }
    }
    
    newTag = false;
    performCloseTag();
    return this;
  }
  
  public boolean isNewTag() {
    return newTag;
  }
  
  public StringBuffer openTextBuffer() {
    if (textBuffer != null) {
      throw new IllegalStateException("Another CDATA or text buffer seems to be open");
    }
    
    textBuffer = new StringBuffer();
    return textBuffer;
  }
  
  public XMLTagStack endTextBuffer() {
    if (textBuffer == null) {
      throw new IllegalStateException("Text buffer not opened");
    }
    
    addText(textBuffer.toString());
    textBuffer = null;
    return this;
  }
  
  public StringBuffer openCDATABuffer() {
    if (textBuffer != null) {
      throw new IllegalStateException("Another CDATA or text buffer seems to be open");
    }
    
    textBuffer = new StringBuffer();
    return textBuffer;
  }
  
  public XMLTagStack endCDATABuffer() {
    if (textBuffer == null) {
      throw new IllegalStateException("CDATA buffer not opened");
    }
    
    addCDATA(textBuffer.toString());
    textBuffer = null;
    return this;
  }
  
  public static void configureTransformer(Transformer t, String charset,
      boolean xhtml) {
    t.setOutputProperty(OutputKeys.METHOD, "xml");
    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    t.setOutputProperty(OutputKeys.INDENT, "yes");
    
    if (xhtml) {
      t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,
          "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
    }
    
    if (charset != null) {
      t.setOutputProperty(OutputKeys.ENCODING, charset);
    }
  }
}
