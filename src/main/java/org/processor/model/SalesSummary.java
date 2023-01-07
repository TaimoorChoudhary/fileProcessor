package org.processor.model;

import java.util.StringJoiner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * Contains sales summary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesSummary {

  private int sellers;
  private int clients;
  private String highestSaleId;
  private String lowestSalesSeller;
  private String dataSourceName;

  /**
   * Check is the model object is empty.
   *
   * @return boolean
   */
  public boolean isEmpty() {
    return sellers == 0 && clients == 0
        && StringUtils.isEmpty(highestSaleId)
        && StringUtils.isEmpty(lowestSalesSeller);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ",  "Sales Summary [", "]")
        .add("Seller Count = " + sellers)
        .add("Client Count = " + clients)
        .add("Highest Sale Id = '" + highestSaleId + "'")
        .add("Lowest Sales Seller = '" + lowestSalesSeller + "'")
        .toString();
  }
}
