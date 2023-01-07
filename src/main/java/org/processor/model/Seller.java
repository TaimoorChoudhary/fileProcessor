package org.processor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains seller details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seller {

  private String code;
  private String name;
}
