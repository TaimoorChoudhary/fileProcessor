package org.processor.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.processor.utility.FileUploadUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FileProcessorService {

  @Value("${batchJob.output:output}")
  private String outputFolder;

  @Value("${batchJob.error:errorFiles}")
  private String errorFolder;

  @Autowired
  FileUploadUtility fileUploadUtility;

  public boolean uploadFile(MultipartFile multipartFile) {

    String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

    try {
      fileUploadUtility.saveFile(fileName, multipartFile);
      return true;
    } catch (IOException e) {
      log.error(e.getMessage());
      return false;
    }
  }

  public String getFileSummary(String fileName) throws FileNotFoundException {

    var file = new File(outputFolder + "/" + fileName.replace(".txt", ".done.txt"));

    if (file.exists()) {
      BufferedReader br = new BufferedReader(new FileReader(file));

      try {
        // reading a single line as the output files have summary written in one line only
        return br.readLine();
      } catch (IOException e) {
        log.error(e.getMessage());
        return "ERROR: Error reading file summary";
      }

    } else {
      file = new File(errorFolder + "/" + fileName);

      if (file.exists()) {
        return "ERROR: There was an error processing the file, summary is not available";
      }
    }

    return "";
  }
}
