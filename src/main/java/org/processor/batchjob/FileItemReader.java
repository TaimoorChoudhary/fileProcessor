package org.processor.batchjob;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import org.processor.utility.DataType;
import org.processor.model.Client;
import org.processor.model.Sale;
import org.processor.model.Seller;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.UrlResource;

public class FileItemReader {

  public FlatFileItemReader<Object> createReader(String fileName) throws MalformedURLException {

    return new FlatFileItemReaderBuilder<Object>().name("personItemReader")
        .lineMapper(lineMapper())
        .resource(new UrlResource(fileName))
        .build();
  }

  public PatternMatchingCompositeLineMapper<Object> lineMapper() {
    var mapper = new PatternMatchingCompositeLineMapper<>();

    mapper.setTokenizers(Map.of(
        DataType.SELLER.getCode() + "*", sellerTokenizer(),
        DataType.CLIENT.getCode() + "*", clientTokenizer(),
        DataType.SALE.getCode() + "*", saleTokenizer()
    ));

    Map<String, FieldSetMapper<Object>> mappers = new HashMap<>(2);
    mappers.put(DataType.SELLER.getCode() + "*", sellerFieldSetMapper());
    mappers.put(DataType.CLIENT.getCode() + "*", clientFieldSetMapper());
    mappers.put(DataType.SALE.getCode() + "*", saleFieldSetMapper());

    mapper.setFieldSetMappers(mappers);

    return mapper;
  }

  private DelimitedLineTokenizer sellerTokenizer() {
    return new DelimitedLineTokenizer() {
      {
        setNames("code", "name");
      }
    };
  }

  private DelimitedLineTokenizer clientTokenizer() {
    return new DelimitedLineTokenizer() {
      {
        setNames("code", "name", "profession");
      }
    };
  }

  private DelimitedLineTokenizer saleTokenizer() {
    return new DelimitedLineTokenizer() {
      {
        setNames("code", "id", "itemsRaw", "sellerName");
      }
    };
  }

  private BeanWrapperFieldSetMapper<Object> sellerFieldSetMapper() {

    return new BeanWrapperFieldSetMapper<Object>() {
      {
        setTargetType(Seller.class);
      }
    };
  }

  private BeanWrapperFieldSetMapper<Object> clientFieldSetMapper() {

    return new BeanWrapperFieldSetMapper<Object>() {
      {
        setTargetType(Client.class);
      }
    };
  }

  private BeanWrapperFieldSetMapper<Object> saleFieldSetMapper() {

    return new BeanWrapperFieldSetMapper<Object>() {
      {
        setTargetType(Sale.class);
      }
    };
  }
}
