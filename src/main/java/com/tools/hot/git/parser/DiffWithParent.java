package com.tools.hot.git.parser;

import static java.util.stream.Collectors.toList;

import java.util.List;
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

  public List<Change> changes() {
    return diffEntries().stream()
        .map(DiffEntry::getNewPath)
        .distinct()
        .map(file -> new Change(file, commit.getAuthorIdent().getWhen().toInstant()))
        .collect(toList());
  }

  private List<DiffEntry> diffEntries() {
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
