package com.tools.hot.git;

import static java.lang.String.format;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.tools.hot.git.parser.Change;
import com.tools.hot.git.parser.CommitTreeParserFactory;
import com.tools.hot.git.parser.Commits;
import com.tools.hot.git.parser.DiffWithParent;
import com.tools.hot.git.parser.FileChanges;
import com.tools.hot.git.parser.FileChangesGroup;
import com.tools.hot.git.parser.RelativeChange;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public final class HotGit {

  public static void main(String[] args) throws IOException {
    final String sonarlint = "../sonarlint-intellij/.git";
    final Repository repository = new FileRepositoryBuilder()
        .setGitDir(new File(sonarlint))
        .readEnvironment()
        .findGitDir()
        .build();
    final Git git = new Git(repository);
    try (final ObjectReader objectReader = repository.newObjectReader()) {
      final CommitTreeParserFactory parserFactory = new CommitTreeParserFactory(objectReader);
      final List<Change> changes = new Commits(git).stream()
          .map(commit -> new DiffWithParent(commit, parserFactory, git))
          .map(DiffWithParent::changes)
          .flatMap(Collection::stream)
          .collect(toList());
      final List<RelativeChange> relativeChanges = new FileChangesGroup(changes).stream()
          .map(FileChanges::relativeChanges)
          .flatMap(Collection::stream)
          .collect(toList());
      final Duration duration = Duration.ofDays(2);
      final Map<String, Long> hotFiles = relativeChanges.stream()
          .filter(relativeChange -> relativeChange.within(duration))
          .collect(groupingBy(RelativeChange::file, counting()));
      hotFiles.entrySet().stream()
          .filter(entry -> entry.getValue() > 0)
          .forEach(entry -> System.out.println(outputFormat(entry, duration)));
    }
  }

  private static String outputFormat(Entry<String, Long> entry, Duration duration) {
    return format("%s: %d changes within %d hours", entry.getKey(), entry.getValue(),
        duration.toHours());
  }
}
