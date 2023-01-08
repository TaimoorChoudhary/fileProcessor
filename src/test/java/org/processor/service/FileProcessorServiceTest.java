package org.processor.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.processor.utility.FileUploadUtility;
import org.processor.utility.FileUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;

@SpringBootTest
public class FileProcessorServiceTest {

  @Value("${batchJob.output:output}")
  private String outputFolder;
  @Value("${batchJob.error:errorFiles}")
  private String errorFolder;

  @MockBean
  FileUploadUtility fileUploadUtility;

  @Autowired
  FileProcessorService fileProcessorService;

  private final FileUtility fileUtility = new FileUtility();

  @AfterEach
  public void cleanUpEach() {
    FileSystemUtils.deleteRecursively(new File(outputFolder));
    FileSystemUtils.deleteRecursively(new File(errorFolder));
  }

  @Test
  public void saveFile_successful() {
    var multipartFile = new MockMultipartFile(
        "file",
        "filename.txt",
        "text/plain",
        "some text".getBytes());

    var fileSaved = fileProcessorService.saveFile(multipartFile);

    assertTrue(fileSaved);
  }

  @Test
  public void saveFile_failed() throws IOException {
    var multipartFile = new MockMultipartFile(
        "file",
        "filename.txt",
        "text/plain",
        "some text".getBytes());

    doThrow(IOException.class).when(fileUploadUtility).saveFile("filename.txt", multipartFile);

    var fileSaved = fileProcessorService.saveFile(multipartFile);

    assertFalse(fileSaved);
  }

  @Test
  public void getSummary_successful() throws IOException {
    var multipartFile = new MockMultipartFile(
        "file",
        "test-file.done.txt",
        "text/plain",
        "File Summary".getBytes());

    fileUtility.saveFile("test-file.done.txt", multipartFile, outputFolder);

    var summary = fileProcessorService.getFileSummary("test-file.txt");

    assertEquals("File Summary", summary);
  }

  @Test
  public void getSummary_errorFile() throws IOException {
    var multipartFile = new MockMultipartFile(
        "file",
        "test-file.txt",
        "text/plain",
        "File Summary".getBytes());

    fileUtility.saveFile("test-file.txt", multipartFile, errorFolder);

    var summary = fileProcessorService.getFileSummary("test-file.txt");

    assertThat(summary, containsString("ERROR: There was an error processing the file"));
  }

  @Test
  public void getSummary_notFound() throws IOException {
    var summary = fileProcessorService.getFileSummary("test-file.txt");

    assertTrue(summary.isBlank());
  }
}
