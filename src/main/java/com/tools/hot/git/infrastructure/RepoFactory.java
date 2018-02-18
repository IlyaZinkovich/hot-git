package com.tools.hot.git.infrastructure;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class RepoFactory {

  public Repository get(String pathToRepo) {
    try {
      return new FileRepositoryBuilder()
          .setGitDir(new File(pathToRepo))
          .readEnvironment()
          .findGitDir()
          .build();
    } catch (IOException e) {
      throw new RepoNotFoundException(e);
    }
  }
}
