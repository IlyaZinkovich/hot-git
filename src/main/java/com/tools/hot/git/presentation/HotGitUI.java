package com.tools.hot.git.presentation;

import static javafx.collections.FXCollections.observableArrayList;

import com.tools.hot.git.application.service.RepoAnalysisService;
import com.tools.hot.git.infrastructure.RelativeChangeRepository;
import com.tools.hot.git.infrastructure.RepoFactory;
import com.tools.hot.git.parser.RelativeChange;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class HotGitUI extends Application {

  private static final String TITLE = "Hot Git";
  private static final String FILE_MENU_TITLE = "File";
  private static final String CHOOSE_REPO_FILE_MENU_ITEM_TITLE = "Choose repo...";
  private static final String FILE_NAME_COLUMN_TITLE = "File name";
  private static final String CONCURRENT_CHANGES_COUNT_COLUMN_NAME = "Concurrent changes count";
  private static final boolean FULL_SCREEN = true;

  private final DirectoryChooser directoryChooser;
  private final RepoAnalysisService repoAnalysisService;
  private Duration currentConcurrencyDuration = Duration.ofDays(1);
  private LineChart chart;

  public HotGitUI() {
    this.directoryChooser = new DirectoryChooser();
    final RepoFactory repoFactory = new RepoFactory();
    final RelativeChangeRepository relativeChangeRepository = new RelativeChangeRepository();
    this.repoAnalysisService = new RepoAnalysisService(repoFactory, relativeChangeRepository);
    final NumberAxis numberAxis = new NumberAxis();
    final CategoryAxis categoryAxis = new CategoryAxis();
    chart = new LineChart(categoryAxis, numberAxis);
  }

  @Override
  public void start(Stage stage) {
    stage.setTitle(TITLE);
    final VBox root = new VBox();
    final Scene scene = new Scene(root);
    chart.setVisible(false);
    final TableView<ConcurrentChangesPerFileData> tableView = setupTableView();
    final MenuBar menuBar = setupMenuBar(stage, tableView);
    root.getChildren().addAll(menuBar, tableView, chart);
    stage.setScene(scene);
    stage.show();
    stage.setFullScreen(FULL_SCREEN);
  }

  private TableView<ConcurrentChangesPerFileData> setupTableView() {
    final TableView<ConcurrentChangesPerFileData> tableView = new TableView<>();
    final TableColumn<ConcurrentChangesPerFileData, String> fileNameColumn =
        new TableColumn<>(FILE_NAME_COLUMN_TITLE);
    fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
    fileNameColumn.prefWidthProperty()
        .bind(tableView.widthProperty().multiply(0.50));
    final TableColumn<ConcurrentChangesPerFileData, Long> concurrentChangesCountColumn =
        new TableColumn<>(CONCURRENT_CHANGES_COUNT_COLUMN_NAME);
    concurrentChangesCountColumn
        .setCellValueFactory(new PropertyValueFactory<>("concurrentChangesCount"));
    concurrentChangesCountColumn.prefWidthProperty()
        .bind(tableView.widthProperty().multiply(0.50));
    tableView.getColumns().addAll(fileNameColumn, concurrentChangesCountColumn);
    tableView.setVisible(false);
    tableView.setRowFactory(this::rowFactory);
    return tableView;
  }

  private TableRow<ConcurrentChangesPerFileData> rowFactory(
      TableView<ConcurrentChangesPerFileData> param) {
    final TableRow<ConcurrentChangesPerFileData> row = new TableRow<>();
    row.setOnMouseClicked(event -> {
      final ConcurrentChangesPerFileData data = row.getItem();
      final String fileName = data.getFileName();
      final List<RelativeChange> relativeChanges = repoAnalysisService.getRelativeChangesPerFile()
          .get(fileName);
      final Series series = new Series();
      relativeChanges.stream()
          .map(relativeChange -> {
            final String x = relativeChange.date().toString();
            final Long y = relativeChange.durationSinceLastChange().toDays();
            return new Data<>(x, y);
          })
          .forEach(series.getData()::add);
      final Instant lastChangeDate = relativeChanges.get(relativeChanges.size() - 1).date();
      final Instant now = Instant.now();
      final Duration durationBetweenLastChangeAndNow = Duration.between(lastChangeDate, now);
      series.getData().add(new Data<>(now.toString(), durationBetweenLastChangeAndNow.toDays()));
      series.setName(fileName);
      chart.getData().clear();
      chart.getData().addAll(series);
      chart.setVisible(true);
    });
    return row;
  }

  private MenuBar setupMenuBar(Stage stage, TableView<ConcurrentChangesPerFileData> tableView) {
    final MenuBar menuBar = new MenuBar();
    final Menu fileMenu = new Menu(FILE_MENU_TITLE);
    final MenuItem chooseRepo = new MenuItem(CHOOSE_REPO_FILE_MENU_ITEM_TITLE);
    chooseRepo.setOnAction(event -> {
      File file = directoryChooser.showDialog(stage);
      if (file != null) {
        repoAnalysisService.prepare(repoMetadataPath(file));
        final Map<String, Long> concurrentChangesPerFile =
            repoAnalysisService.getConcurrentChangesPerFile(currentConcurrencyDuration);
        final List<ConcurrentChangesPerFileData> data = concurrentChangesPerFile.entrySet()
            .stream()
            .map(entry -> new ConcurrentChangesPerFileData(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
        tableView.setItems(observableArrayList(data));
        tableView.setVisible(true);
        chart.setVisible(false);
      }
    });
    fileMenu.getItems().addAll(chooseRepo);
    menuBar.getMenus().addAll(fileMenu);
    return menuBar;
  }

  private String repoMetadataPath(File file) {
    final Path absolutePath = Paths.get(file.toURI());
    final Path base = Paths.get("").toAbsolutePath();
    final String relativeRepoPath = base.relativize(absolutePath).toString();
    return relativeRepoPath.concat("/.git");
  }

  public static void main(String[] args) {
    launch(args);
  }
}