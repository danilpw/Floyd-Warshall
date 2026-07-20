package com.example.floidyolsheer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.VPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GraphEditor extends StackPane {

    private Canvas canvas;
    private GraphicsContext gc;
    private Graph graph;
    private double canvasWidth = 1200;
    private double canvasHeight = 500;

    private Button edgeModeButton;
    private Button vertexModeButton;

    private double panOffsetX = 0;
    private double panOffsetY = 0;
    private double dragAnchorX;
    private double dragAnchorY;

    private EditorMode currentMode = EditorMode.ADD_VERTEX;
    private Vertex selectedVertex = null;
    private Vertex firstVertexForEdge = null;

    private double scale = 1.0;
    private final double MIN_SCALE = 0.5;
    private final double MAX_SCALE = 2.0;
    private final double ZOOM_SPEED = 0.1;

    private double mousePressX;
    private double mousePressY;
    private boolean isDragging = false;
    private static final double DRAG_THRESHOLD = 5;

    private Runnable onGraphChangedListener;

    private static final double VERTEX_RADIUS = 22;
    private boolean lastEdgeDirected = false;

    private boolean isLocked = false;

    public GraphEditor() {
        canvas = new Canvas(canvasWidth, canvasHeight);
        gc = canvas.getGraphicsContext2D();
        graph = new Graph(false);
        setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1;");

        HBox toolbar = createToolbar();
        StackPane canvasContainer = new StackPane(canvas);
        canvasContainer.setStyle("-fx-background-color: white;");

        BorderPane layout = new BorderPane();
        layout.setTop(toolbar);
        layout.setCenter(canvasContainer);

        getChildren().add(layout);

        setupMouseClicks();
        setupZooming();

        updateModeButtons();
        draw();
    }

    public void setLocked(boolean locked) {
        this.isLocked = locked;
        edgeModeButton.setDisable(locked);
        vertexModeButton.setDisable(locked);
        canvas.setCursor(locked ? javafx.scene.Cursor.DEFAULT : javafx.scene.Cursor.DEFAULT);
    }

    public void setOnGraphChangedListener(Runnable listener) {
        this.onGraphChangedListener = listener;
    }

    private void notifyGraphChanged() {
        if (onGraphChangedListener != null) {
            onGraphChangedListener.run();
        }
    }

    private void setupMouseClicks() {
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
    }

    private void handleMouseDragged(MouseEvent e) {
        if (isLocked) {
            double deltaX = e.getX() - dragAnchorX;
            double deltaY = e.getY() - dragAnchorY;
            panOffsetX += deltaX;
            panOffsetY += deltaY;
            dragAnchorX = e.getX();
            dragAnchorY = e.getY();
            draw();
            return;
        }

        double dx = e.getX() - mousePressX;
        double dy = e.getY() - mousePressY;
        if (Math.sqrt(dx * dx + dy * dy) > DRAG_THRESHOLD) {
            isDragging = true;
        }

        if (currentMode == EditorMode.MOVE && selectedVertex != null) {
            double worldX = (e.getX() - panOffsetX) / scale;
            double worldY = (e.getY() - panOffsetY) / scale;
            selectedVertex.setX(worldX);
            selectedVertex.setY(worldY);
            draw();
            return;
        }

        double deltaX = e.getX() - dragAnchorX;
        double deltaY = e.getY() - dragAnchorY;
        panOffsetX += deltaX;
        panOffsetY += deltaY;
        dragAnchorX = e.getX();
        dragAnchorY = e.getY();
        draw();
    }

    private void handleMousePressed(MouseEvent e) {
        mousePressX = e.getX();
        mousePressY = e.getY();
        isDragging = false;
        dragAnchorX = e.getX();
        dragAnchorY = e.getY();
    }

    private void handleMouseReleased(MouseEvent e) {
        if (isLocked) {
            return;
        }

        if (isDragging) {
            return;
        }

        double x = e.getX();
        double y = e.getY();

        double worldX = (x - panOffsetX) / scale;
        double worldY = (y - panOffsetY) / scale;

        switch (currentMode) {
            case ADD_VERTEX:
                handleAddVertexMode(worldX, worldY);
                break;
            case ADD_EDGE:
                handleAddEdgeMode(worldX, worldY);
                break;
            case SELECT:
                handleSelectMode(worldX, worldY);
                break;
            case MOVE:
                break;
            case DELETE:
                break;
        }
    }

    private void handleSelectMode(double x, double y) {
        if (isLocked) return;

        Edge clickedEdge = findEdgeAt(x, y);
        if (clickedEdge != null) {
            showEdgeOptions(clickedEdge);
            return;
        }

        Vertex clickedVertex = findVertexAt(x, y);
        if (clickedVertex != null) {
            showVertexOptions(clickedVertex);
            return;
        }
    }

    private void showVertexOptions(Vertex vertex) {
        if (isLocked) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Действие с вершиной");
        alert.setHeaderText("Вершина " + vertex.getName());
        alert.setContentText("Выберите действие:");

        ButtonType deleteButton = new ButtonType("Удалить вершину");
        ButtonType cancelButton = new ButtonType("Отмена");

        alert.getButtonTypes().setAll(deleteButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == deleteButton) {
            graph.removeVertex(vertex);
            draw();
            notifyGraphChanged();
        }
    }

    private void showEdgeOptions(Edge edge) {
        if (isLocked) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Действие с ребром");

        Edge opposite = findOppositeEdge(edge);
        String oppositeInfo = "";
        if (opposite != null) {
            oppositeInfo = "\nПротивоположное: " + opposite.getSource().getName() + " -> " + opposite.getTarget().getName() +
                    " (вес: " + opposite.getWeight() + ")";
        }

        String typeInfo = "";
        if (opposite != null) {
            if (opposite.getWeight() == edge.getWeight()) {
                typeInfo = "\nТип: Неориентированное (вес одинаковый)";
            } else {
                typeInfo = "\nТип: Ориентированное в обе стороны (веса разные)";
            }
        } else if (edge.isDirected()) {
            typeInfo = "\nТип: Ориентированное";
        } else {
            typeInfo = "\nТип: Неориентированное";
        }

        alert.setHeaderText("Ребро " + edge.getSource().getName() + " -> " + edge.getTarget().getName());
        alert.setContentText("Вес: " + edge.getWeight() +
                typeInfo +
                oppositeInfo +
                "\n\nВыберите действие:");

        ButtonType deleteButton = new ButtonType("Удалить ребро");
        ButtonType changeWeightButton = new ButtonType("Изменить вес");
        ButtonType oppositeButton = new ButtonType("К противоположному ребру");
        ButtonType cancelButton = new ButtonType("Отмена");

        alert.getButtonTypes().setAll(deleteButton, changeWeightButton, oppositeButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == deleteButton) {
                Edge opp = findOppositeEdge(edge);
                graph.removeEdge(edge);
                if (opp != null) {
                    graph.removeEdge(opp);
                }
                draw();
                notifyGraphChanged();
            } else if (result.get() == changeWeightButton) {
                showChangeWeightDialog(edge);
            } else if (result.get() == oppositeButton) {
                if (opposite != null) {
                    showEdgeOptions(opposite);
                } else {
                    showError("Противоположное ребро не найдено");
                }
            }
        }
    }

    private Edge findOppositeEdge(Edge edge) {
        for (Edge e : graph.getEdges()) {
            if (e.getSource().equals(edge.getTarget()) && e.getTarget().equals(edge.getSource()) && e != edge) {
                return e;
            }
        }
        return null;
    }

    private void showChangeWeightDialog(Edge edge) {
        if (isLocked) return;

        TextInputDialog dialog = new TextInputDialog(String.valueOf(edge.getWeight()));
        dialog.setTitle("Изменение веса");
        dialog.setHeaderText("Ребро " + edge.getSource().getName() + " -> " + edge.getTarget().getName());
        dialog.setContentText("Введите новый вес:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int newWeight = Integer.parseInt(result.get());
                Edge opposite = findOppositeEdge(edge);

                if (opposite != null) {
                    if (opposite.getWeight() == newWeight) {
                        graph.removeEdge(edge);
                        graph.removeEdge(opposite);
                        graph.addEdge(edge.getSource(), edge.getTarget(), newWeight, false);
                    } else {
                        edge.setWeight(newWeight);
                    }
                } else {
                    graph.removeEdge(edge);
                    graph.addEdge(edge.getSource(), edge.getTarget(), newWeight, true);
                }

                draw();
                notifyGraphChanged();
            } catch (NumberFormatException e) {
                showError("Введите целое число");
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleAddVertexMode(double x, double y) {
        if (isLocked) return;

        Vertex existingVertex = findVertexAt(x, y);
        if (existingVertex != null) {
            showVertexOptions(existingVertex);
            return;
        }

        String name = String.valueOf(graph.getVertexCount() + 1);
        graph.addVertex(name, x, y);
        draw();
        notifyGraphChanged();
    }

    private void handleAddEdgeMode(double x, double y) {
        if (isLocked) return;

        Vertex clicked = findVertexAt(x, y);

        if (clicked != null) {
            if (firstVertexForEdge == null) {
                firstVertexForEdge = clicked;
                clicked.setHighlighted(true);
                draw();
            } else {
                if (firstVertexForEdge != clicked) {
                    try {
                        boolean isDirected = askEdgeType();

                        if (isDirected) {
                            graph.addEdge(firstVertexForEdge, clicked, 1, true);
                        } else {
                            graph.addEdge(firstVertexForEdge, clicked, 1, false);
                        }
                        lastEdgeDirected = isDirected;
                        draw();
                        notifyGraphChanged();
                    } catch (IllegalArgumentException ex) {
                        showError("Ошибка при создании ребра: " + ex.getMessage());
                    }
                } else {
                    showError("Нельзя создать ребро из вершины в саму себя");
                }

                firstVertexForEdge.setHighlighted(false);
                firstVertexForEdge = null;
                draw();
            }
            return;
        }

        Edge clickedEdge = findEdgeAt(x, y);
        if (clickedEdge != null) {
            if (firstVertexForEdge != null) {
                firstVertexForEdge.setHighlighted(false);
                firstVertexForEdge = null;
                draw();
            }
            showEdgeOptions(clickedEdge);
            return;
        }

        if (firstVertexForEdge != null) {
            firstVertexForEdge.setHighlighted(false);
            firstVertexForEdge = null;
            draw();
        }
    }

    private boolean askEdgeType() {
        List<String> choices = new ArrayList<>();
        choices.add("Неориентированное");
        choices.add("Ориентированное");

        String defaultChoice = lastEdgeDirected ? "Ориентированное" : "Неориентированное";
        ChoiceDialog<String> dialog = new ChoiceDialog<>(defaultChoice, choices);
        dialog.setTitle("Тип ребра");
        dialog.setHeaderText("Выберите тип создаваемого ребра");
        dialog.setContentText("Тип:");

        Optional<String> result = dialog.showAndWait();
        return result.isPresent() && result.get().equals("Ориентированное");
    }

    private Vertex findVertexAt(double x, double y) {
        double radius = VERTEX_RADIUS + 8;
        for (Vertex v : graph.getVertices()) {
            double dx = v.getX() - x;
            double dy = v.getY() - y;
            if (dx * dx + dy * dy <= radius * radius) {
                return v;
            }
        }
        return null;
    }

    private Edge findEdgeAt(double x, double y) {
        double threshold = 15;
        Set<String> processedEdges = new HashSet<>();

        for (Edge edge : graph.getEdges()) {
            Edge opposite = findOppositeEdge(edge);
            if (opposite != null && opposite.getWeight() == edge.getWeight()) {
                String edgeKey = "U:" + Math.min(edge.getSource().getId(), edge.getTarget().getId()) + "-" +
                        Math.max(edge.getSource().getId(), edge.getTarget().getId());
                if (processedEdges.contains(edgeKey)) {
                    continue;
                }
                Vertex from = edge.getSource();
                Vertex to = edge.getTarget();
                double d = distanceToLineSegment(x, y, from.getX(), from.getY(), to.getX(), to.getY());
                if (d < threshold) {
                    processedEdges.add(edgeKey);
                    return edge;
                }
                processedEdges.add(edgeKey);
            } else {
                String edgeKey = "D:" + edge.getSource().getId() + "-" + edge.getTarget().getId();
                if (processedEdges.contains(edgeKey)) {
                    continue;
                }
                Vertex from = edge.getSource();
                Vertex to = edge.getTarget();
                double d = distanceToLineSegment(x, y, from.getX(), from.getY(), to.getX(), to.getY());
                if (d < threshold) {
                    processedEdges.add(edgeKey);
                    return edge;
                }
                processedEdges.add(edgeKey);
            }
        }
        return null;
    }

    private double distanceToLineSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lenSq = dx * dx + dy * dy;

        if (lenSq == 0) {
            return Math.sqrt((px - x1) * (px - x1) + (py - y1) * (py - y1));
        }

        double t = ((px - x1) * dx + (py - y1) * dy) / lenSq;
        t = Math.max(0, Math.min(1, t));

        double projX = x1 + t * dx;
        double projY = y1 + t * dy;

        return Math.sqrt((px - projX) * (px - projX) + (py - projY) * (py - projY));
    }

    private void setupZooming() {
        canvas.setOnScroll(this::handleZoom);
        this.setOnScroll(this::handleZoom);
    }

    private void handleZoom(ScrollEvent event) {
        double delta = event.getDeltaY();
        if (delta > 0) {
            scale = Math.min(scale + ZOOM_SPEED, MAX_SCALE);
        } else if (delta < 0) {
            scale = Math.max(scale - ZOOM_SPEED, MIN_SCALE);
        }
        event.consume();
        draw();
    }

    private HBox createToolbar() {
        edgeModeButton = new Button("Режим рёбер");
        vertexModeButton = new Button("Режим вершин");

        edgeModeButton.setOnAction(e -> setMode(EditorMode.ADD_EDGE));
        vertexModeButton.setOnAction(e -> setMode(EditorMode.ADD_VERTEX));

        HBox toolbar = new HBox(10, edgeModeButton, vertexModeButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(5, 5, 5, 5));
        return toolbar;
    }

    public void setMode(EditorMode mode) {
        if (isLocked) return;

        this.currentMode = mode;

        if (firstVertexForEdge != null) {
            firstVertexForEdge.setHighlighted(false);
            firstVertexForEdge = null;
        }

        updateModeButtons();
        draw();
    }

    private void updateModeButtons() {
        String activeStyle = "-fx-background-color: #2E76A3;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 8 15 8 15;" +
                "-fx-background-radius: 5;";

        String inactiveStyle = "-fx-background-color: #53A0CF;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 8 15 8 15;" +
                "-fx-background-radius: 5;";

        edgeModeButton.setStyle(currentMode == EditorMode.ADD_EDGE ? activeStyle : inactiveStyle);
        vertexModeButton.setStyle(currentMode == EditorMode.ADD_VERTEX ? activeStyle : inactiveStyle);
    }

    public void draw() {
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        gc.save();
        gc.translate(panOffsetX, panOffsetY);
        gc.scale(scale, scale);

        drawGrid();

        Set<String> drawnEdges = new HashSet<>();
        for (Edge edge : graph.getEdges()) {
            Edge opposite = findOppositeEdge(edge);

            if (opposite != null && opposite.getWeight() == edge.getWeight()) {
                String edgeKey = "U:" + Math.min(edge.getSource().getId(), edge.getTarget().getId()) + "-" +
                        Math.max(edge.getSource().getId(), edge.getTarget().getId());
                if (!drawnEdges.contains(edgeKey)) {
                    drawEdge(edge, false);
                    drawnEdges.add(edgeKey);
                }
            } else {
                String edgeKey = "D:" + edge.getSource().getId() + "-" + edge.getTarget().getId();
                if (!drawnEdges.contains(edgeKey)) {
                    drawEdge(edge, true);
                    drawnEdges.add(edgeKey);
                }
            }
        }

        for (Vertex vertex : graph.getVertices()) {
            drawVertex(vertex);
        }

        gc.restore();
    }

    private void drawGrid() {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5 / scale);

        double startX = -panOffsetX / scale;
        double endX = (canvasWidth - panOffsetX) / scale;
        double startY = -panOffsetY / scale;
        double endY = (canvasHeight - panOffsetY) / scale;

        startX = Math.floor(startX / 50) * 50;
        startY = Math.floor(startY / 50) * 50;
        endX = Math.ceil(endX / 50) * 50;
        endY = Math.ceil(endY / 50) * 50;

        for (double x = startX; x <= endX; x += 50) {
            gc.strokeLine(x, startY, x, endY);
        }

        for (double y = startY; y <= endY; y += 50) {
            gc.strokeLine(startX, y, endX, y);
        }
    }

    private void drawVertex(Vertex vertex) {
        double x = vertex.getX();
        double y = vertex.getY();
        double radius = VERTEX_RADIUS;

        gc.setFill(Color.rgb(0, 0, 0, 0.1));
        gc.fillOval(x - radius + 3, y - radius + 3, radius * 2, radius * 2);

        String customColor = vertex.getHighlightColor();
        Color fillColor;
        Color strokeColor;
        if (vertex.isHighlighted() && customColor != null) {
            fillColor = Color.web(customColor);
            strokeColor = Color.web(customColor).darker();
        } else if (vertex.isHighlighted()) {
            fillColor = Color.web("#FF6B35");
            strokeColor = Color.web("#CC5500");
        } else {
            fillColor = Color.web("#2E76A3");
            strokeColor = Color.web("#1A425B");
        }
        gc.setFill(fillColor);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        gc.setStroke(strokeColor);
        gc.setLineWidth(vertex.isHighlighted() ? 4 : 3);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(vertex.getName(), x, y);
    }

    private void drawEdge(Edge edge, boolean showArrow) {
        Vertex from = edge.getSource();
        Vertex to = edge.getTarget();

        double fromX = from.getX();
        double fromY = from.getY();
        double toX = to.getX();
        double toY = to.getY();

        double angle = Math.atan2(toY - fromY, toX - fromX);
        double radius = VERTEX_RADIUS;

        double startX = fromX + radius * Math.cos(angle);
        double startY = fromY + radius * Math.sin(angle);
        double endX = toX - radius * Math.cos(angle);
        double endY = toY - radius * Math.sin(angle);

        Color edgeColor;
        if (edge.isHighlighted() && edge.getHighlightColor() != null) {
            edgeColor = Color.web(edge.getHighlightColor());
        } else if (edge.isHighlighted()) {
            edgeColor = Color.RED;
        } else {
            edgeColor = Color.web("#333333");
        }

        gc.setStroke(edgeColor);
        gc.setLineWidth(edge.isHighlighted() ? 4 : 2.5);
        gc.strokeLine(startX, startY, endX, endY);

        if (showArrow) {
            drawArrow(startX, startY, endX, endY, angle, edgeColor);
        }

        double midX = (startX + endX) / 2;
        double midY = (startY + endY) / 2;
        double offsetX = -Math.sin(angle) * 9;
        double offsetY = Math.cos(angle) * 9;

        String weightText = String.valueOf(edge.getWeight());

        gc.setFill(Color.web("#1A425B"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(weightText, midX + offsetX, midY + offsetY);
    }

    private void drawArrow(double startX, double startY, double endX, double endY, double angle, Color color) {
        double arrowSize = 12;
        double arrowAngle = Math.PI / 6;

        double tipX = endX;
        double tipY = endY;

        double leftX = tipX - arrowSize * Math.cos(angle - arrowAngle);
        double leftY = tipY - arrowSize * Math.sin(angle - arrowAngle);
        double rightX = tipX - arrowSize * Math.cos(angle + arrowAngle);
        double rightY = tipY - arrowSize * Math.sin(angle + arrowAngle);

        gc.setFill(color);
        gc.setStroke(color);
        gc.setLineWidth(2.5);

        double[] xPoints = {tipX, leftX, rightX};
        double[] yPoints = {tipY, leftY, rightY};

        gc.fillPolygon(xPoints, yPoints, 3);
        gc.strokePolygon(xPoints, yPoints, 3);
    }

    public void highlightEdges(Vertex from, Vertex to, String color) {
        for (Edge edge : graph.getEdges()) {
            if ((edge.getSource().equals(from) && edge.getTarget().equals(to)) ||
                    (edge.getSource().equals(to) && edge.getTarget().equals(from))) {
                edge.setHighlighted(true);
                edge.setHighlightColor(color);
            }
        }
        draw();
    }

    public void highlightVertex(Vertex vertex, boolean highlight) {
        if (vertex != null) {
            vertex.setHighlighted(highlight);
            if (!highlight) {
                vertex.setHighlightColor(null);
            }
            draw();
        }
    }

    public void highlightVertex(Vertex vertex, String color) {
        if (vertex != null) {
            vertex.setHighlighted(true);
            vertex.setHighlightColor(color);
            draw();
        }
    }

    public void clearHighlight() {
        for (Edge edge : graph.getEdges()) {
            edge.setHighlighted(false);
            edge.setHighlightColor(null);
        }
        for (Vertex vertex : graph.getVertices()) {
            vertex.setHighlighted(false);
            vertex.setHighlightColor(null);
        }
        draw();
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        selectedVertex = null;
        firstVertexForEdge = null;
        draw();
        notifyGraphChanged();
    }
}