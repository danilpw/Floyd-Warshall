package com.example.floidyolsheer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ControlPanel extends VBox {

    private VisualizationController controller;
    private Button loadButton;
    private Button saveButton;
    private Button startButton;
    private Button nextButton;
    private Button previousButton;
    private Button resetButton;
    private Button authorsButton;

    public ControlPanel(VisualizationController controller) {
        this.controller = controller;
        initializeUI();
    }

    private void initializeUI() {
        setPadding(new Insets(10));
        setSpacing(10);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #85C2DE; -fx-border-color: #ffffff; -fx-border-width: 2;");

        HBox buttonBox = new HBox(70);
        buttonBox.setAlignment(Pos.CENTER);

        loadButton = createButton("Загрузить", "#53A0CF");
        saveButton = createButton("Сохранить", "#53A0CF");
        startButton = createButton("Старт", "#53A0CF");
        nextButton = createButton("Шаг вперед", "#53A0CF");
        previousButton = createButton("Шаг назад", "#53A0CF");
        resetButton = createButton("Сброс", "#53A0CF");
        authorsButton = createButton("О разработчиках", "#53A0CF");

        loadButton.setOnAction(e -> controller.onLoadButtonClick());
        saveButton.setOnAction(e -> controller.onSaveButtonClick());
        startButton.setOnAction(e -> controller.onStartButtonClick());
        nextButton.setOnAction(e -> controller.onNextButtonClick());
        previousButton.setOnAction(e -> controller.onPreviousButtonClick());
        resetButton.setOnAction(e -> controller.onResetButtonClick());
        authorsButton.setOnAction(e -> controller.onAuthorsButtonClick());

        buttonBox.getChildren().addAll(
                loadButton, saveButton, startButton,
                nextButton, previousButton, resetButton, authorsButton
        );

        getChildren().add(buttonBox);
    }

    private Button createButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 25 12 25;" +
                        "-fx-font-size: 16px;" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-width: 0;" +
                        "-fx-focus-color: transparent;" +
                        "-fx-faint-focus-color: transparent;"
        );
        return button;
    }

    public void setButtonsEnabled(boolean enabled) {
        loadButton.setDisable(!enabled);
        saveButton.setDisable(!enabled);
        startButton.setDisable(!enabled);
        nextButton.setDisable(!enabled);
        previousButton.setDisable(!enabled);
        resetButton.setDisable(!enabled);
        authorsButton.setDisable(!enabled);
    }
}