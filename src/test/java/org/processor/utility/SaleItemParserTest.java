package org.processor.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.processor.model.SaleItem;
import org.springframework.batch.item.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SaleItemParserTest {

  @Autowired
  private SaleItemParser saleItemParser;

  @Test
  public void parseSalesItems_successful() {

    List<SaleItem> saleItems = saleItemParser.getSaleItems("[1-10-100;2-30-2.50;3-40-3.10]");


    assertEquals(3, saleItems.size());
    assertEquals("1", saleItems.get(0).getId());
    assertEquals(10, saleItems.get(0).getAmount());
    assertEquals(BigDecimal.valueOf(100), saleItems.get(0).getPrice());
  }

  @Test
  public void parseSalesItems_invalidFormat() {

    var exception = Assertions.assertThrows(ParseException.class, () -> {
      saleItemParser.getSaleItems("[1-10100;2-30-2.50;3-40-3.10]");
    });

    Assertions.assertEquals("Unable to parse sale items, invalid format", exception.getMessage());
  }
}
