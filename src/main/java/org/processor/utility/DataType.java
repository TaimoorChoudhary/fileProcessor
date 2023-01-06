package org.processor.utility;

import lombok.Getter;

@Getter
public enum DataType {

  SELLER("001"),
  CLIENT("002"),
  SALE("003");

  private final String code;

  DataType(String code) {
    this.code = code;
  }
}
