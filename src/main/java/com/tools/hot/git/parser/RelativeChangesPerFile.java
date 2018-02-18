package com.tools.hot.git.parser;

import static java.util.stream.Collectors.groupingBy;

import java.util.List;
import java.util.Map;

public class RelativeChangesPerFile {

  private final RelativeChanges relativeChanges;

  public RelativeChangesPerFile(final RelativeChanges relativeChanges) {
    this.relativeChanges = relativeChanges;
  }

  public Map<String, List<RelativeChange>> toMap() {
    return relativeChanges.stream().collect(groupingBy(RelativeChange::file));
  }
}
