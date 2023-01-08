package org.processor.batchjob;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import lombok.extern.slf4j.Slf4j;
import org.processor.model.SalesSummary;
import org.processor.service.FileCleanUpService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Contains configuration for Batch job.
 */
@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchJobConfiguration {

  @Autowired
  public JobBuilderFactory jobBuilderFactory;

  @Autowired
  public StepBuilderFactory stepBuilderFactory;

  @Autowired
  private FlatFileItemReader<Object> fileItemReader;

  @Autowired
  private FlatFileItemWriter<SalesSummary> fileItemWriter;

  @Autowired
  private FileCleanUpService fileCleanUpService;

  @Autowired
  private BatchStepExecutionListener batchStepExecutionListener;

  @Value("${batchJob.input:input}")
  private String inputFolder;

  @Value("${batchJob.output:output}")
  private String outputFolder;

  @Bean
  public Job fileProcessingJob(Step fileProcessorStep) {
    return jobBuilderFactory.get("fileProcessingJob")
        .incrementer(new RunIdIncrementer())
        .flow(masterStep())
        .on("*")
        .to(fileCleanUpStep())
        .end()
        .build();
  }
  @Bean("partitioner")
  @StepScope
  public Partitioner partitioner() {
    log.info("Partitioner Started");

    var partitioner = new MultiResourcePartitioner();
    var resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = null;
    try {

      resources = resolver.getResources("file:" + inputFolder + "/*.txt");
    } catch (IOException e) {
      log.error("Error while starting partitioner" + e.getMessage());
    }
    partitioner.setResources(resources);
    partitioner.partition(10);
    return partitioner;
  }

  @Bean
  @StepScope
  public FileItemProcessor processor() {
    return new FileItemProcessor();
  }

  @Bean
  public Step fileProcessorStep() {
    return stepBuilderFactory.get("fileProcessorStep")
        .<Object, SalesSummary>chunk(10)
        .reader(fileItemReader)
        .processor(processor())
        .writer(fileItemWriter)
        .build();
  }

  @Bean
  public Step fileCleanUpStep() {
    return stepBuilderFactory.get("fileCleanUpStep")
        .tasklet(fileCleanUpService)
        .build();
  }

  @Bean
  public ThreadPoolTaskExecutor taskExecutor() {
    var taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setMaxPoolSize(10);
    taskExecutor.setCorePoolSize(10);
    taskExecutor.setQueueCapacity(10);
    taskExecutor.afterPropertiesSet();
    return taskExecutor;
  }

  @Bean
  @Qualifier("masterStep")
  public Step masterStep() {
    return stepBuilderFactory.get("masterStep")
        .partitioner("fileProcessorStep", partitioner())
        .step(fileProcessorStep())
        .listener(batchStepExecutionListener)
        .taskExecutor(taskExecutor())
        .build();
  }

  @Bean
  @StepScope
  @Qualifier("fileItemReader")
  @DependsOn("partitioner")
  public FlatFileItemReader<Object> fileItemReader(
      @Value("#{stepExecutionContext['fileName']}") String filename)
      throws MalformedURLException {
    log.info("Reader started");

    // Create reader instance
    var fileReader = new FileItemReader();
    return fileReader.createReader(filename);
  }

  @Bean
  @StepScope
  @Qualifier("fileItemWriter")
  @DependsOn("partitioner")
  public FlatFileItemWriter<SalesSummary> fileItemWriter(
      @Value("#{stepExecutionContext[fileName]}") String filename) {

    var name = filename.replace("file:", "");
    var file = new File(name);

    //Create writer instance
    var writer = new FileItemWriter(file);
    writer.setFooterCallback(writer);

    // Set output file location
    var outputFileName = writer.removeFileExtension(file.getName()) + ".done.txt";
    var outputResource = new FileSystemResource(outputFolder + "/" + outputFileName);
    writer.setResource(outputResource);

    // Keep track of output file for corresponding input file
    FileCleanUpService.outputFiles.put(file.getName(), outputFileName);

    //All job repetitions should "re-write" to same output file (if name matches)
    writer.setAppendAllowed(false);
    writer.setLineSeparator("");
    writer.setLineAggregator(new SingleLineAggregator());
    return writer;
  }
}
