package com.tools.hot.git.presentation;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HotGitUI extends Application {

  @Override
  public void start(Stage primaryStage) {
    Group root = new Group();
    primaryStage.setTitle("Hot Git");
    primaryStage.setScene(new Scene(root, 300, 275));
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}