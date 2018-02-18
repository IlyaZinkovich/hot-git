package com.tools.hot.git.application.service;

import com.tools.hot.git.infrastructure.RelativeChangeRepository;
import com.tools.hot.git.infrastructure.RepoFactory;
import com.tools.hot.git.parser.RelativeChange;
import com.tools.hot.git.parser.RelativeChanges;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;

public class RepoAnalysisService {

  private final RepoFactory repoFactory;
  private final RelativeChangeRepository relativeChangeRepository;

  public RepoAnalysisService(final RepoFactory repoFactory,
      final RelativeChangeRepository relativeChangeRepository) {
    this.repoFactory = repoFactory;
    this.relativeChangeRepository = relativeChangeRepository;
  }

  public void prepare(String pathToRepo) {
    final Repository repository = repoFactory.get(pathToRepo);
    final Git git = new Git(repository);
    try (final ObjectReader objectReader = repository.newObjectReader()) {
      final List<RelativeChange> relativeChanges = new RelativeChanges(objectReader, git).list();
      relativeChangeRepository.save(relativeChanges);
    }
  }
}