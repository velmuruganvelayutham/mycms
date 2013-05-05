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

import java.io.Serializable;
import org.meshcms.util.Utils;

/**
 * Encapsulates the functionalities of a form field.
 */
public class FormField implements Serializable {
  /**
   * Denotes a text field.
   */
  public static final int TEXT = 0;

  /**
   * Denotes a text field that is supposed to accept e-mail addresses.
   */
  public static final int EMAIL = 1;

  /**
   * Denotes a submit button.
   */
  public static final int SUBMIT = 2;

  /**
   * Denotes a reset button.
   */
  public static final int RESET = 3;

  /**
   * Denotes a text field that is supposed to accept numbers.
   */
  public static final int NUMBER = 4;

  /**
   * Denotes a hidden field. Hidden fields are stored in memory and not in the
   * form.
   */
  public static final int HIDDEN = 5;

  /**
   * Denotes a select field.
   */
  public static final int SELECT_OPTION = 6;

  private String name;
  private String code;
  private int type;
  private String value;
  private String[] options;
  private boolean required;
  private boolean sender;
  private boolean senderName;
  private boolean recipient;
  private boolean subject;
  private boolean messageBody;
  private int rows = 1;

  /**
   * Sets the friendly name of the field.
   */
  public void setName(String name) {
    this.name = Utils.trim(name);
  }

  /**
   * Returns the friendly name of the field.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the element name of the field.
   */
  public void setCode(String code) {
    this.code = Utils.trim(code);
  }

  /**
   * Returns the element name of the field.
   */
  public String getCode() {
    return code;
  }

  /**
   * Sets the type of the field.
   */
  public void setType(int type) {
    this.type = type;
  }

  /**
   * Returns the type of the field.
   */
  public int getType() {
    return type;
  }

  /**
   * Sets the value of the field.
   */
  public void setValue(String value) {
    this.value = Utils.trim(value);
  }

  /**
   * Returns the value of the field.
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets the options of the field.&nbsp;To be used only for fields whose type
   * is {@link #SELECT_OPTION}.
   */
  public void setOptions(String[] options) {
    this.options = options;

    for (int i = 0; i < options.length; i++) {
      this.options[i] = Utils.trim(this.options[i]);
    }
  }

  /**
   * Returns the options of the field.
   */
  public String[] getOptions() {
    return options;
  }

  /**
   * Sets both the friendly name and the element name of the field. The value
   * passed as argument is used as friendly name, while the element name will
   * be created using {@link #createCode}.
   */
  public void setNameAndCode(String name) {
    setName(name);
    setCode(createCode(name));
  }

  /**
   * Sets the required flag. A required field is supposed to have a value.
   */
  public void setRequired(boolean required) {
    this.required = required;
  }

  /**
   * Returns the value of the required flag.
   */
  public boolean isRequired() {
    return required;
  }

  /**
   * Sets the sender flag. This should be set to true for a field that is
   * supposed to contain the e-mail address of the sender.
   */
  public void setSender(boolean sender) {
    this.sender = sender;

    if (sender) {
      setRequired(true);
    }
  }

  /**
   * Returns the value of the sender flag.
   */
  public boolean isSender() {
    return sender;
  }

  /**
   * Sets the recipient flag. This should be set to true for a field that is
   * supposed to contain the e-mail address of the recipient.
   */
  public void setRecipient(boolean recipient) {
    this.recipient = recipient;
  }

  /**
   * Returns the value of the recipient flag.
   */
  public boolean isRecipient() {
    return recipient;
  }

  /**
   * Sets the number of rows for the editable field. In general, an
   * <code>input</code> field is replaced with a <code>textarea</code> when
   * this number is greater than 1.
   */
  public void setRows(int rows) {
    this.rows = rows;
  }

  /**
   * Returns the number of rows for the editable field.
   */
  public int getRows() {
    return rows;
  }

  /**
   * Sets the value of a parameter. The parameter set depends on the passed
   * argument.
   */
  public void setParameter(String value) {
    if (value.equals("text")) {
      setType(TEXT);
    } else if (value.equals("email")) {
      setType(EMAIL);
    } else if (value.startsWith("text:")) {
      setType(TEXT);
      setRows(Utils.parseInt(Utils.trim(value.substring(5)), 12));
    } else if (value.equals("textarea")) {
      setType(TEXT);
      setRows(12);
    } else if (value.equals("submit")) {
      setType(SUBMIT);
    } else if (value.equals("reset")) {
      setType(RESET);
    } else if (value.equals("number")) {
      setType(NUMBER);
    } else if (value.equals("hidden")) {
      setType(HIDDEN);
    } else if (value.startsWith("options:")) {
      setOptions(Utils.tokenize(value.substring(8), ","));
      setType(SELECT_OPTION);
    } else if (value.equals("required")) {
      setRequired(true);
    } else if (value.equals("sender")) {
      setSender(true); // implies setRequired(true);
    } else if (value.equals("sendername")) {
      setSenderName(true);
    } else if (value.equals("subject")) {
      setSubject(true);
    } else if (value.equals("messagebody")) {
      setMessageBody(true);
    } else if (value.startsWith("recipient:")) {
      setRecipient(true);
      setValue(value.substring(10));
    } else if (value.startsWith("value:")) {
      setValue(value.substring(6));
    } else {
      setNameAndCode(value);
    }
  }

  /**
   * Tries to determine if the value of the field is acceptable. The check
   * covers required values, e-mail fields and numeric fields.
   */
  public boolean checkValue() {
    String val = Utils.noNull(getValue()).trim();

    if (isRequired() && val.equals("")) {
      return false;
    }

    if (getType() == EMAIL || isSender() || isRecipient()) {
      if (!Utils.checkAddress(val)) {
        return false;
      }
    }

    if (getType() == NUMBER && !val.equals("")) {
      try {
        Double.parseDouble(val);
      } catch (Exception ex) {
        return false;
      }
    }

    val = val.toLowerCase();

    /* The purpose of this if block is to reject some attempts to spam the mail
       form. All identified attempts contained these pieces of text somewhere */
      return !(val.indexOf("content-type:") > -1 &&
          val.indexOf("mime-version:") > -1);

  }

  /**
   * Returns a modified version of the passed string, such as it can be used as
   * a field name.
   */
  public static String createCode(String s) {
    StringBuffer sb = new StringBuffer(s.length());

    for (int i = 0; i < s.length(); i++) {
      char c = Character.toLowerCase(s.charAt(i));

      if ("abcdefghijklmnopqrstuvwxyz_-0123456789".indexOf(c) < 0) {
        sb.append('_');
      } else {
        sb.append(c);
      }
    }

    return sb.toString();
  }

  /**
   * Returns a descriptive name of the field. This is generally the field name.
   */
  public String getDescription() {
    String desc = getValue();

    if (Utils.isNullOrEmpty(desc)) {
      desc = getName();

      if (Utils.isNullOrEmpty(desc)) {
        if (isRecipient()) {
          desc = "(recipient)";
        } else if (isSender()) {
          desc = "(sender)";
        } else {
          desc = "(unknown)";
        }
      }
    }

    return desc;
  }

  /**
   * Returns a description of the field.
   */
  public String toString() {
    return "Form Field: " + getDescription();
  }

  /**
   * Returns the value of the sender name flag.
   */
  public boolean isSenderName() {
    return senderName;
  }

  /**
   * Sets the sender name flag. This should be set to true for a field that is
   * supposed to contain the (complete) name of the sender.
   */
  public void setSenderName(boolean senderName) {
    this.senderName = senderName;
  }

  /**
   * Returns the value of the subject flag.
   */
  public boolean isSubject() {
    return subject;
  }

  /**
   * Sets the subject flag. This should be set to true for a field that is
   * supposed to contain the subject of the message.
   */
  public void setSubject(boolean subject) {
    this.subject = subject;
  }

  /**
   * Returns the value of the message body flag.
   */
  public boolean isMessageBody() {
    return messageBody;
  }

  /**
   * Sets the message body flag. If true, this filed is considered to be the
   * body of the message and will be written without caption
   */
  public void setMessageBody(boolean messageBody) {
    this.messageBody = messageBody;
  }

  /*public static List getFields(ModuleDescriptor md, PageContext pageContext) {
    List fields = new ArrayList();

    if (Utils.checkAddress(md.getArgument())) {
      FormField tempField = new FormField();
      String recipient = md.getArgument();
      tempField.setParameter("recipient:" + recipient);
      fields.add(tempField);

      ResourceBundle pageBundle = ResourceBundle.getBundle
          ("org/meshcms/webui/Locales", WebUtils.getPageLocale(pageContext));

      tempField = new FormField();
      tempField.setParameter(pageBundle.getString("mailName"));
      tempField.setParameter("sendername");
      fields.add(tempField);

      tempField = new FormField();
      tempField.setParameter(pageBundle.getString("mailAddress"));
      tempField.setParameter("email");
      tempField.setParameter("sender");
      fields.add(tempField);

      tempField = new FormField();
      tempField.setParameter(pageBundle.getString("mailMessage"));
      tempField.setParameter("textarea");
      tempField.setParameter("messagebody");
      fields.add(tempField);

      tempField = new FormField();
      tempField.setParameter(pageBundle.getString("mailSend"));
      tempField.setParameter("submit");
      fields.add(tempField);
    } else {
      WebSite webSite = (WebSite) pageContext.getRequest().getAttribute("webSite");
      File[] files = md.getModuleFiles(webSite, false);
      WebUtils.updateLastModifiedTime((HttpServletRequest) pageContext.getRequest(), files[0]);

      try {
        String[] lines = Utils.readAllLines(files[0]);
        FormField field = null;

        for (int i = 0; i < lines.length; i++) {
          String line = lines[i].trim();

          if (line.equals("")) {
            if (field != null) {
              fields.add(field);
              field = null;
            }
          } else {
            if (field == null) {
              field = new FormField();
            }

            field.setParameter(line);
          }
        }

        if (field != null) {
          fields.add(field);
        }
      } catch (IOException ex) {
        webSite.log("Can't read mail form description", ex);
      }
    }

    return fields;
  }*/
}
