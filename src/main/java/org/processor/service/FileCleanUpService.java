package org.processor.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

/**
 * Cleans up data files after processing.
 */
@Slf4j
@Service
public class FileCleanUpService implements Tasklet, InitializingBean {

  @Value("${batchJob.output:output}")
  private String outputFolder;

  public static ConcurrentMap<String, String> inputFiles;
  public static ConcurrentMap<String, String> outputFiles;
  public static ConcurrentMap<String, String> errorFiles;

  static {
    inputFiles = new ConcurrentHashMap<>();
    outputFiles = new ConcurrentHashMap<>();
    errorFiles = new ConcurrentHashMap<>();
  }

  /**
   * Start file clean up process.
   *
   * @param contribution StepContribution
   * @param chunkContext ChunkContext
   * @return RepeatStatus
   */
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

    // Remove processed input files
    inputFiles.forEach((name, path) -> deleteFile(new File(path)));

    outputFiles.forEach((inputFile, outputFile) -> {

      if (errorFiles.containsKey(inputFile)) {

        // Remove empty output file corresponding to input file with error
        var fr = new FileSystemResource(outputFolder + "/" + outputFile);
        deleteFile(fr.getFile());

        // Move input file with errors to separate folder
        moveFile(inputFile, errorFiles.get(inputFile));
      }
    });

    // Clear maps for next run
    inputFiles.clear();
    outputFiles.clear();
    errorFiles.clear();

    return RepeatStatus.FINISHED;
  }

  @Override
  public void afterPropertiesSet() throws Exception {

  }

  private void deleteFile(File file) {

    if (file.exists()) {
      boolean deleted = file.delete();
      if (!deleted) {
        log.error("Could not delete file " + file.getPath());
      }
    }
  }

  private void moveFile(String fileName, String filePath) {

    try {

      // Create directory for error files if it does not exist
      new File("errorFiles").mkdirs();

      Files.move(Paths.get(filePath), Paths.get("errorFiles/" + fileName),
          StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }
}
