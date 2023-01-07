package org.processor.batchjob;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.processor.model.Client;
import org.processor.model.Sale;
import org.processor.model.SalesSummary;
import org.processor.model.Seller;
import org.processor.utility.SaleItemParser;
import org.springframework.batch.item.ItemProcessor;

/**
 * Responsible for processing file data to extract information.
 */
@Slf4j
public class FileItemProcessor implements ItemProcessor<Object, SalesSummary> {

  private int sellers = 0;
  private int clients = 0;
  private String lowestSalesSeller;
  private String highestSaleId;
  private BigDecimal highestSale = BigDecimal.ZERO;

  private final Map<String, BigDecimal> sellerSalesMap = new HashMap<>();

  private final SaleItemParser saleItemParser = new SaleItemParser();

  /**
   * Receives data line read by file reader.
   *
   * @param data Contains data models
   * @return SalesSummary
   * @throws Exception generic exception
   */
  @Override
  public SalesSummary process(final Object data) throws Exception {

    log.debug(data.toString());

    if (data instanceof Seller) {
      sellers++;
    } else if (data instanceof Client) {
      clients++;
    } else if (data instanceof Sale) {

      var sale = (Sale) data;

      var currentSalesValue = findHighestSalesId(sale);
      findLowestSalesSeller(sale, currentSalesValue);
    }

    return SalesSummary.builder()
        .sellers(sellers)
        .clients(clients)
        .highestSaleId(highestSaleId)
        .lowestSalesSeller(lowestSalesSeller)
        .build();
  }

  /**
   * For each incoming sale data re-calculates highest sales id.
   *
   * @param sale new Sale data
   * @return incoming Sale's value
   */
  private BigDecimal findHighestSalesId(Sale sale) {

    var saleItems = saleItemParser.getSaleItems(sale.getItemsRaw());

    // Calculate sales value for incoming sale
    var salesValue = saleItems.stream()
        .map(x -> (x.getPrice().multiply(new BigDecimal(x.getAmount()))))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    int comparison = salesValue.compareTo(highestSale);

    if (comparison > 0) {
      highestSale = salesValue;
      highestSaleId = sale.getId();
    } else if (comparison == 0) {
      // Considering the possibility that multiple sales might end-up with same value
      highestSale = salesValue;
      highestSaleId = highestSaleId + "," + sale.getId();
    }

    return salesValue;
  }

  /**
   * Finds the lowest sales seller.
   *
   * @param sale new Sale data
   * @param salesValue incoming Sale's value
   */
  private void findLowestSalesSeller(Sale sale, BigDecimal salesValue) {

    if (sellerSalesMap.containsKey(sale.getSeller())) {
      sellerSalesMap.computeIfPresent(sale.getSeller(),
          (key, val) -> val.add(salesValue));
    } else {
      sellerSalesMap.put(sale.getSeller(), salesValue);
    }

    Map.Entry<String, BigDecimal> minEntry = null;
    for (var entry : sellerSalesMap.entrySet()) {
      if (minEntry == null) {
        minEntry = entry;
      } else if (entry.getValue().compareTo(minEntry.getValue()) < 0) {
        minEntry = entry;
      } else if (entry.getValue().compareTo(minEntry.getValue()) == 0) {
        minEntry = new AbstractMap.SimpleEntry<>(
            minEntry.getKey() + "," + entry.getKey(), minEntry.getValue());
      }
    }

    lowestSalesSeller = minEntry != null ? minEntry.getKey() : "";
  }
}
