package com.tools.hot.git;

import static java.lang.String.format;
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
    Repository repository = builder.setGitDir(new File("../sonarlint-intellij/.git"))
        .readEnvironment()
        .findGitDir()
        .build();
    final Git git = new Git(repository);
    final boolean notParallel = false;
    final Map<String, List<Instant>> statistics = stream(allCommits(repository).spliterator(),
        notParallel)
        .flatMap(commit -> diff(repository, git, commit).stream().map(DiffEntry::getNewPath)
            .distinct()
            .collect(toMap(path -> path, path -> commit.getAuthorIdent().getWhen().toInstant()))
            .entrySet().stream())
        .collect(groupingBy(Entry::getKey, mapping(Entry::getValue, toList())));
    final Map<String, List<Long>> filesWithDurationsBetweenChangesInHours = statistics.entrySet()
        .stream()
        .collect(toMap(entry -> entry.getKey(), entry -> durations(entry.getValue())));
    final Map<String, Long> hotFiles = filesWithDurationsBetweenChangesInHours.entrySet()
        .stream()
        .collect(toMap(entry -> entry.getKey(),
            entry -> entry.getValue().stream().filter(duration -> duration < 48L).count()));
    hotFiles.entrySet().stream()
        .filter(entry -> entry.getValue() > 0)
        .forEach(entry -> System.out.println(outputFormat(entry)));
  }

  private static String outputFormat(Entry<String, Long> entry) {
    return format("%s: %d changes within 2 days", entry.getKey(), entry.getValue());
  }

  private static List<Long> durations(List<Instant> times) {
    times.sort(Instant::compareTo);
    return Seq.seq(times).sliding(2).map(seq -> {
      final Instant[] changeTimes = seq.toArray(Instant[]::new);
      Instant first = changeTimes[0];
      Instant second = changeTimes[1];
      return Duration.between(first, second).toHours();
    }).collect(toList());
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
