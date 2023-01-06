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
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchJobConfiguration {

  @Autowired
  public JobBuilderFactory jobBuilderFactory;

  @Autowired
  public StepBuilderFactory stepBuilderFactory;

  @Autowired
  private FlatFileItemReader<Object> personItemReader;

  @Autowired
  private FlatFileItemWriter<SalesSummary> personItemWriter;

  @Autowired
  private FileCleanUpService fileCleanUpService;

  @Autowired
  private BatchStepExecutionListener batchStepExecutionListener;

  @Value("${batchJob.input:input}")
  private String inputFolder;

  @Value("${batchJob.output:output}")
  private String outputFolder;

  @Bean
  public Job importUserJob(Step step1) {
    return jobBuilderFactory.get("importUserJob")
        .incrementer(new RunIdIncrementer())
        .flow(masterStep())
        .on("*")
        .to(step2())
        .end()
        .build();
  }
  @Bean("partitioner")
  @StepScope
  public Partitioner partitioner() {
    log.info("In Partitioner");

    MultiResourcePartitioner partitioner = new MultiResourcePartitioner();
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = null;
    try {

      resources = resolver.getResources("file:" + inputFolder + "/*.txt");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
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
  public Step step1() {
    return stepBuilderFactory.get("step1")
        .<Object, SalesSummary>chunk(10)
        .reader(personItemReader)
        .processor(processor())
        .writer(personItemWriter)
        .build();
  }

  @Bean
  public Step step2() {
    return stepBuilderFactory.get("step2")
        .tasklet(fileCleanUpService)
        .build();
  }

  @Bean
  public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
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
        .partitioner("step1", partitioner())
        .step(step1())
        .listener(batchStepExecutionListener)
        .taskExecutor(taskExecutor())
        .build();
  }

  @Bean
  @StepScope
  @Qualifier("personItemReader")
  @DependsOn("partitioner")
  public FlatFileItemReader<Object> personItemReader(@Value("#{stepExecutionContext['fileName']}") String filename)
      throws MalformedURLException {
    log.info("In Reader");
    var fileReader = new FileItemReader();
    return fileReader.createReader(filename);
  }

  @Bean
  @StepScope
  @Qualifier("personItemWriter")
  @DependsOn("partitioner")
  public FlatFileItemWriter<SalesSummary> personItemWriter(@Value("#{stepExecutionContext[fileName]}") String filename) {

    String name = filename.replace("file:", "");
    var file = new File(name);

    //Create writer instance
    FileItemWriter writer = new FileItemWriter(file);
    writer.setFooterCallback(writer);

    String outputFileName = writer.removeFileExtension(file.getName()) + ".done.txt";
    FileSystemResource outputResource = new FileSystemResource(outputFolder + "/" + outputFileName);

    //Set output file location
    writer.setResource(outputResource);

    FileCleanUpService.outputFiles.put(file.getName(), outputFileName);

    //All job repetitions should "append" to same output file
    writer.setAppendAllowed(false);
    writer.setLineSeparator("");
    writer.setLineAggregator(new SingleLineAggregator());
    return writer;
  }
}
