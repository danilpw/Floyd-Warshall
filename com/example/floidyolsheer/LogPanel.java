package com.example.floidyolsheer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LogPanel extends VBox {

    private TextArea logArea;
    private Label titleLabel;

    public LogPanel() {
        setPadding(new Insets(10, 20, 10, 20));
        setSpacing(5);
        setStyle("-fx-background-color: #85C2DE;");
        setPrefHeight(200);
        setMaxHeight(230);

        titleLabel = new Label("Лог выполнения");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setStyle(
                "-fx-border-color: #ffffff;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-color: #85C2DE;" +
                        "-fx-padding: 5 15 5 15;" +
                        "-fx-text-fill: #ffffff;"
        );
        titleLabel.setAlignment(Pos.CENTER);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setFont(Font.font("Consolas", 14));
        logArea.setStyle(
                "-fx-control-inner-background: #85C2DE;" +
                        "-fx-border-color: #ffffff;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 5;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-background-color: #85C2DE;" +
                        "-fx-background-radius: 5;"
        );
        logArea.setPrefHeight(150);

        getChildren().addAll(titleLabel, logArea);

        addLogMessage("Для запуска алгоритма нажмите 'Старт'!");
    }

    public void addLogMessage(String message) {
        logArea.appendText(message + "\n");
        logArea.setScrollTop(Double.MAX_VALUE);
    }

    public void clearLog() {
        logArea.clear();
    }

    public void setCurrentVertex(String vertex) {
        addLogMessage("Текущая вершина: " + vertex);
    }
}