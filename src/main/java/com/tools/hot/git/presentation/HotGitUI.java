package com.tools.hot.git.presentation;

import static java.util.Objects.requireNonNull;

import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HotGitUI extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    final ClassLoader classLoader = getClass().getClassLoader();
    final String fxmlFileName = "index.fxml";
    final URL fxml = requireNonNull(classLoader.getResource(fxmlFileName)).toURI().toURL();
    Parent root = FXMLLoader.load(fxml);
    primaryStage.setTitle("Hot Git");
    primaryStage.setScene(new Scene(root, 300, 275));
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}