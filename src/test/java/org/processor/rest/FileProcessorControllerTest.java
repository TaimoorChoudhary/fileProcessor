package org.processor.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.processor.service.FileProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(FileProcessorController.class)
public class FileProcessorControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  FileProcessorService fileProcessorService;

  @Test
  public void uploadFile_successful() throws Exception {
    when(fileProcessorService.saveFile(any())).thenReturn(true);

    var multipartFile = new MockMultipartFile(
        "file",
        "filename.txt",
        "text/plain",
        "some text".getBytes());

    var requestBuilder = MockMvcRequestBuilders
        .multipart("/file/upload")
        .file(multipartFile)
        .accept(MediaType.MULTIPART_FORM_DATA);

    mockMvc.perform(requestBuilder)
        .andExpect(content().string("File upload successful"))
        .andExpect(status().isOk());
  }

  @Test
  public void uploadFile_failed() throws Exception {
    when(fileProcessorService.saveFile(any())).thenReturn(false);

    var multipartFile = new MockMultipartFile(
        "file",
        "filename.txt",
        "text/plain",
        "some text".getBytes());

    var requestBuilder = MockMvcRequestBuilders
        .multipart("/file/upload")
        .file(multipartFile)
        .accept(MediaType.MULTIPART_FORM_DATA);

    mockMvc.perform(requestBuilder)
        .andExpect(content().string("Failed to upload file"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void uploadFile_invalid() throws Exception {
    var multipartFile = new MockMultipartFile(
        "file",
        "filename.csv",
        "text/plain",
        "some text".getBytes());

    var requestBuilder = MockMvcRequestBuilders
        .multipart("/file/upload")
        .file(multipartFile)
        .accept(MediaType.MULTIPART_FORM_DATA);

    mockMvc.perform(requestBuilder)
        .andExpect(content().string("File type not supported, please upload a txt file"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void getFileSummary_successful() throws Exception {
    when(fileProcessorService.getFileSummary(anyString())).thenReturn("FileSummaryString");

    var requestBuilder = MockMvcRequestBuilders.get(
        "/file/summary/test-file.txt").accept(
        MediaType.APPLICATION_JSON);

    var result = mockMvc.perform(requestBuilder)
        .andExpect(content().string("FileSummaryString"))
        .andExpect(status().isOk());
  }

  @Test
  public void getFileSummary_notFound() throws Exception {
    when(fileProcessorService.getFileSummary(anyString())).thenReturn("");

    var requestBuilder = MockMvcRequestBuilders.get(
        "/file/summary/test-file.txt").accept(
        MediaType.APPLICATION_JSON);

    var result = mockMvc.perform(requestBuilder)
        .andExpect(content().string("Unable to process request, summary file not found."))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void getFileSummary_errorFile() throws Exception {
    when(fileProcessorService.getFileSummary(anyString())).thenReturn("ERROR: file error");

    var requestBuilder = MockMvcRequestBuilders.get(
        "/file/summary/test-file.txt").accept(
        MediaType.APPLICATION_JSON);

    var result = mockMvc.perform(requestBuilder)
        .andExpect(content().string("ERROR: file error"))
        .andExpect(status().isInternalServerError());
  }

  @Test
  public void getFileSummary_exception() throws Exception {
    when(fileProcessorService.getFileSummary(anyString())).thenThrow(NullPointerException.class);

    var requestBuilder = MockMvcRequestBuilders.get(
        "/file/summary/test-file.txt").accept(
        MediaType.APPLICATION_JSON);

    var result = mockMvc.perform(requestBuilder)
        .andExpect(content().string("Unable to process request"))
        .andExpect(status().isInternalServerError());
  }
}
