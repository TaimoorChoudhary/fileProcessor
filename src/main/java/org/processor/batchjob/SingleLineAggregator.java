package org.processor.batchjob;

import org.processor.model.SalesSummary;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;

public class SingleLineAggregator extends PassThroughLineAggregator<SalesSummary> {

  @Override
  public  String aggregate(SalesSummary subrogration) {

    StringBuilder result = new StringBuilder();


    //logic

    return result.toString();

  }

}
