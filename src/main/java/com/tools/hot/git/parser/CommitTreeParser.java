package com.tools.hot.git.parser;

import java.io.IOException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public class CommitTreeParser extends CanonicalTreeParser {

  private static final byte[] ROOT = {};

  public CommitTreeParser(ObjectReader repositoryObjectReader, ObjectId commitTreeObjectId)
      throws IOException {
    super(ROOT, repositoryObjectReader, commitTreeObjectId);
  }
}
