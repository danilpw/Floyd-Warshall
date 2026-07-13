package com.example.floidyolsheer;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainWindow extends Application {

    private VisualizationController controller;
    private ControlPanel controlPanel;
    private GraphEditor graphEditor;
    private MatrixPanel matrixPanel;
    private LogPanel logPanel;

    @Override
    public void start(Stage primaryStage) {
        controller = new VisualizationController();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        controlPanel = new ControlPanel(controller);
        graphEditor = new GraphEditor();
        matrixPanel = new MatrixPanel();
        logPanel = new LogPanel();

        root.setTop(controlPanel);
        
        ScrollPane graphScroll = new ScrollPane(graphEditor);
        graphScroll.setFitToWidth(true);
        graphScroll.setFitToHeight(true);
        graphScroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        graphScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        graphScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        root.setCenter(graphScroll);
        root.setRight(matrixPanel);
        root.setBottom(logPanel);

        matrixPanel.setMinWidth(450);
        matrixPanel.setMaxWidth(450);

        Scene scene = new Scene(root, 1400, 900);
        primaryStage.setTitle("Floyd-Warshall Algorithm");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        controller.setGraphEditor(graphEditor);
        controller.setMatrixPanel(matrixPanel);
        controller.setLogPanel(logPanel);
        controller.initDemoData();
    }

    public static void main(String[] args) {
        launch(args);
    }
}