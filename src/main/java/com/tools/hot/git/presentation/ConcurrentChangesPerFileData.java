package com.tools.hot.git.presentation;

public class ConcurrentChangesPerFileData {

  private final String fileName;
  private final Long concurrentChangesCount;

  public ConcurrentChangesPerFileData(final String fileName, final Long concurrentChangesCount) {
    this.fileName = fileName;
    this.concurrentChangesCount = concurrentChangesCount;
  }

  public String getFileName() {
    return fileName;
  }

  public Long getConcurrentChangesCount() {
    return concurrentChangesCount;
  }
}
