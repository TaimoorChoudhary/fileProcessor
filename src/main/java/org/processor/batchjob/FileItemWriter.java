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

public class FileItemWriter extends FlatFileItemWriter<SalesSummary> implements FlatFileFooterCallback {

  private File file;
  private SalesSummary salesSummary = new SalesSummary();

  public FileItemWriter(File file) {
    this.file = file;
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

  public String removeFileExtension(String filename) {
    if (filename == null || filename.isEmpty()) {
      return filename;
    }

    String extPattern = "(?<!^)[.]" + "[^.]*$";
    return filename.replaceAll(extPattern, "");
  }
}
