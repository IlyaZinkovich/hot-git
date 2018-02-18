package com.tools.hot.git.infrastructure;

import com.tools.hot.git.parser.RelativeChange;
import java.util.List;

public class RelativeChangeRepository {

  private List<RelativeChange> relativeChanges;

  public void save(List<RelativeChange> relativeChanges) {
    this.relativeChanges = relativeChanges;
  }
}
