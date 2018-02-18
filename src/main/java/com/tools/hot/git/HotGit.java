package com.tools.hot.git;

import static java.lang.String.format;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.tools.hot.git.parser.CommitTreeParserFactory;
import com.tools.hot.git.parser.Commits;
import com.tools.hot.git.parser.DiffWithParent;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jooq.lambda.Seq;

public class HotGit {

  public static void main(String[] args) throws IOException {
    final String sonarlint = "../sonarlint-intellij/.git";
    final Repository repository = new FileRepositoryBuilder().setGitDir(new File(sonarlint))
        .readEnvironment()
        .findGitDir()
        .build();
    final Git git = new Git(repository);
    try (final ObjectReader objectReader = repository.newObjectReader()) {
      final CommitTreeParserFactory parserFactory = new CommitTreeParserFactory(objectReader);
      final Map<String, List<Instant>> fileChangeDates = new Commits(git).stream()
          .flatMap(commit -> new DiffWithParent(commit, parserFactory, git).fileChangeDates()
              .entrySet().stream())
          .collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toList())));
      final List<RelativeChange> relativeChanges = fileChangeDates.entrySet()
          .stream()
          .flatMap(HotGit::fileChangeDatesToRelativeChanges)
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

  private static Stream<? extends RelativeChange> fileChangeDatesToRelativeChanges(
      Entry<String, List<Instant>> entry) {
    final String file = entry.getKey();
    final List<Instant> changeDates = entry.getValue();
    changeDates.sort(Instant::compareTo);
    return Seq.seq(changeDates).sliding(2).map(seq -> {
      final Instant[] changeTimes = seq.toArray(Instant[]::new);
      Instant first = changeTimes[0];
      Instant second = changeTimes[1];
      final Duration duration = Duration.between(first, second);
      return new RelativeChange(file, second, duration);
    });
  }

  private static String outputFormat(Entry<String, Long> entry, Duration duration) {
    return format("%s: %d changes within %d hours", entry.getKey(), entry.getValue(),
        duration.toHours());
  }

  private static Iterable<RevCommit> allCommits(Repository repository) throws GitAPIException {
    return new Git(repository).log().call();
  }
}
