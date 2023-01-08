package org.processor.utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles file uploading to input folder.
 */
@Slf4j
@Service
public class FileUploadUtility {

  @Value("${batchJob.input:input}")
  private String inputFolder;

  /**
   * Save incoming file.
   *
   * @param fileName input file name
   * @param multipartFile incoming file
   * @throws IOException exception while saving file
   */
  public void saveFile(String fileName, MultipartFile multipartFile)
      throws IOException {
    new File(inputFolder).mkdirs();
    var uploadPath = Paths.get(inputFolder);

    try (InputStream inputStream = multipartFile.getInputStream()) {
      var filePath = uploadPath.resolve(fileName);
      Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException ioe) {
      throw new IOException("Could not save file: " + fileName, ioe);
    }
  }
}
