package org.processor.batchjob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.processor.model.Client;
import org.processor.model.Sale;
import org.processor.model.SalesSummary;
import org.processor.model.Seller;
import org.processor.utility.DataType;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FileItemProcessorTest {

  private FileItemProcessor fileItemProcessor;

  @BeforeEach
  public void setUp() {
    fileItemProcessor = new FileItemProcessor();
  }

  @AfterEach
  public void cleanUpEach() {
  }

  @Test
  public void fileItemProcessorSellerCount() throws Exception {

    var seller = Seller.builder()
        .code(DataType.SELLER.getCode())
        .name("testSeller")
        .build();

    var seller2 = Seller.builder()
        .code(DataType.SELLER.getCode())
        .name("testSeller2")
        .build();

    fileItemProcessor.process(seller);
    SalesSummary salesSummary = fileItemProcessor.process(seller2);

    assertEquals(2, salesSummary.getSellers());
  }

  @Test
  public void fileItemProcessorClientCount() throws Exception {

    var client = Client.builder()
        .code(DataType.CLIENT.getCode())
        .name("testClient")
        .build();

    var client2 = Client.builder()
        .code(DataType.CLIENT.getCode())
        .name("testClient2")
        .build();

    fileItemProcessor.process(client);
    SalesSummary salesSummary = fileItemProcessor.process(client2);

    assertEquals(2, salesSummary.getClients());
  }

  @Test
  public void fileItemProcessorHighestSalesId() throws Exception {

    var sale = Sale.builder()
        .id("01")
        .itemsRaw("[1-10-100;2-30-2.50;3-40-3.10]")
        .build();
    var sale2 = Sale.builder()
        .id("02")
        .itemsRaw("[1-27-100;2-15-2.50]")
        .build();

    var sale3 = Sale.builder()
        .id("03")
        .itemsRaw("[1-27-10;2-25-2.50]")
        .build();

    fileItemProcessor.process(sale);
    fileItemProcessor.process(sale2);
    SalesSummary salesSummary = fileItemProcessor.process(sale3);

    assertEquals("02", salesSummary.getHighestSaleId());
  }
}
