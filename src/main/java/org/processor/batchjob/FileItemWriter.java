package org.processor.batchjob;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.processor.model.SalesSummary;
import org.processor.service.FileCleanUpService;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;

/**
 * File item writer for file summary.
 */
public class FileItemWriter extends FlatFileItemWriter<SalesSummary>
    implements FlatFileFooterCallback {

  private final File file;
  private SalesSummary salesSummary;

  public FileItemWriter(File file) {
    this.file = file;
    salesSummary = new SalesSummary();
  }

  @Override
  public String doWrite(List<? extends SalesSummary> items) {
    salesSummary = items.get(items.size() - 1);
    return StringUtils.EMPTY;
  }

  @Override
  public void writeFooter(Writer writer) throws IOException {
    if (!salesSummary.isEmpty()) {
      writer.write(salesSummary.toString());
      FileCleanUpService.inputFiles.put(file.getName(), file.getCanonicalPath());
    } else {
      FileCleanUpService.errorFiles.put(file.getName(), file.getCanonicalPath());
    }
  }

  /**
   * Removes file extension from file name.
   *
   * @param filename name from which to remove extension
   * @return file name without extension
   */
  public String removeFileExtension(String filename) {
    if (filename == null || filename.isEmpty()) {
      return filename;
    }

    var extPattern = "(?<!^)[.]" + "[^.]*$";
    return filename.replaceAll(extPattern, "");
  }
}
