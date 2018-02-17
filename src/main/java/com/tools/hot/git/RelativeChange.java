package com.tools.hot.git;

import java.time.Duration;
import java.time.Instant;

public class RelativeChange {

  private final String file;
  private final Instant date;
  private final Duration durationSinceLastChange;

  public RelativeChange(final String file, final Instant date,
      final Duration durationSinceLastChange) {
    this.file = file;
    this.date = date;
    this.durationSinceLastChange = durationSinceLastChange;
  }

  public boolean within(Duration duration) {
    return durationSinceLastChange.minus(duration).isNegative();
  }

  public String file() {
    return file;
  }
}
