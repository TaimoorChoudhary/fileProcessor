package org.processor.rest;


import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.processor.service.FileProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping()
public class FileProcessorController {

  @Autowired
  FileProcessorService fileProcessorService;

  @PostMapping("/uploadFile")
  public ResponseEntity<String> uploadFile(
      @RequestParam("file") MultipartFile multipartFile)
      throws IOException {

    log.info("in controller");
    String fileExtensions = ".txt";
    String fileName = multipartFile.getOriginalFilename();
    int lastIndex = fileName.lastIndexOf('.');
    String substring = fileName.substring(lastIndex, fileName.length());

    if (!fileExtensions.contains(substring.toLowerCase())) {
      return
          new ResponseEntity<>("File type not supported, please upload a txt file",
              HttpStatus.BAD_REQUEST);
    } else {
      boolean uploadFile = fileProcessorService.uploadFile(multipartFile);

      if (uploadFile) {
        return new ResponseEntity<>("File upload successful", HttpStatus.OK);
      } else {
        return new ResponseEntity<>("Failed to upload file",
            HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
  }

  @GetMapping("/{fileName}")
  public ResponseEntity<String> getSummary(@PathVariable("fileName") String fileName) {

    try {
      String fileSummary = fileProcessorService.getFileSummary(fileName);

      if (fileSummary.isBlank() || fileSummary.contains("ERROR:")) {
        return new ResponseEntity<>(
            fileSummary.isBlank() ? "Unable to process request, summary file not found"
                : fileSummary,
            HttpStatus.INTERNAL_SERVER_ERROR);
      } else  {
        return new ResponseEntity<>(fileSummary, HttpStatus.OK);
      }
    } catch (Exception e) {
      return new ResponseEntity<>("Unable to process request",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
