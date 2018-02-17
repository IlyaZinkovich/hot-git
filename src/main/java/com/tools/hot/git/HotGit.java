package com.tools.hot.git;

import static java.lang.String.format;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;

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
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.jooq.lambda.Seq;

public class HotGit {

  public static void main(String[] args) throws IOException, GitAPIException {
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    final String sonarlint = "../sonarlint-intellij/.git";
    Repository repository = builder.setGitDir(new File(sonarlint))
        .readEnvironment()
        .findGitDir()
        .build();
    final Git git = new Git(repository);
    final boolean notParallel = false;
    final Map<String, List<Instant>> fileChangeDates = stream(allCommits(repository).spliterator(),
        notParallel)
        .flatMap(commit -> diff(repository, git, commit).stream().map(DiffEntry::getNewPath)
            .distinct()
            .collect(toMap(path -> path, path -> commit.getAuthorIdent().getWhen().toInstant()))
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

  private static List<DiffEntry> diff(Repository repository, Git git, RevCommit commit) {
    AbstractTreeIterator newTreeIterator = treeIterator(repository, commit);
    AbstractTreeIterator oldTreeIterator = parentTreeIterator(repository, commit);
    try {
      return git.diff()
          .setNewTree(newTreeIterator)
          .setOldTree(oldTreeIterator)
          .call();
    } catch (GitAPIException e) {
      throw new RuntimeException(e);
    }
  }

  private static AbstractTreeIterator parentTreeIterator(Repository repository, RevCommit commit) {
    AbstractTreeIterator oldTreeIterator;
    if (commit.getParentCount() > 0) {
      oldTreeIterator = treeIterator(repository, commit.getParent(0));
    } else {
      oldTreeIterator = new EmptyTreeIterator();
    }
    return oldTreeIterator;
  }

  private static CanonicalTreeParser treeIterator(Repository repository, RevCommit commit) {
    try {
      CanonicalTreeParser treeIterator = new CanonicalTreeParser();
      treeIterator.reset(repository.newObjectReader(), commit.getTree().toObjectId());
      return treeIterator;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Iterable<RevCommit> allCommits(Repository repository) throws GitAPIException {
    return new Git(repository).log().call();
  }
}
