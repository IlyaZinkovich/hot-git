package com.tools.hot.git.presentation;

import com.tools.hot.git.application.service.RepoAnalysisService;
import com.tools.hot.git.infrastructure.RelativeChangeRepository;
import com.tools.hot.git.infrastructure.RepoFactory;
import java.io.File;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class HotGitUI extends Application {

  private static final String TITLE = "Hot Git";
  private static final int WIDTH = 500;
  private static final int HEIGHT = 500;
  private static final String FILE_MENU_TITLE = "File";
  private static final String CHOOSE_REPO_FILE_MENU_ITEM_TITLE = "Choose repo...";

  private final DirectoryChooser directoryChooser;
  private final RepoAnalysisService repoAnalysisService;

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
    final Scene scene = new Scene(root, WIDTH, HEIGHT);
    final MenuBar menuBar = new MenuBar();
    final Menu fileMenu = new Menu(FILE_MENU_TITLE);
    final MenuItem chooseRepo = new MenuItem(CHOOSE_REPO_FILE_MENU_ITEM_TITLE);
    chooseRepo.setOnAction(event -> {
      File file = directoryChooser.showDialog(stage);
      if (file != null) {
        repoAnalysisService.prepare(file.toURI().toString().concat("/.git"));
      }
    });
    fileMenu.getItems().addAll(chooseRepo);
    menuBar.getMenus().addAll(fileMenu);
    root.getChildren().addAll(menuBar);
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}