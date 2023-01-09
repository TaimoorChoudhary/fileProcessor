package org.processor.utility;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.processor.model.SaleItem;
import org.springframework.batch.item.ParseException;
import org.springframework.stereotype.Service;

/**
 * Handles parsing of sale items
 */
@Service
public class SaleItemParser {

  /**
   * Extracts sale items details.
   *
   * @param rawData data to parse
   * @return List of {@link SaleItem}
   */
  public List<SaleItem> getSaleItems(String rawData) {
    var saleItems = new ArrayList<SaleItem>();

    if (!rawData.startsWith("[") || !rawData.endsWith("]")) {
      throw new ParseException("Unable to parse sale items, invalid format");
    }

    var updateRawData = rawData.replaceAll("[\\[\\]]", "");

    var saleItemsString = updateRawData.split(";");

    for (String item : saleItemsString) {
      saleItems.add(constructSaleItem(item));
    }

    return saleItems;
  }

  private SaleItem constructSaleItem(String saleItemString) {
    var itemValues = saleItemString.split("-");

    if (itemValues.length == 3) {
      return SaleItem.builder()
          .id(itemValues[0])
          .amount(Integer.parseInt(itemValues[1]))
          .price(new BigDecimal(itemValues[2]))
          .build();
    } else {
      throw new ParseException("Unable to parse sale items, invalid format");
    }
  }
}
