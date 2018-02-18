package com.tools.hot.git.application.service;

import com.tools.hot.git.infrastructure.RelativeChangeRepository;
import com.tools.hot.git.infrastructure.RepoFactory;
import com.tools.hot.git.parser.ConcurrentChangesPerFile;
import com.tools.hot.git.parser.RelativeChanges;
import java.time.Duration;
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;

public final class RepoAnalysisService {

  private final RepoFactory repoFactory;
  private final RelativeChangeRepository relativeChangeRepository;

  public RepoAnalysisService(final RepoFactory repoFactory,
      final RelativeChangeRepository relativeChangeRepository) {
    this.repoFactory = repoFactory;
    this.relativeChangeRepository = relativeChangeRepository;
  }

  public void prepare(final String pathToRepo) {
    final Repository repository = repoFactory.get(pathToRepo);
    final Git git = new Git(repository);
    try (final ObjectReader objectReader = repository.newObjectReader()) {
      relativeChangeRepository.save(new RelativeChanges(objectReader, git));
    }
  }

  public Map<String, Long> getConcurrentChangesPerFile(final Duration concurrencyDuration) {
    final RelativeChanges relativeChanges = relativeChangeRepository.findAll();
    return new ConcurrentChangesPerFile(relativeChanges, concurrencyDuration).toMap();
  }
}
