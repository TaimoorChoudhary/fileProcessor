package org.processor.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains sale details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

  private String code;
  private String id;
  private String itemsRaw;
  private String seller;
  private List<Item> items;
}
