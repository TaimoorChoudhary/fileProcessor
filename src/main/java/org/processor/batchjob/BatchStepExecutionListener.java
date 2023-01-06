package org.processor.batchjob;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchStepExecutionListener implements StepExecutionListener {

  @Override
  public void beforeStep(StepExecution stepExecution) {

  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    List<Throwable> failureExceptions = stepExecution.getFailureExceptions();

    for (var exception : failureExceptions) {
      log.error(exception.getMessage());
    }

    return null;
  }

}
