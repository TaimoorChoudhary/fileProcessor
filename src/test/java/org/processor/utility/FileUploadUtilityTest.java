package org.processor.utility;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest
public class FileUploadUtilityTest {

  @Value("${batchJob.input:input}")
  private String inputFolder;

  @Autowired
  FileUploadUtility fileUploadUtility;

  @AfterEach
  public void cleanUpEach() {
    var file = new File(inputFolder + "/testFile.txt");
    if (file.exists()) {
      file.delete();
    }
  }

  @Test
  public void parseSalesItems_successful() throws IOException {

    var multipartFile = new MockMultipartFile(
        "data",
        "filename.txt",
        "text/plain",
        "some text".getBytes());

    fileUploadUtility.saveFile("testFile.txt", multipartFile);

    var file = new File(inputFolder + "/testFile.txt");

    assertTrue(file.exists());
  }
}
