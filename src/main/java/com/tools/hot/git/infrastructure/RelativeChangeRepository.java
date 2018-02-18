package com.tools.hot.git.infrastructure;

import com.tools.hot.git.parser.RelativeChanges;

public final class RelativeChangeRepository {

  private RelativeChanges relativeChanges;

  public void save(final RelativeChanges relativeChanges) {
    this.relativeChanges = relativeChanges;
  }

  public RelativeChanges findAll() {
    return relativeChanges;
  }
}
