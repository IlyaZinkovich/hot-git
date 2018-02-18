package com.tools.hot.git.parser;

import static java.util.stream.Collectors.toMap;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;

public final class DiffWithParent {

  private final RevCommit commit;
  private final CommitTreeParserFactory factory;
  private final Git git;

  public DiffWithParent(final RevCommit commit, final CommitTreeParserFactory factory,
      final Git git) {
    this.commit = commit;
    this.factory = factory;
    this.git = git;
  }

  public Map<String, Instant> fileChangeDates() {
    return toList().stream()
        .map(DiffEntry::getNewPath)
        .distinct()
        .collect(toMap(path -> path, path -> commit.getAuthorIdent().getWhen().toInstant()));
  }

  private List<DiffEntry> toList() {
    try {
      return git.diff()
          .setNewTree(factory.commitTreeParser(commit))
          .setOldTree(factory.parentCommitTreeParser(commit))
          .call();
    } catch (GitAPIException e) {
      throw new DiffException(e);
    }
  }
}
