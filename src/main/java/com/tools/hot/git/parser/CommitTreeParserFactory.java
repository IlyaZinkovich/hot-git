package com.tools.hot.git.parser;

import java.io.IOException;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;

public final class CommitTreeParserFactory {

  private final ObjectReader objectReader;

  public CommitTreeParserFactory(final ObjectReader objectReader) {
    this.objectReader = objectReader;
  }

  public AbstractTreeIterator commitTreeParser(final RevCommit commit) {
    try {
      return new CommitTreeParser(objectReader, commit.getTree().toObjectId());
    } catch (IOException e) {
      throw new CommitTreeParsingException(e);
    }
  }

  public AbstractTreeIterator parentCommitTreeParser(final RevCommit commit) {
    if (commit.getParentCount() > 0) {
      return commitTreeParser(commit.getParent(0));
    } else {
      return new EmptyTreeIterator();
    }
  }
}
