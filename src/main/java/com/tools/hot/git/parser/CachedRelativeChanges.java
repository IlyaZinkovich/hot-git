package com.tools.hot.git.parser;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectReader;

public class CachedRelativeChanges extends RelativeChanges {

  private List<RelativeChange> cache;

  public CachedRelativeChanges(ObjectReader objectReader, Git git) {
    super(objectReader, git);
  }

  @Override
  public Stream<RelativeChange> stream() {
    if (cache == null) {
      cache = super.stream().collect(toList());
    }
    return cache.stream();
  }
}
