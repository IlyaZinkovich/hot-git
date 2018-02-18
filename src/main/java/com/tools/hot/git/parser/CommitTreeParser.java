package com.tools.hot.git.parser;

import java.io.IOException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

final class CommitTreeParser extends CanonicalTreeParser {

  private static final byte[] ROOT = {};

  CommitTreeParser(final ObjectReader repositoryObjectReader, final ObjectId commitTreeObjectId)
      throws IOException {
    super(ROOT, repositoryObjectReader, commitTreeObjectId);
  }
}
