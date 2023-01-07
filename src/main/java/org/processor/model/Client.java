package org.processor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains client details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {

  private String code;
  private String name;
  private String profession;
}
