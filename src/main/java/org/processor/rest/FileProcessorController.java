package org.processor.rest;


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

/**
 * Rest controller handles file upload and summary retrieval.
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileProcessorController {

  @Autowired
  FileProcessorService fileProcessorService;

  /**
   * Allows file to be uploaded into input folder.
   *
   * @param multipartFile file to upload
   * @return ResponseEntity
   */
  @PostMapping("/upload")
  public ResponseEntity<String> uploadFile(
      @RequestParam("file") MultipartFile multipartFile) {
    var fileExtensions = ".txt";
    var fileName = multipartFile.getOriginalFilename();
    var lastIndex = fileName.lastIndexOf('.');
    var substring = fileName.substring(lastIndex, fileName.length());

    log.info("File received for upload: " + fileName);

    if (!fileExtensions.contains(substring.toLowerCase())) {
      log.debug("File extension not supported");

      return
          new ResponseEntity<>("File type not supported, please upload a txt file",
              HttpStatus.BAD_REQUEST);
    } else {
      boolean uploadFile = fileProcessorService.saveFile(multipartFile);

      if (uploadFile) {

        log.debug("File upload successful");
        return new ResponseEntity<>("File upload successful", HttpStatus.OK);
      } else {

        log.debug("Failed to upload file");
        return new ResponseEntity<>("Failed to upload file",
            HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
  }

  /**
   * Get file summary for provided file name.
   *
   * @param fileName input file name
   * @return ResponseEntity
   */
  @GetMapping("/summary/{fileName}")
  public ResponseEntity<String> getSummary(@PathVariable("fileName") String fileName) {
    try {
      var fileSummary = fileProcessorService.getFileSummary(fileName);

      if (fileSummary.isBlank() || fileSummary.contains("ERROR:")) {

        var message = fileSummary.isBlank()
            ? "Unable to process request, summary file not found."
            : fileSummary;

        log.debug(message);

        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
      } else  {

        return new ResponseEntity<>(fileSummary, HttpStatus.OK);
      }
    } catch (Exception e) {

      log.error("Exception while processing file summary request: " + e.getMessage());
      return new ResponseEntity<>("Unable to process request",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
