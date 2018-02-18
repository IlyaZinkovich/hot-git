package com.tools.hot.git.parser;

import java.time.Instant;

public final class Change {

  private final String file;
  private final Instant changeDate;

  Change(final String file, final Instant changeDate) {
    this.file = file;
    this.changeDate = changeDate;
  }

  public String file() {
    return file;
  }

  public Instant changeDate() {
    return changeDate;
  }
}
