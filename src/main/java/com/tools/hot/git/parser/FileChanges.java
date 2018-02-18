package com.tools.hot.git.parser;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.jooq.lambda.Seq;

public final class FileChanges {

  private final String file;
  private final List<Instant> changeDates;

  public FileChanges(final String file, final List<Instant> changeDates) {
    this.file = file;
    this.changeDates = changeDates;
  }

  public Stream<RelativeChange> relativeChanges() {
    return Seq.seq(changeDates).sorted().sliding(2).map(seq -> {
      final Instant[] changeTimes = seq.toArray(Instant[]::new);
      final Instant first = changeTimes[0];
      final Instant second = changeTimes[1];
      final Duration duration = Duration.between(first, second);
      return new RelativeChange(file, second, duration);
    });
  }
}
