package org.processor.batchjob;

import org.processor.model.SalesSummary;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;

/**
 * This is responsible for writing data to file.
 */
public class SingleLineAggregator extends PassThroughLineAggregator<SalesSummary> {

  /**
   * Skips writing incoming data to file and return empty string
   * as we only want file summary to be written at the end.
   *
   * @param salesSummary file summary
   * @return empty String
   */
  @Override
  public String aggregate(SalesSummary salesSummary) {

    return "";
  }

}
