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

@Slf4j
public class FileItemProcessor implements ItemProcessor<Object, SalesSummary> {

  private int sellers = 0;
  private int clients = 0;
  private BigDecimal highestSale = BigDecimal.ZERO;
  private String lowestSalesSeller;
  private String highestSaleId;

  private final Map<String, BigDecimal> sellerSalesMap = new HashMap<>();

  private final SaleItemParser saleItemParser = new SaleItemParser();

  @Override
  public SalesSummary process(final Object data) throws Exception {

    log.debug(data.toString());

    if (data instanceof Seller) {
      sellers++;
    } else if (data instanceof Client) {
      clients++;
    } else if (data instanceof Sale) {

      var sale = (Sale) data;
      var saleItems = saleItemParser.getSaleItems(sale.getItemsRaw());

      var salesValue = saleItems.stream()
          .map(x -> (x.getPrice().multiply(new BigDecimal(x.getAmount()))))
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      int comparison = salesValue.compareTo(highestSale);

      if (comparison == 1) {
        highestSale = salesValue;
        highestSaleId = sale.getId();
      } else if (comparison == 0) {
        highestSale = salesValue;
        highestSaleId += sale.getId();
      }

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

    return SalesSummary.builder()
        .sellers(sellers)
        .clients(clients)
        .highestSaleId(highestSaleId)
        .lowestSalesSeller(lowestSalesSeller)
        .build();
  }
}
