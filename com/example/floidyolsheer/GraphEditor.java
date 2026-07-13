package com.example.floidyolsheer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;

public class GraphEditor extends StackPane {

    private Canvas canvas;
    private GraphicsContext gc;
    public List<Vertex> vertices;
    private List<Edge> edges;
    private double canvasWidth = 1200;
    private double canvasHeight = 500;

    private Button edgeModeButton;
    private Button vertexModeButton;

    private double panOffsetX = 0;
    private double panOffsetY = 0;
    private double dragAnchorX;
    private double dragAnchorY;
    

    private double scale = 1.0;
    private final double MIN_SCALE = 0.5;
    private final double MAX_SCALE = 2.0;
    private final double ZOOM_SPEED = 0.1;

    public GraphEditor() {
        canvas = new Canvas(canvasWidth, canvasHeight);
        gc = canvas.getGraphicsContext2D();
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
        setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1;");

        HBox toolbar = createToolbar();
        StackPane canvasContainer = new StackPane(canvas);
        canvasContainer.setStyle("-fx-background-color: white;");

        BorderPane layout = new BorderPane();
        layout.setTop(toolbar);
        layout.setCenter(canvasContainer);

        getChildren().add(layout);

        setupPanning();
        setupZooming();

        initDemoGraph();
        draw();
    }

    private void setupPanning() {
        canvas.setOnMousePressed(this::handlePanPressed);
        canvas.setOnMouseDragged(this::handlePanDragged);
        canvas.setOnMouseReleased(this::handlePanReleased);
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

    private void handlePanPressed(MouseEvent e) {
        dragAnchorX = e.getX();
        dragAnchorY = e.getY();
        canvas.setCursor(Cursor.CLOSED_HAND);
    }

    private void handlePanDragged(MouseEvent e) {
        double deltaX = e.getX() - dragAnchorX;
        double deltaY = e.getY() - dragAnchorY;
        panOffsetX += deltaX;
        panOffsetY += deltaY;
        dragAnchorX = e.getX();
        dragAnchorY = e.getY();
        draw();
    }

    private void handlePanReleased(MouseEvent e) {
        canvas.setCursor(Cursor.DEFAULT);
    }

    private HBox createToolbar() {
        edgeModeButton = new Button("Режим ребер");
        vertexModeButton = new Button("Режим вершин");

        edgeModeButton.setStyle(
                "-fx-background-color: #53A0CF;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 15 8 15;" +
                        "-fx-background-radius: 5;"
        );

        vertexModeButton.setStyle(
                "-fx-background-color: #53A0CF;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 15 8 15;" +
                        "-fx-background-radius: 5;"
        );

        edgeModeButton.setOnAction(e -> onEdgeModeButtonClicked());
        vertexModeButton.setOnAction(e -> onVertexModeButtonClicked());

        HBox toolbar = new HBox(10, edgeModeButton, vertexModeButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(5, 5, 5, 5));
        return toolbar;
    }

    private void onEdgeModeButtonClicked() {
        System.out.println("Edge mode clicked");
    }

    private void onVertexModeButtonClicked() {
        System.out.println("Vertex mode clicked");
    }

    private void initDemoGraph() {

        double centerX = 600;
        double centerY = 250;
        double radius = 200;
        

        double[] angles = {
            -Math.PI / 2,          // 1 - верх
            -Math.PI / 2 + Math.PI / 3.5,  // 2
            -Math.PI / 2 + 2 * Math.PI / 3.5, // 3
            -Math.PI / 2 + 3 * Math.PI / 3.5, // 4
            -Math.PI / 2 + 4 * Math.PI / 3.5, // 5
            -Math.PI / 2 + 5 * Math.PI / 3.5, // 6
            -Math.PI / 2 + 6 * Math.PI / 3.5  // 7
        };
        
        Vertex v1 = new Vertex(0, "1", centerX + radius * Math.cos(angles[0]) - 35, centerY + radius * Math.sin(angles[0]));
        Vertex v2 = new Vertex(1, "2", centerX + radius * Math.cos(angles[1]), centerY + radius * Math.sin(angles[1]));
        Vertex v3 = new Vertex(2, "3", centerX + radius * Math.cos(angles[2]), centerY + radius * Math.sin(angles[2]));
        Vertex v4 = new Vertex(3, "4", centerX + radius * Math.cos(angles[3]), centerY + radius * Math.sin(angles[3]));
        Vertex v5 = new Vertex(4, "5", centerX + radius * Math.cos(angles[4]), centerY + radius * Math.sin(angles[4]));
        Vertex v6 = new Vertex(5, "6", centerX + radius * Math.cos(angles[5]), centerY + radius * Math.sin(angles[5]));
        Vertex v7 = new Vertex(6, "7", centerX + radius * Math.cos(angles[6]) - 30, centerY + radius * Math.sin(angles[6]) + 30);

        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);
        vertices.add(v5);
        vertices.add(v6);
        vertices.add(v7);


        edges.add(new Edge(v1, v2, 4, false));
        edges.add(new Edge(v1, v3, 8, false));
        edges.add(new Edge(v1, v5, 9, false));
        
        edges.add(new Edge(v2, v3, 5, false));
        edges.add(new Edge(v2, v4, 7, false));
        edges.add(new Edge(v2, v7, 10, false));
        
        edges.add(new Edge(v3, v4, 6, false));
        edges.add(new Edge(v3, v7, 6, false));
        
        edges.add(new Edge(v4, v6, 4, false));
        edges.add(new Edge(v4, v7, 2, false));
        
        edges.add(new Edge(v5, v6, 3, false));
    }

    public void draw() {
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        gc.save();
        

        gc.translate(panOffsetX, panOffsetY);
        gc.scale(scale, scale);

        drawGrid();


        for (Edge edge : edges) {
            drawEdge(edge);
        }


        for (Vertex vertex : vertices) {
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
        double radius = 30;


        gc.setFill(Color.rgb(0, 0, 0, 0.1));
        gc.fillOval(x - radius + 3, y - radius + 3, radius * 2, radius * 2);


        gc.setFill(Color.web("#2E76A3"));
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        

        gc.setStroke(Color.web("#1A425B"));
        gc.setLineWidth(3);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);


        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(vertex.getName(), x, y);
    }

    private void drawEdge(Edge edge) {
        Vertex from = edge.getFrom();
        Vertex to = edge.getTo();

        double fromX = from.getX();
        double fromY = from.getY();
        double toX = to.getX();
        double toY = to.getY();


        double angle = Math.atan2(toY - fromY, toX - fromX);
        double radius = 30;
        

        double startX = fromX + radius * Math.cos(angle);
        double startY = fromY + radius * Math.sin(angle);
        double endX = toX - radius * Math.cos(angle);
        double endY = toY - radius * Math.sin(angle);


        gc.setStroke(edge.isHighlighted() ? Color.RED : Color.web("#333333"));
        gc.setLineWidth(edge.isHighlighted() ? 4 : 2.5);
        gc.strokeLine(startX, startY, endX, endY);


        double midX = (startX + endX) / 2;
        double midY = (startY + endY) / 2;
        

        double offsetX = -Math.sin(angle) * 9;
        double offsetY = Math.cos(angle) * 9;


        gc.setFill(Color.web("#1A425B"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(String.valueOf(edge.getWeight()), midX + offsetX, midY + offsetY);
    }

    public void highlightEdge(Vertex from, Vertex to) {
        for (Edge edge : edges) {
            if ((edge.getFrom() == from && edge.getTo() == to) ||
                (edge.getFrom() == to && edge.getTo() == from)) {
                edge.setHighlighted(true);
            } else {
                edge.setHighlighted(false);
            }
        }
        draw();
    }

    public void clearHighlight() {
        for (Edge edge : edges) {
            edge.setHighlighted(false);
        }
        draw();
    }

    public void addVertex() {
        System.out.println("Add vertex");
    }

    public void addEdge() {
        System.out.println("Add edge");
    }

    public void moveVertex() {
        System.out.println("Move vertex");
    }

    public void deleteElement() {
        System.out.println("Delete element");
    }

    public static class Vertex {
        private int id;
        private String name;
        private double x;
        private double y;

        public Vertex(int id, String name, double x, double y) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public double getX() { return x; }
        public double getY() { return y; }
    }

    public static class Edge {
        private Vertex from;
        private Vertex to;
        private int weight;
        private boolean highlighted;

        public Edge(Vertex from, Vertex to, int weight, boolean highlighted) {
            this.from = from;
            this.to = to;
            this.weight = weight;
            this.highlighted = highlighted;
        }

        public Vertex getFrom() { return from; }
        public Vertex getTo() { return to; }
        public int getWeight() { return weight; }
        public boolean isHighlighted() { return highlighted; }
        public void setHighlighted(boolean highlighted) { this.highlighted = highlighted; }
    }
}