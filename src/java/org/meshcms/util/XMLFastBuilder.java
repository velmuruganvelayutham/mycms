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

import java.io.Writer;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class XMLFastBuilder extends XMLTagStack {
  TransformerHandler hd;
  AttributesImpl atts;
  String nextTag;
  
  public XMLFastBuilder(Writer out, String charset, boolean xhtml) throws
      TransformerConfigurationException {
    StreamResult streamResult = new StreamResult(out);
    SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
    hd = tf.newTransformerHandler();
    Transformer serializer = hd.getTransformer();
    configureTransformer(serializer, charset, xhtml);
    hd.setResult(streamResult);
    
    try {
      hd.startDocument();
    } catch (SAXException ex) {
      manageSAXExceptions(ex);
    }
    
    atts = new AttributesImpl();
  }
  
  private void openPendingTag() {
    if (nextTag != null) {
      try {
        hd.startElement("", "", nextTag, atts);
      } catch (SAXException ex) {
        manageSAXExceptions(ex);
      }
      
      atts.clear();
      nextTag = null;
    }
  }
  
  public String getCurrentTagName() {
    return tagStack.empty() ? null : (String) tagStack.peek();
  }
  
  public XMLTagStack performOpenTag(String tagName) {
    openPendingTag();
    nextTag = tagName;
    tagStack.push(tagName);
    return this;
  }
  
  public XMLTagStack setAttribute(String name, String value) {
    atts.addAttribute("", "", name, "CDATA", value);
    return this;
  }
  
  public XMLTagStack addText(String textData) {
    openPendingTag();
    
    try {
      hd.characters(textData.toCharArray(), 0, textData.length());
    } catch (SAXException ex) {
      manageSAXExceptions(ex);
    }
    
    return this;
  }
  
  public XMLTagStack addCDATA(String textData) {
    openPendingTag();
    
    try {
      hd.startCDATA();
      addText(textData);
      hd.endCDATA();
    } catch (SAXException ex) {
      manageSAXExceptions(ex);
    }
    
    return this;
  }
  
  protected void performCloseTag() {
    openPendingTag();
    
    try {
      hd.endElement("", "", (String) tagStack.pop());
    } catch (SAXException ex) {
      manageSAXExceptions(ex);
    }
  }
  
  public void flush() {
    try {
      hd.endDocument();
    } catch (SAXException ex) {
      manageSAXExceptions(ex);
    }
  }

  private void manageSAXExceptions(SAXException ex) {
    ex.printStackTrace();
  }
}
