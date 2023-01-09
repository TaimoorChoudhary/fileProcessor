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
import org.springframework.util.FileSystemUtils;

@SpringBootTest
public class FileUploadUtilityTest {

  @Value("${batchJob.input:input}")
  private String inputFolder;

  @Autowired
  FileUploadUtility fileUploadUtility;

  @AfterEach
  public void cleanUpEach() {
    FileSystemUtils.deleteRecursively(new File(inputFolder));
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
