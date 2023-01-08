package org.processor.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.processor.utility.FileUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;

@SpringBootTest
public class FileCleanUpServiceTest {

  @Autowired
  FileCleanUpService fileCleanUpService;

  @Value("${batchJob.input:input}")
  private String inputFolder;
  @Value("${batchJob.output:output}")
  private String outputFolder;
  @Value("${batchJob.error:errorFiles}")
  private String errorFolder;

  private final FileUtility fileUtility = new FileUtility();

  @AfterEach
  public void cleanUpEach() {
    FileSystemUtils.deleteRecursively(new File(inputFolder));
    FileSystemUtils.deleteRecursively(new File(outputFolder));
    FileSystemUtils.deleteRecursively(new File(errorFolder));
  }

  @Test
  public void fileCleanupWithErrorFiles() {

    var multipartFile = new MockMultipartFile(
        "data",
        "filename.txt",
        "text/plain",
        "some text".getBytes());

    fileUtility.saveFile("testFile.txt", multipartFile, inputFolder);
    fileUtility.saveFile("testFile.done.txt", multipartFile, outputFolder);
    fileUtility.saveFile("testFile2.done.txt", multipartFile, outputFolder);
    fileUtility.saveFile("testFile2.txt", multipartFile, inputFolder);

    FileCleanUpService.inputFiles.put("testFile.txt", inputFolder + "/testFile.txt");
    FileCleanUpService.outputFiles.put("testFile.txt", "testFile.done.txt");
    FileCleanUpService.outputFiles.put("testFile2.txt", "testFile2.done.txt");
    FileCleanUpService.errorFiles.put("testFile2.txt", inputFolder + "/testFile2.txt");

    var inputFile = new File(inputFolder + "/testFile.txt");
    var inputFile2 = new File(inputFolder + "/testFile2.txt");
    var outputFile = new File(outputFolder + "/testFile.done.txt");
    var outputFile2 = new File(outputFolder + "/testFile2.done.txt");

    assertTrue(inputFile.exists());
    assertTrue(inputFile2.exists());
    assertTrue(outputFile.exists());
    assertTrue(outputFile2.exists());

    try {
      fileCleanUpService.execute(null, null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    var errorFile = new File(errorFolder + "/testFile2.txt");

    assertFalse(inputFile.exists());
    assertFalse(inputFile2.exists());
    assertTrue(outputFile.exists());
    assertFalse(outputFile2.exists());
    assertTrue(errorFile.exists());
  }
}
