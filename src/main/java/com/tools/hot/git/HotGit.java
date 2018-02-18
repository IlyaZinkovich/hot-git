package com.tools.hot.git;

import static java.lang.String.format;

import com.tools.hot.git.application.service.RepoAnalysisService;
import com.tools.hot.git.infrastructure.RelativeChangeRepository;
import com.tools.hot.git.infrastructure.RepoFactory;
import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;

public final class HotGit {

  public static void main(String[] args) {
    final String sonarlint = "../sonarlint-intellij/.git";
    final RepoAnalysisService repoAnalysisService =
        new RepoAnalysisService(new RepoFactory(), new RelativeChangeRepository());
    repoAnalysisService.prepare(sonarlint);
    final Duration duration = Duration.ofDays(2);
    final Map<String, Long> concurrentChangesPerFile =
        repoAnalysisService.getConcurrentChangesPerFile(duration);
    concurrentChangesPerFile.entrySet().stream()
        .filter(entry -> entry.getValue() > 0)
        .forEach(entry -> System.out.println(outputFormat(entry, duration)));
  }

  private static String outputFormat(Entry<String, Long> entry, Duration duration) {
    return format("%s: %d changes within %d hours", entry.getKey(), entry.getValue(),
        duration.toHours());
  }
}
