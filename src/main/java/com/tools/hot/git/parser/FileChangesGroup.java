package com.tools.hot.git.parser;

import java.util.List;
import java.util.stream.Stream;

public final class FileChangesGroup {

  private final List<Change> changes;

  public FileChangesGroup(final List<Change> changes) {
    this.changes = changes;
  }

  public Stream<FileChanges> stream() {
    return changes.stream().collect(new FileChangesCollector()).stream();
  }
}
