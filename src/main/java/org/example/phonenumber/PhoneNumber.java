/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 *
 */
package org.example.phonenumber;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Scim core schema, <a
 * href="https://tools.ietf.org/html/rfc7643#section-4.1.2>section 4.1.2</a>
 *
 */

public class PhoneNumber {

  private static final long serialVersionUID = 607319505715224096L;

  private static final String VISUAL_SEPARATORS = "[\\(\\)\\-\\.]";

  private static boolean strict = true;

  String value;

  String display;

  String type;

  Boolean primary = false;

  boolean isGlobalNumber = false;

  String number;

  String extension;

  String subAddress;

  String phoneContext;

  boolean isDomainPhoneContext = false;

  Map<String, String> params;

  public static boolean isStrict() {
    return PhoneNumber.strict;
  }

  public static void setStrict(boolean strict) {
    PhoneNumber.strict = strict;
  }

  public void addParam(String name, String value) {
    if (this.params == null) {
      this.params = new HashMap<String, String>();
    }

    this.params.put(name, value);
  }

  // This is annotated here to ensure that JAXB uses the setter rather than
  // reflection
  // to assigned the value. Do not move the XmlElement annotation to the field
  // please.
  public String getValue() {
    return value;
  }

  public void setValue(String value) throws PhoneNumberParseException {
    if (value == null) {
      throw new PhoneNumberParseException("null values are illegal for phone numbers");
    }

    if (strict) {
      PhoneNumberLexer phoneNumberLexer = new PhoneNumberLexer(new ANTLRInputStream(value));
      PhoneNumberParser p = new PhoneNumberParser(new CommonTokenStream(phoneNumberLexer));
      p.setBuildParseTree(true);

      p.addErrorListener(new BaseErrorListener() {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
          throw new IllegalStateException("failed to parse at line " + line + " due to " + msg, e);
        }
      });

      PhoneNumberParseTreeListener tpl = new PhoneNumberParseTreeListener();
      try {
        ParseTree tree = p.phoneNumber();
        ParseTreeWalker.DEFAULT.walk(tpl, tree);
      } catch (IllegalStateException e) {
        throw new PhoneNumberParseException(e);
      }

      PhoneNumber parsedPhoneNumber = tpl.getPhoneNumber();

      this.value = parsedPhoneNumber.getValue();
      this.number = parsedPhoneNumber.getNumber();
      this.extension = parsedPhoneNumber.getExtension();
      this.subAddress = parsedPhoneNumber.getSubAddress();
      this.phoneContext = parsedPhoneNumber.getPhoneContext();
      this.params = parsedPhoneNumber.getParams();
      this.isGlobalNumber = parsedPhoneNumber.isGlobalNumber();
      this.isDomainPhoneContext = parsedPhoneNumber.isDomainPhoneContext();
    } else {
      this.value = value;
    }
  }

  /*
   * Implements RFC 3996 URI Equality for the value property
   * https://tools.ietf.org/html/rfc3966#section-3
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PhoneNumber other = (PhoneNumber) obj;

    if (isGlobalNumber != other.isGlobalNumber)
      return false;

    String numberWithoutVisualSeparators = number != null ? number.replaceAll(VISUAL_SEPARATORS, "") : null;
    String otherNumberWithoutVisualSeparators = other.number != null ? other.number.replaceAll(VISUAL_SEPARATORS, "") : null;
    if (numberWithoutVisualSeparators == null) {
      if (otherNumberWithoutVisualSeparators != null)
        return false;
    } else if (!numberWithoutVisualSeparators.equals(otherNumberWithoutVisualSeparators))
      return false;

    String extensionWithoutVisualSeparators = extension != null ? extension.replaceAll(VISUAL_SEPARATORS, "") : null;
    String otherExtensionWithoutVisualSeparators = other.extension != null ? other.extension.replaceAll(VISUAL_SEPARATORS, "") : null;
    if (extensionWithoutVisualSeparators == null) {
      if (otherExtensionWithoutVisualSeparators != null)
        return false;
    } else if (!extensionWithoutVisualSeparators.equals(otherExtensionWithoutVisualSeparators))
      return false;

    if (subAddress == null) {
      if (other.subAddress != null)
        return false;
    } else if (!subAddress.equalsIgnoreCase(other.subAddress))
      return false;

    String phoneContextTemp = phoneContext;
    if (!StringUtils.isBlank(phoneContext) && !isDomainPhoneContext) {
      phoneContextTemp = phoneContext.replaceAll(VISUAL_SEPARATORS, "");
    }

    String otherPhoneContextTemp = other.phoneContext;
    if (!StringUtils.isBlank(other.phoneContext) && !other.isDomainPhoneContext) {
      otherPhoneContextTemp = other.phoneContext.replaceAll(VISUAL_SEPARATORS, "");
    }

    if (phoneContextTemp == null) {
      if (otherPhoneContextTemp != null)
        return false;
    } else if (!phoneContextTemp.equalsIgnoreCase(otherPhoneContextTemp))
      return false;

    if (!equalsIgnoreCaseAndOrderParams(other.params)) {
      return false;
    }

    if (primary == null) {
      if (other.primary != null)
        return false;
    } else if (!primary.equals(other.primary))
      return false;

    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equalsIgnoreCase(other.type))
      return false;

    return true;
  }

  /*
   * Implements RFC 3996 URI Equality for the value property
   * https://tools.ietf.org/html/rfc3966#section-3
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (isGlobalNumber ? 1231 : 1237);
    result = prime * result + ((number == null) ? 0 : number.replaceAll(VISUAL_SEPARATORS, "").hashCode());
    result = prime * result + ((extension == null) ? 0 : extension.replaceAll(VISUAL_SEPARATORS, "").hashCode());
    result = prime * result + ((subAddress == null) ? 0 : subAddress.toLowerCase().hashCode());
    result = prime * result + ((phoneContext == null) ? 0 : (isDomainPhoneContext ? phoneContext.toLowerCase().hashCode() : phoneContext.replaceAll(VISUAL_SEPARATORS, "").hashCode()));
    result = prime * result + ((params == null) ? 0 : paramsToLowerCase().hashCode());
    result = prime * result + ((primary == null) ? 0 : primary.hashCode());
    result = prime * result + ((type == null) ? 0 : type.toLowerCase().hashCode());
    return result;
  }

  HashMap<String, String> paramsToLowerCase() {
    HashMap<String, String> paramsLowercase = new HashMap<String, String>();
    for (Entry<String, String> entry : params.entrySet()) {
      paramsLowercase.put(entry.getKey().toLowerCase(), entry.getValue().toLowerCase());
    }

    return paramsLowercase;
  }

  boolean equalsIgnoreCaseAndOrderParams(Map<String, String> otherParams) {
    if (params == null && otherParams == null) {
      return true;
    }

    if ((params == null && otherParams != null) || (params != null && otherParams == null) || (params.size() != otherParams.size())) {
      return false;
    }

    HashMap<String, String> paramsLowercase = paramsToLowerCase();

    for (Entry<String, String> entry : otherParams.entrySet()) {
      String foundValue = paramsLowercase.get(entry.getKey().toLowerCase());

      if (!entry.getValue().equalsIgnoreCase(foundValue)) {
        return false;
      }
    }

    return true;
  }

  public String getDisplay() {
    return this.display;
  }

  public String getType() {
    return this.type;
  }

  public Boolean getPrimary() {
    return this.primary;
  }

  public boolean isGlobalNumber() {
    return this.isGlobalNumber;
  }

  public String getNumber() {
    return this.number;
  }

  public String getExtension() {
    return this.extension;
  }

  public String getSubAddress() {
    return this.subAddress;
  }

  public String getPhoneContext() {
    return this.phoneContext;
  }

  public boolean isDomainPhoneContext() {
    return this.isDomainPhoneContext;
  }

  public Map<String, String> getParams() {
    return this.params;
  }

  public PhoneNumber setDisplay(String display) {
    this.display = display;
    return this;
  }

  public PhoneNumber setType(String type) {
    this.type = type;
    return this;
  }

  public PhoneNumber setPrimary(Boolean primary) {
    this.primary = primary;
    return this;
  }

  public abstract static class PhoneNumberBuilder {

    static final Logger LOGGER = LoggerFactory.getLogger(PhoneNumberBuilder.class);

    final String HYPHEN = "-";
    final String INTERNATIONAL_PREFIX = "+";
    final String PREFIX = "tel:%s";
    final String EXTENSTION_PREFIX = ";ext=%s";
    final String ISUB_PREFIX = ";isub=%s";
    final String CONTEXT_PREFIX = ";phone-context=%s";
    final String PARAMS_STRING = ";%s=%s";
    final String LOCAL_SUBSCRIBER_NUMBER_REGEX = "^[\\d\\.\\-\\(\\)]+$";
    final String DOMAIN_NAME_REGEX = "^[a-zA-Z0-9\\.\\-]+$";
    final String GLOBAL_NUMBER_REGEX = "^(\\+)?[\\d\\.\\-\\(\\)]+$";
    final String COUNTRY_CODE_REGEX = "^(\\+)?[1-9][0-9]{0,2}$";

    String number;
    String display;
    String extension;
    String subAddress;
    String phoneContext;
    Map<String, String> params;

    boolean isGlobalNumber = false;
    boolean isDomainPhoneContext = false;

    public PhoneNumberBuilder() {
    }

    public PhoneNumberBuilder display(String display) {
      this.display = display;
      return this;
    }

    public PhoneNumberBuilder extension(String extension) {
      this.extension = extension;
      return this;
    }

    public PhoneNumberBuilder subAddress(String subAddress) {
      this.subAddress = subAddress;
      return this;
    }

    public PhoneNumberBuilder phoneContext(String phoneContext) {
      this.phoneContext = phoneContext;
      return this;
    }

    public PhoneNumberBuilder param(String name, String value) {
      if (this.params == null) {
        this.params = new HashMap<String, String>();
      }

      this.params.put(name, value);
      return this;
    }

    String getFormattedExtension() {
      if (this.extension != null && !this.extension.isEmpty()) {
        return String.format(EXTENSTION_PREFIX, this.extension);
      }

      return null;
    }

    String getFormattedSubAddress() {
      if (this.subAddress != null && !this.subAddress.isEmpty()) {
        return String.format(ISUB_PREFIX, this.subAddress);
      }

      return null;
    }

    String getFormattedPhoneContext() {
      if (this.phoneContext != null && !this.phoneContext.isEmpty()) {
        return String.format(CONTEXT_PREFIX, this.phoneContext);
      }

      return null;
    }

    String getFormattedParams() {
      String paramsFormatted = "";

      if (params != null) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
          paramsFormatted += String.format(PARAMS_STRING, entry.getKey(), entry.getValue() != null ? entry.getValue() : "");
        }
      }

      return !paramsFormatted.isEmpty() ? paramsFormatted : null;
    }

    String getFormattedValue() {
      String valueString = String.format(PREFIX, this.number);

      String fExtension = getFormattedExtension();
      if (fExtension != null) {
        valueString += fExtension;
      }

      String fSubAddr = getFormattedSubAddress();
      if (fSubAddr != null) {
        valueString += fSubAddr;
      }

      String fContext = getFormattedPhoneContext();
      if (fContext != null) {
        valueString += fContext;
      }

      String fParams = getFormattedParams();
      if (fParams != null) {
        valueString += fParams;
      }

      return !valueString.isEmpty() ? valueString : null;
    }

    public PhoneNumber build() throws PhoneNumberParseException {
      return build(true);
    }

    public PhoneNumber build(boolean validate) throws PhoneNumberParseException {
      if (!StringUtils.isBlank(extension) && !StringUtils.isBlank(subAddress)) {
        throw new IllegalArgumentException("PhoneNumberBuilder cannot have a value for both extension and subAddress.");
      }

      if (extension != null && !extension.matches(LOCAL_SUBSCRIBER_NUMBER_REGEX)) {
        throw new IllegalArgumentException("PhoneNumberBuilder extension must contain only numeric characters and optional ., -, (, ) visual separator characters.");
      }

      if (params != null && !params.isEmpty()) {
        if (params.get("") != null || params.get(null) != null || params.values().contains(null) || params.values().contains("")) {
          throw new IllegalArgumentException("PhoneNumberBuilder params names and values cannot be null or empty.");
        }
      }

      PhoneNumber phoneNumber = new PhoneNumber();

      String formattedValue = getFormattedValue();
      LOGGER.debug("" + formattedValue);

      if (validate) {
        phoneNumber.setValue(formattedValue);
      } else {
        phoneNumber.value = formattedValue;
        phoneNumber.extension = this.extension;
        phoneNumber.isDomainPhoneContext = this.isDomainPhoneContext;
        phoneNumber.isGlobalNumber = this.isGlobalNumber;
        phoneNumber.number = this.number;
        phoneNumber.params = this.params;
        phoneNumber.phoneContext = this.phoneContext;
        phoneNumber.subAddress = this.subAddress;
      }
      return phoneNumber;
    }

    public String getHYPHEN() {
      return this.HYPHEN;
    }

    public String getINTERNATIONAL_PREFIX() {
      return this.INTERNATIONAL_PREFIX;
    }

    public String getPREFIX() {
      return this.PREFIX;
    }

    public String getEXTENSTION_PREFIX() {
      return this.EXTENSTION_PREFIX;
    }

    public String getISUB_PREFIX() {
      return this.ISUB_PREFIX;
    }

    public String getCONTEXT_PREFIX() {
      return this.CONTEXT_PREFIX;
    }

    public String getPARAMS_STRING() {
      return this.PARAMS_STRING;
    }

    public String getLOCAL_SUBSCRIBER_NUMBER_REGEX() {
      return this.LOCAL_SUBSCRIBER_NUMBER_REGEX;
    }

    public String getDOMAIN_NAME_REGEX() {
      return this.DOMAIN_NAME_REGEX;
    }

    public String getGLOBAL_NUMBER_REGEX() {
      return this.GLOBAL_NUMBER_REGEX;
    }

    public String getCOUNTRY_CODE_REGEX() {
      return this.COUNTRY_CODE_REGEX;
    }

    public String getNumber() {
      return this.number;
    }

    public String getDisplay() {
      return this.display;
    }

    public String getExtension() {
      return this.extension;
    }

    public String getSubAddress() {
      return this.subAddress;
    }

    public String getPhoneContext() {
      return this.phoneContext;
    }

    public Map<String, String> getParams() {
      return this.params;
    }

    public boolean isGlobalNumber() {
      return this.isGlobalNumber;
    }

    public boolean isDomainPhoneContext() {
      return this.isDomainPhoneContext;
    }

    public PhoneNumberBuilder setNumber(String number) {
      this.number = number;
      return this;
    }

    public PhoneNumberBuilder setDisplay(String display) {
      this.display = display;
      return this;
    }

    public PhoneNumberBuilder setExtension(String extension) {
      this.extension = extension;
      return this;
    }

    public PhoneNumberBuilder setSubAddress(String subAddress) {
      this.subAddress = subAddress;
      return this;
    }

    public PhoneNumberBuilder setPhoneContext(String phoneContext) {
      this.phoneContext = phoneContext;
      return this;
    }

    public PhoneNumberBuilder setParams(Map<String, String> params) {
      this.params = params;
      return this;
    }

    public PhoneNumberBuilder setGlobalNumber(boolean isGlobalNumber) {
      this.isGlobalNumber = isGlobalNumber;
      return this;
    }

    public PhoneNumberBuilder setDomainPhoneContext(boolean isDomainPhoneContext) {
      this.isDomainPhoneContext = isDomainPhoneContext;
      return this;
    }

    public boolean equals(final Object o) {
      if (o == this) return true;
      if (!(o instanceof PhoneNumberBuilder)) return false;
      final PhoneNumberBuilder other = (PhoneNumberBuilder) o;
      if (!other.canEqual((Object) this)) return false;
      final Object this$HYPHEN = this.getHYPHEN();
      final Object other$HYPHEN = other.getHYPHEN();
      if (this$HYPHEN == null ? other$HYPHEN != null : !this$HYPHEN.equals(other$HYPHEN)) return false;
      final Object this$INTERNATIONAL_PREFIX = this.getINTERNATIONAL_PREFIX();
      final Object other$INTERNATIONAL_PREFIX = other.getINTERNATIONAL_PREFIX();
      if (this$INTERNATIONAL_PREFIX == null ? other$INTERNATIONAL_PREFIX != null : !this$INTERNATIONAL_PREFIX.equals(other$INTERNATIONAL_PREFIX))
        return false;
      final Object this$PREFIX = this.getPREFIX();
      final Object other$PREFIX = other.getPREFIX();
      if (this$PREFIX == null ? other$PREFIX != null : !this$PREFIX.equals(other$PREFIX)) return false;
      final Object this$EXTENSTION_PREFIX = this.getEXTENSTION_PREFIX();
      final Object other$EXTENSTION_PREFIX = other.getEXTENSTION_PREFIX();
      if (this$EXTENSTION_PREFIX == null ? other$EXTENSTION_PREFIX != null : !this$EXTENSTION_PREFIX.equals(other$EXTENSTION_PREFIX))
        return false;
      final Object this$ISUB_PREFIX = this.getISUB_PREFIX();
      final Object other$ISUB_PREFIX = other.getISUB_PREFIX();
      if (this$ISUB_PREFIX == null ? other$ISUB_PREFIX != null : !this$ISUB_PREFIX.equals(other$ISUB_PREFIX))
        return false;
      final Object this$CONTEXT_PREFIX = this.getCONTEXT_PREFIX();
      final Object other$CONTEXT_PREFIX = other.getCONTEXT_PREFIX();
      if (this$CONTEXT_PREFIX == null ? other$CONTEXT_PREFIX != null : !this$CONTEXT_PREFIX.equals(other$CONTEXT_PREFIX))
        return false;
      final Object this$PARAMS_STRING = this.getPARAMS_STRING();
      final Object other$PARAMS_STRING = other.getPARAMS_STRING();
      if (this$PARAMS_STRING == null ? other$PARAMS_STRING != null : !this$PARAMS_STRING.equals(other$PARAMS_STRING))
        return false;
      final Object this$LOCAL_SUBSCRIBER_NUMBER_REGEX = this.getLOCAL_SUBSCRIBER_NUMBER_REGEX();
      final Object other$LOCAL_SUBSCRIBER_NUMBER_REGEX = other.getLOCAL_SUBSCRIBER_NUMBER_REGEX();
      if (this$LOCAL_SUBSCRIBER_NUMBER_REGEX == null ? other$LOCAL_SUBSCRIBER_NUMBER_REGEX != null : !this$LOCAL_SUBSCRIBER_NUMBER_REGEX.equals(other$LOCAL_SUBSCRIBER_NUMBER_REGEX))
        return false;
      final Object this$DOMAIN_NAME_REGEX = this.getDOMAIN_NAME_REGEX();
      final Object other$DOMAIN_NAME_REGEX = other.getDOMAIN_NAME_REGEX();
      if (this$DOMAIN_NAME_REGEX == null ? other$DOMAIN_NAME_REGEX != null : !this$DOMAIN_NAME_REGEX.equals(other$DOMAIN_NAME_REGEX))
        return false;
      final Object this$GLOBAL_NUMBER_REGEX = this.getGLOBAL_NUMBER_REGEX();
      final Object other$GLOBAL_NUMBER_REGEX = other.getGLOBAL_NUMBER_REGEX();
      if (this$GLOBAL_NUMBER_REGEX == null ? other$GLOBAL_NUMBER_REGEX != null : !this$GLOBAL_NUMBER_REGEX.equals(other$GLOBAL_NUMBER_REGEX))
        return false;
      final Object this$COUNTRY_CODE_REGEX = this.getCOUNTRY_CODE_REGEX();
      final Object other$COUNTRY_CODE_REGEX = other.getCOUNTRY_CODE_REGEX();
      if (this$COUNTRY_CODE_REGEX == null ? other$COUNTRY_CODE_REGEX != null : !this$COUNTRY_CODE_REGEX.equals(other$COUNTRY_CODE_REGEX))
        return false;
      final Object this$number = this.getNumber();
      final Object other$number = other.getNumber();
      if (this$number == null ? other$number != null : !this$number.equals(other$number)) return false;
      final Object this$display = this.getDisplay();
      final Object other$display = other.getDisplay();
      if (this$display == null ? other$display != null : !this$display.equals(other$display)) return false;
      final Object this$extension = this.getExtension();
      final Object other$extension = other.getExtension();
      if (this$extension == null ? other$extension != null : !this$extension.equals(other$extension)) return false;
      final Object this$subAddress = this.getSubAddress();
      final Object other$subAddress = other.getSubAddress();
      if (this$subAddress == null ? other$subAddress != null : !this$subAddress.equals(other$subAddress))
        return false;
      final Object this$phoneContext = this.getPhoneContext();
      final Object other$phoneContext = other.getPhoneContext();
      if (this$phoneContext == null ? other$phoneContext != null : !this$phoneContext.equals(other$phoneContext))
        return false;
      final Object this$params = this.getParams();
      final Object other$params = other.getParams();
      if (this$params == null ? other$params != null : !this$params.equals(other$params)) return false;
      if (this.isGlobalNumber() != other.isGlobalNumber()) return false;
      if (this.isDomainPhoneContext() != other.isDomainPhoneContext()) return false;
      return true;
    }

    protected boolean canEqual(final Object other) {
      return other instanceof PhoneNumberBuilder;
    }

    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      final Object $HYPHEN = this.getHYPHEN();
      result = result * PRIME + ($HYPHEN == null ? 43 : $HYPHEN.hashCode());
      final Object $INTERNATIONAL_PREFIX = this.getINTERNATIONAL_PREFIX();
      result = result * PRIME + ($INTERNATIONAL_PREFIX == null ? 43 : $INTERNATIONAL_PREFIX.hashCode());
      final Object $PREFIX = this.getPREFIX();
      result = result * PRIME + ($PREFIX == null ? 43 : $PREFIX.hashCode());
      final Object $EXTENSTION_PREFIX = this.getEXTENSTION_PREFIX();
      result = result * PRIME + ($EXTENSTION_PREFIX == null ? 43 : $EXTENSTION_PREFIX.hashCode());
      final Object $ISUB_PREFIX = this.getISUB_PREFIX();
      result = result * PRIME + ($ISUB_PREFIX == null ? 43 : $ISUB_PREFIX.hashCode());
      final Object $CONTEXT_PREFIX = this.getCONTEXT_PREFIX();
      result = result * PRIME + ($CONTEXT_PREFIX == null ? 43 : $CONTEXT_PREFIX.hashCode());
      final Object $PARAMS_STRING = this.getPARAMS_STRING();
      result = result * PRIME + ($PARAMS_STRING == null ? 43 : $PARAMS_STRING.hashCode());
      final Object $LOCAL_SUBSCRIBER_NUMBER_REGEX = this.getLOCAL_SUBSCRIBER_NUMBER_REGEX();
      result = result * PRIME + ($LOCAL_SUBSCRIBER_NUMBER_REGEX == null ? 43 : $LOCAL_SUBSCRIBER_NUMBER_REGEX.hashCode());
      final Object $DOMAIN_NAME_REGEX = this.getDOMAIN_NAME_REGEX();
      result = result * PRIME + ($DOMAIN_NAME_REGEX == null ? 43 : $DOMAIN_NAME_REGEX.hashCode());
      final Object $GLOBAL_NUMBER_REGEX = this.getGLOBAL_NUMBER_REGEX();
      result = result * PRIME + ($GLOBAL_NUMBER_REGEX == null ? 43 : $GLOBAL_NUMBER_REGEX.hashCode());
      final Object $COUNTRY_CODE_REGEX = this.getCOUNTRY_CODE_REGEX();
      result = result * PRIME + ($COUNTRY_CODE_REGEX == null ? 43 : $COUNTRY_CODE_REGEX.hashCode());
      final Object $number = this.getNumber();
      result = result * PRIME + ($number == null ? 43 : $number.hashCode());
      final Object $display = this.getDisplay();
      result = result * PRIME + ($display == null ? 43 : $display.hashCode());
      final Object $extension = this.getExtension();
      result = result * PRIME + ($extension == null ? 43 : $extension.hashCode());
      final Object $subAddress = this.getSubAddress();
      result = result * PRIME + ($subAddress == null ? 43 : $subAddress.hashCode());
      final Object $phoneContext = this.getPhoneContext();
      result = result * PRIME + ($phoneContext == null ? 43 : $phoneContext.hashCode());
      final Object $params = this.getParams();
      result = result * PRIME + ($params == null ? 43 : $params.hashCode());
      result = result * PRIME + (this.isGlobalNumber() ? 79 : 97);
      result = result * PRIME + (this.isDomainPhoneContext() ? 79 : 97);
      return result;
    }

    public String toString() {
      return "PhoneNumber.PhoneNumberBuilder(HYPHEN=" + this.getHYPHEN() + ", INTERNATIONAL_PREFIX=" + this.getINTERNATIONAL_PREFIX() + ", PREFIX=" + this.getPREFIX() + ", EXTENSTION_PREFIX=" + this.getEXTENSTION_PREFIX() + ", ISUB_PREFIX=" + this.getISUB_PREFIX() + ", CONTEXT_PREFIX=" + this.getCONTEXT_PREFIX() + ", PARAMS_STRING=" + this.getPARAMS_STRING() + ", LOCAL_SUBSCRIBER_NUMBER_REGEX=" + this.getLOCAL_SUBSCRIBER_NUMBER_REGEX() + ", DOMAIN_NAME_REGEX=" + this.getDOMAIN_NAME_REGEX() + ", GLOBAL_NUMBER_REGEX=" + this.getGLOBAL_NUMBER_REGEX() + ", COUNTRY_CODE_REGEX=" + this.getCOUNTRY_CODE_REGEX() + ", number=" + this.getNumber() + ", display=" + this.getDisplay() + ", extension=" + this.getExtension() + ", subAddress=" + this.getSubAddress() + ", phoneContext=" + this.getPhoneContext() + ", params=" + this.getParams() + ", isGlobalNumber=" + this.isGlobalNumber() + ", isDomainPhoneContext=" + this.isDomainPhoneContext() + ")";
    }
  }

  public static class LocalPhoneNumberBuilder extends PhoneNumberBuilder {
    String subscriberNumber;
    String countryCode;
    String areaCode;
    String domainName;

    public LocalPhoneNumberBuilder subscriberNumber(String subscriberNumber) {
      this.subscriberNumber = subscriberNumber;
      this.number = subscriberNumber;
      return this;
    }

    public LocalPhoneNumberBuilder countryCode(String countryCode) {

      String localCode = countryCode;

      if (localCode != null && !localCode.isEmpty()) {
        localCode = localCode.trim();
        if (localCode.length() > 0 && localCode.charAt(0) != '+') {
          localCode = '+' + localCode;
        }
      }
      this.countryCode = localCode;
      return this;
    }

    public LocalPhoneNumberBuilder areaCode(String areaCode) {
      this.areaCode = areaCode;
      return this;
    }

    public LocalPhoneNumberBuilder domainName(String domainName) {
      this.domainName = domainName;
      return this;
    }

    public LocalPhoneNumberBuilder isDomainPhoneContext(boolean hasDomainPhoneContext) {
      this.isDomainPhoneContext = hasDomainPhoneContext;
      return this;
    }

    @Override
    public PhoneNumber build() throws PhoneNumberParseException {
      if (StringUtils.isBlank(subscriberNumber) || !subscriberNumber.matches(LOCAL_SUBSCRIBER_NUMBER_REGEX)) {
        throw new IllegalArgumentException("LocalPhoneNumberBuilder subscriberNumber must contain only numeric characters and optional ., -, (, ) visual separator characters.");
      }

      if (StringUtils.isBlank(countryCode) && StringUtils.isBlank(domainName)) {
        throw new IllegalArgumentException("LocalPhoneNumberBuilder must have values for domainName or countryCode.");
      }

      if (StringUtils.isBlank(domainName)) {
        if (StringUtils.isBlank(countryCode) || !countryCode.matches(COUNTRY_CODE_REGEX)) {
          throw new IllegalArgumentException("LocalPhoneNumberBuilder countryCode must contain only numeric characters and an optional plus (+) prefix.");
        }

        if (areaCode != null && !StringUtils.isNumeric(areaCode)) {
          throw new IllegalArgumentException("LocalPhoneNumberBuilder areaCode must contain only numberic characters.");
        }

        if (!countryCode.startsWith(INTERNATIONAL_PREFIX)) {
          this.phoneContext = INTERNATIONAL_PREFIX + countryCode;
        } else {
          this.phoneContext = countryCode;
        }

        if (!StringUtils.isBlank(areaCode)) {
          this.phoneContext += (HYPHEN + areaCode);
        }

      } else {
        if (!domainName.matches(DOMAIN_NAME_REGEX)) {
          throw new IllegalArgumentException("LocalPhoneNumberBuilder domainName must contain only alphanumeric, . and - characters.");
        }

        this.phoneContext = domainName;
      }

      return super.build();
    }
  }

  public static class GlobalPhoneNumberBuilder extends PhoneNumberBuilder {
    String globalNumber;

    public GlobalPhoneNumberBuilder() {
      this.isGlobalNumber = true;
    }

    public GlobalPhoneNumberBuilder globalNumber(String globalNumber) {
      this.globalNumber = globalNumber;

      if (globalNumber != null) {
        if (globalNumber.startsWith(INTERNATIONAL_PREFIX)) {
          this.number = globalNumber;
        } else {
          this.number = INTERNATIONAL_PREFIX + globalNumber;
        }
      }

      return this;
    }

    @Override
    public PhoneNumber build() throws PhoneNumberParseException {
      if (StringUtils.isBlank(globalNumber) || !globalNumber.matches(GLOBAL_NUMBER_REGEX)) {
        throw new IllegalArgumentException("GlobalPhoneNumberBuilder globalNumber must contain only numeric characters, optional ., -, (, ) visual separators, and an optional plus (+) prefix.");
      }

      return super.build();
    }
  }
}
