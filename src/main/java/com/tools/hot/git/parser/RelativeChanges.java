package com.tools.hot.git.parser;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectReader;

public class RelativeChanges {

  private final ObjectReader objectReader;
  private final Git git;

  public RelativeChanges(final ObjectReader objectReader, final Git git) {
    this.objectReader = objectReader;
    this.git = git;
  }

  public Stream<RelativeChange> stream() {
    final CommitTreeParserFactory parserFactory = new CommitTreeParserFactory(objectReader);
    final List<Change> changes = new Commits(git).stream()
        .map(commit -> new DiffWithParent(commit, parserFactory, git))
        .map(DiffWithParent::changes)
        .flatMap(Collection::stream)
        .collect(toList());
    return new FileChangesGroup(changes).stream()
        .map(FileChanges::relativeChanges)
        .flatMap(Collection::stream);
  }
}
