package com.tools.hot.git.parser;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.time.Duration;
import java.util.Map;

public class ConcurrentChangesPerFile {

  private final Duration concurrencyDuration;
  private final RelativeChanges relativeChanges;

  public ConcurrentChangesPerFile(final RelativeChanges relativeChanges,
      final Duration concurrencyDuration) {
    this.relativeChanges = relativeChanges;
    this.concurrencyDuration = concurrencyDuration;
  }

  public Map<String, Long> toMap() {
    return relativeChanges.stream()
        .filter(relativeChange -> relativeChange.within(concurrencyDuration))
        .collect(groupingBy(RelativeChange::file, counting()));
  }
}
