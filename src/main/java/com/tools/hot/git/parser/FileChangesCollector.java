package com.tools.hot.git.parser;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class FileChangesCollector
    implements Collector<Change, Map<String, FileChanges>, List<FileChanges>> {

  @Override
  public Supplier<Map<String, FileChanges>> supplier() {
    return HashMap::new;
  }

  @Override
  public BiConsumer<Map<String, FileChanges>, Change> accumulator() {
    return (changesAccumulator, fileChange) -> {
      final String changedFileName = fileChange.file();
      if (!changesAccumulator.containsKey(changedFileName)) {
        final FileChanges fileChanges = new FileChanges(changedFileName, new ArrayList<>());
        changesAccumulator.put(changedFileName, fileChanges);
      }
      final FileChanges fileChanges = changesAccumulator.get(changedFileName);
      fileChanges.addFileChange(fileChange);
    };
  }

  @Override
  public BinaryOperator<Map<String, FileChanges>> combiner() {
    return (firstAccumulator, secondAccumulator) -> {
      final Stream<Entry<String, FileChanges>> firstStream = firstAccumulator.entrySet().stream();
      final Stream<Entry<String, FileChanges>> secondStream = firstAccumulator.entrySet().stream();
      return concat(firstStream, secondStream)
          .collect(toMap(Entry::getKey, Entry::getValue, FileChanges::merge));
    };
  }

  @Override
  public Function<Map<String, FileChanges>, List<FileChanges>> finisher() {
    return accumulator -> new ArrayList<>(accumulator.values());
  }

  @Override
  public Set<Characteristics> characteristics() {
    return emptySet();
  }
}
