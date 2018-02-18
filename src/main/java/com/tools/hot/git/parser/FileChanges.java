package com.tools.hot.git.parser;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.jooq.lambda.Seq;

public final class FileChanges {

  private final String file;
  private final List<Instant> changeDates;

  FileChanges(final String file, final List<Instant> changeDates) {
    this.file = file;
    this.changeDates = changeDates;
  }

  public List<RelativeChange> relativeChanges() {
    return Seq.seq(changeDates).sorted().sliding(2).map(seq -> {
      final Instant[] changeTimes = seq.toArray(Instant[]::new);
      final Instant first = changeTimes[0];
      final Instant second = changeTimes[1];
      final Duration duration = Duration.between(first, second);
      return new RelativeChange(file, second, duration);
    }).toList();
  }

  public void addFileChange(Change fileChange) {
    changeDates.add(fileChange.changeDate());
  }

  public FileChanges merge(FileChanges fileChanges) {
    if (file.equals(fileChanges.file)) {
      final List<Instant> combinedChangeDates =
          concat(changeDates.stream(), fileChanges.changeDates.stream()).collect(toList());
      return new FileChanges(file, combinedChangeDates);
    } else {
      return new FileChanges(file, changeDates);
    }
  }
}
