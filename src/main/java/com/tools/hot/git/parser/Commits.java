package com.tools.hot.git.parser;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

public final class Commits {

  private final Git git;

  public Commits(final Git git) {
    this.git = git;
  }

  public Stream<RevCommit> stream() {
    try {
      final Iterable<RevCommit> commitIterable = git.log().call();
      final boolean notParallel = false;
      return StreamSupport.stream(commitIterable.spliterator(), notParallel);
    } catch (GitAPIException e) {
      throw new GitLogException(e);
    }
  }
}
