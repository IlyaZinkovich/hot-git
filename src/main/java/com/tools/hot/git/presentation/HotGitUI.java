package com.tools.hot.git.presentation;

import static javafx.collections.FXCollections.observableArrayList;

import com.tools.hot.git.application.service.RepoAnalysisService;
import com.tools.hot.git.infrastructure.RelativeChangeRepository;
import com.tools.hot.git.infrastructure.RepoFactory;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
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

  public HotGitUI() {
    this.directoryChooser = new DirectoryChooser();
    final RepoFactory repoFactory = new RepoFactory();
    final RelativeChangeRepository relativeChangeRepository = new RelativeChangeRepository();
    this.repoAnalysisService = new RepoAnalysisService(repoFactory, relativeChangeRepository);
  }

  @Override
  public void start(Stage stage) {
    stage.setTitle(TITLE);
    final VBox root = new VBox();
    final Scene scene = new Scene(root);
    final TableView<ConcurrentChangesPerFileData> tableView = setupTableView();
    final MenuBar menuBar = setupMenuBar(stage, tableView);
    tableView.setVisible(false);
    root.getChildren().addAll(menuBar, tableView);
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
    return tableView;
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