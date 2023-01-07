package org.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Application entry point
 */
@Slf4j
@SpringBootApplication
@EnableScheduling
public class Main {

  @Autowired
  JobLauncher jobLauncher;

  @Autowired
  Job job;

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }

  /**
   * Scheduler for batch job.
   */
  @Scheduled(cron = "${batchJob.cron:0 */2 * * * ?}")
  public void perform() {
    JobParameters params = new JobParametersBuilder()
        .addString("JobID", String.valueOf(System.currentTimeMillis()))
        .toJobParameters();
    try {
      jobLauncher.run(job, params);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
  }
}