package org.processor.batchjob;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Listener provides access context before and after step execution.
 */
@Slf4j
@Component
public class BatchStepExecutionListener implements StepExecutionListener {

  @Override
  public void beforeStep(StepExecution stepExecution) {

  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    var failureExceptions = stepExecution.getFailureExceptions();

    for (var exception : failureExceptions) {
      log.error("Error during step executions: " + exception.getMessage());
    }

    return null;
  }

}
