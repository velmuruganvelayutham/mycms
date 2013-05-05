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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XHTMLBuilder {
  Document xmlDocument;
  Element headElement;
  Element titleElement;
  Element bodyElement;
  Fragment headFragment;
  Fragment bodyFragment;
  
  public XHTMLBuilder() {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      DOMImplementation impl = builder.getDOMImplementation();
      xmlDocument = impl.createDocument(null, "html", null);
      Node htmlNode = xmlDocument.getFirstChild();
      headElement = xmlDocument.createElement("head");
      titleElement = xmlDocument.createElement("title");
      bodyElement = xmlDocument.createElement("body");
      titleElement.appendChild(xmlDocument.createTextNode("Generated XHTML Code"));
      headElement.appendChild(titleElement);
      htmlNode.appendChild(headElement);
      htmlNode.appendChild(bodyElement);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    headFragment = new Fragment(headElement);
    bodyFragment = new Fragment(bodyElement);
  }
  
  public Fragment getHead() {
    return headFragment;
  }
  
  public Fragment getBody() {
    return bodyFragment;
  }
  
  public Element getHeadElement() {
    return headElement;
  }
  
  public Element getTitleElement() {
    return titleElement;
  }
  
  public Element getBodyElement() {
    return bodyElement;
  }
  
  public void setTitle(String title) {
    if (title == null) {
      throw new IllegalArgumentException("Provided title is null");
    }
    
    Node node;
    
    while((node = titleElement.getLastChild()) != null) {
      titleElement.removeChild(node);
    }
    
    titleElement.appendChild(xmlDocument.createTextNode(title));
  }
  
  public void normalize() {
    xmlDocument.normalize();
  }
  
  public void writeFullDocument(Writer out, String charset) {
    write(xmlDocument, out, charset);
  }
  
  public void writeBodyContent(Writer out, String charset) {
    Node n = bodyElement.getFirstChild();
    
    while (n != null) {
      write(n, out, charset);
      n = n.getNextSibling();
    }
  }
  
  private void write(Node node, Writer out, String charset) {
    try {
      DOMSource domSource = new DOMSource(node);
      StreamResult streamResult = new StreamResult(out);
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer serializer = tf.newTransformer();
      XMLTagStack.configureTransformer(serializer, charset, true);
      serializer.transform(domSource, streamResult);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  public class Fragment extends XMLTagStack {
    Element mainElement;
    
    public Fragment(Element mainElement) {
      this.mainElement = mainElement;
    }
    
    public Element getCurrentTag() {
      return tagStack.empty() ? mainElement : (Element) tagStack.peek();
    }
    
    public String getCurrentTagName() {
      return tagStack.empty() ? null : ((Element) tagStack.peek()).getTagName();
    }
    
    public XMLTagStack performOpenTag(String tagName) {
      Element tag = xmlDocument.createElement(tagName);
      getCurrentTag().appendChild(tag);
      tagStack.push(tag);
      return this;
    }
    
    public XMLTagStack setAttribute(String name, String value) {
      getCurrentTag().setAttribute(name, value);
      return this;
    }
    
    public XMLTagStack addText(String textData) {
      getCurrentTag().appendChild(xmlDocument.createTextNode(textData));
      return this;
    }
    
    public XMLTagStack addCDATA(String textData) {
      getCurrentTag().appendChild(xmlDocument.createCDATASection(textData));
      return this;
    }
    
    protected void performCloseTag() {
      tagStack.pop();
    }
  }
}
