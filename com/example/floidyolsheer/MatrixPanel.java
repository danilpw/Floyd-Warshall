package com.example.floidyolsheer;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class MatrixPanel extends VBox {

    private static final int CELL_WIDTH = 50;
    private static final int CELL_HEIGHT = 40;
    private static final int GAP = 4;
    private static final int VIEW_SIZE = 300;

    private GridPane topHeaders;
    private GridPane leftHeaders;
    private GridPane matrixGrid;

    private Pane topHeaderViewport;
    private Pane leftHeaderViewport;
    private ScrollPane matrixScroll;

    private int[][] distanceMatrix;
    private String[] vertexNames;
    private int size;

    public MatrixPanel() {
        setPadding(new Insets(20));
        setSpacing(15);
        setStyle(
                "-fx-background-color: #85C2DE;" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 2;"
        );
        setPrefWidth(450);
        setMaxWidth(450);
        setAlignment(Pos.CENTER);

        Label title = new Label("Матрица расстояний");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setStyle("-fx-text-fill: white;");
        title.setAlignment(Pos.CENTER);

        size = 0;
        distanceMatrix = new int[0][0];
        vertexNames = new String[0];

        topHeaders = new GridPane();
        topHeaders.setHgap(GAP);
        topHeaders.setVgap(0);

        leftHeaders = new GridPane();
        leftHeaders.setVgap(GAP);
        leftHeaders.setHgap(0);

        matrixGrid = new GridPane();
        matrixGrid.setHgap(GAP);
        matrixGrid.setVgap(GAP);

        matrixScroll = new ScrollPane(matrixGrid);
        matrixScroll.setPrefViewportWidth(VIEW_SIZE);
        matrixScroll.setPrefViewportHeight(VIEW_SIZE);
        matrixScroll.setMaxWidth(VIEW_SIZE);
        matrixScroll.setMaxHeight(VIEW_SIZE);
        matrixScroll.setFitToWidth(false);
        matrixScroll.setFitToHeight(false);
        matrixScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        matrixScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        matrixScroll.setStyle("-fx-background-color: transparent;");

        topHeaderViewport = createClippedViewport(topHeaders, VIEW_SIZE, CELL_HEIGHT);
        leftHeaderViewport = createClippedViewport(leftHeaders, CELL_WIDTH, VIEW_SIZE);

        matrixScroll.hvalueProperty().addListener((obs, oldVal, newVal) -> syncHeaderPositions());
        matrixScroll.vvalueProperty().addListener((obs, oldVal, newVal) -> syncHeaderPositions());
        matrixScroll.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> syncHeaderPositions());

        styleScrollBars(matrixScroll);

        Label corner = new Label();
        corner.setMinSize(CELL_WIDTH, CELL_HEIGHT);
        corner.setPrefSize(CELL_WIDTH, CELL_HEIGHT);
        corner.setMaxSize(CELL_WIDTH, CELL_HEIGHT);
        corner.setStyle(
                "-fx-background-color: #53A0CF;" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 2;"
        );

        GridPane body = new GridPane();
        body.setAlignment(Pos.CENTER);
        body.add(corner, 0, 0);
        body.add(topHeaderViewport, 1, 0);
        body.add(leftHeaderViewport, 0, 1);
        body.add(matrixScroll, 1, 1);

        VBox bodyWrapper = new VBox(body);
        bodyWrapper.setAlignment(Pos.CENTER);

        getChildren().addAll(title, bodyWrapper);

        syncHeaderPositions();
    }

    private Pane createClippedViewport(Node content, double width, double height) {
        Pane viewport = new Pane(content);
        viewport.setMinSize(0, 0);
        viewport.setPrefSize(width, height);
        viewport.setMaxSize(width, height);

        Rectangle clip = new Rectangle(width, height);
        clip.widthProperty().bind(viewport.widthProperty());
        clip.heightProperty().bind(viewport.heightProperty());
        viewport.setClip(clip);

        return viewport;
    }

    private void syncHeaderPositions() {
        Bounds viewportBounds = matrixScroll.getViewportBounds();
        if (viewportBounds == null) {
            return;
        }

        double contentWidth = matrixGrid.getBoundsInLocal().getWidth();
        double contentHeight = matrixGrid.getBoundsInLocal().getHeight();

        double maxScrollX = Math.max(0, contentWidth - viewportBounds.getWidth());
        double maxScrollY = Math.max(0, contentHeight - viewportBounds.getHeight());

        double offsetX = matrixScroll.getHvalue() * maxScrollX;
        double offsetY = matrixScroll.getVvalue() * maxScrollY;

        topHeaders.setTranslateX(-offsetX);
        leftHeaders.setTranslateY(-offsetY);
    }

    private void styleScrollBars(ScrollPane scrollPane) {
        scrollPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> applyScrollBarStyle(scrollPane));
            }
        });
        if (scrollPane.getScene() != null) {
            Platform.runLater(() -> applyScrollBarStyle(scrollPane));
        }
    }

    private void applyScrollBarStyle(ScrollPane scrollPane) {
        for (Node node : scrollPane.lookupAll(".scroll-bar")) {
            if (!(node instanceof ScrollBar)) {
                continue;
            }
            ScrollBar bar = (ScrollBar) node;
            bar.setStyle("-fx-background-color: transparent;");

            for (Node track : bar.lookupAll(".track")) {
                track.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.35);" +
                                "-fx-background-radius: 6;"
                );
            }
            for (Node thumb : bar.lookupAll(".thumb")) {
                thumb.setStyle(
                        "-fx-background-color: #2E76A3;" +
                                "-fx-background-radius: 6;"
                );
            }
            for (Node button : bar.lookupAll(".increment-button, .decrement-button")) {
                button.setStyle("-fx-opacity: 0; -fx-padding: 0;");
            }
            for (Node arrow : bar.lookupAll(".increment-arrow, .decrement-arrow")) {
                arrow.setStyle("-fx-padding: 0;");
            }
        }
    }

    private void drawHeaders() {
        for (int i = 0; i < size; i++) {
            Label top = createHeader(vertexNames[i]);
            topHeaders.add(top, i, 0);

            Label left = createHeader(vertexNames[i]);
            leftHeaders.add(left, 0, i);
        }
    }

    public void drawMatrix() {
        matrixGrid.getChildren().clear();
        if (size == 0) return;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Label cell = createCell(distanceMatrix[i][j]);
                matrixGrid.add(cell, j, i);
            }
        }
    }

    private Label createHeader(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-background-color: #53A0CF;" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 1;"
        );
        label.setMinSize(CELL_WIDTH, CELL_HEIGHT);
        label.setPrefSize(CELL_WIDTH, CELL_HEIGHT);
        label.setMaxSize(CELL_WIDTH, CELL_HEIGHT);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private Label createCell(int value) {
        Label cell = new Label(value == 999 ? "∞" : String.valueOf(value));
        cell.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        cell.setStyle(
                "-fx-background-color: #e3f0f7;" +
                        "-fx-border-color: #b8d4e8;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;"
        );
        cell.setMinSize(CELL_WIDTH, CELL_HEIGHT);
        cell.setPrefSize(CELL_WIDTH, CELL_HEIGHT);
        cell.setMaxSize(CELL_WIDTH, CELL_HEIGHT);
        cell.setAlignment(Pos.CENTER);
        return cell;
    }

    public void highlightCell(int row, int col) {
        highlightCell(row, col, "#ff6b6b");
    }

    public void highlightCell(int row, int col, String color) {
        clearHighlight();

        if (row >= 0 && row < size && col >= 0 && col < size) {
            for (javafx.scene.Node node : matrixGrid.getChildren()) {
                Integer rowIndex = GridPane.getRowIndex(node);
                Integer colIndex = GridPane.getColumnIndex(node);
                if (rowIndex != null && colIndex != null &&
                        rowIndex == row && colIndex == col && node instanceof Label) {
                    Label cell = (Label) node;
                    cell.setStyle(
                            "-fx-background-color: " + color + ";" +
                                    "-fx-border-color: #b8d4e8;" +
                                    "-fx-border-width: 1;" +
                                    "-fx-border-radius: 4;" +
                                    "-fx-background-radius: 4;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-weight: bold;"
                    );
                    break;
                }
            }
        }
    }

    public void clearHighlight() {
        for (javafx.scene.Node node : matrixGrid.getChildren()) {
            if (node instanceof Label) {
                Label cell = (Label) node;
                cell.setStyle(
                        "-fx-background-color: #e3f0f7;" +
                                "-fx-border-color: #b8d4e8;" +
                                "-fx-border-width: 1;" +
                                "-fx-border-radius: 4;" +
                                "-fx-background-radius: 4;"
                );
            }
        }
    }

    public void updateMatrix(double[][] newMatrix) {
        if (newMatrix == null || newMatrix.length == 0) {
            size = 0;
            distanceMatrix = new int[0][0];
            vertexNames = new String[0];
            topHeaders.getChildren().clear();
            leftHeaders.getChildren().clear();
            drawMatrix();
            syncHeaderPositions();
            return;
        }

        size = newMatrix.length;
        distanceMatrix = new int[size][size];
        vertexNames = new String[size];

        for (int i = 0; i < size; i++) {
            vertexNames[i] = String.valueOf(i + 1);
            for (int j = 0; j < size; j++) {
                if (Double.isInfinite(newMatrix[i][j])) {
                    distanceMatrix[i][j] = 999;
                } else {
                    distanceMatrix[i][j] = (int) Math.round(newMatrix[i][j]);
                }
            }
        }

        topHeaders.getChildren().clear();
        leftHeaders.getChildren().clear();
        drawHeaders();
        drawMatrix();
        syncHeaderPositions();
    }

    public void updateFromGraph(Graph graph) {
        if (graph == null || graph.getVertexCount() == 0) {
            updateMatrix(null);
            return;
        }

        List<Vertex> vertices = graph.getVertices();
        size = vertices.size();
        distanceMatrix = new int[size][size];
        vertexNames = new String[size];

        for (int i = 0; i < size; i++) {
            vertexNames[i] = vertices.get(i).getName();
        }

        double[][] matrix = graph.getAdjacencyMatrix();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (Double.isInfinite(matrix[i][j])) {
                    distanceMatrix[i][j] = 999;
                } else {
                    distanceMatrix[i][j] = (int) Math.round(matrix[i][j]);
                }
            }
        }

        topHeaders.getChildren().clear();
        leftHeaders.getChildren().clear();
        drawHeaders();
        drawMatrix();
        syncHeaderPositions();
    }
}