package org.processor.model;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/**
 * Contains item details
 */
@Data
@Builder
public class Item {

  private String id;
  private int amount;
  private BigDecimal price;
}
