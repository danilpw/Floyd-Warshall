package com.example.floidyolsheer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GraphIO {

    private static final String DELIMITER = ";";

    public static void saveToFile(Graph graph, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            List<Vertex> vertices = graph.getVertices();
            int n = vertices.size();

            writer.println("vertices=" + n);

            for (Vertex v : vertices) {
                writer.println("v" + DELIMITER + v.getId() + DELIMITER + v.getName() +
                        DELIMITER + v.getX() + DELIMITER + v.getY());
            }

            List<Edge> edgesToSave = new ArrayList<>();
            Set<String> processed = new HashSet<>();

            for (Edge edge : graph.getEdges()) {
                if (edge.isDirected()) {
                    String key = edge.getSource().getId() + "->" + edge.getTarget().getId();
                    if (!processed.contains(key)) {
                        edgesToSave.add(edge);
                        processed.add(key);
                    }
                } else {
                    int from = edge.getSource().getId();
                    int to = edge.getTarget().getId();
                    String key = Math.min(from, to) + "<->" + Math.max(from, to);
                    if (!processed.contains(key)) {
                        Edge opposite = findOppositeEdge(graph, edge);
                        if (opposite != null && opposite.getWeight() == edge.getWeight()) {
                            edgesToSave.add(edge);
                        } else {
                            edgesToSave.add(new Edge(edge.getId(), edge.getSource(), edge.getTarget(),
                                    edge.getWeight(), true));
                            if (opposite != null) {
                                Edge oppositeCopy = new Edge(opposite.getId(), opposite.getSource(),
                                        opposite.getTarget(), opposite.getWeight(), true);
                                edgesToSave.add(oppositeCopy);
                            }
                        }
                        processed.add(key);
                    }
                }
            }

            Set<String> uniqueKeys = new HashSet<>();
            List<Edge> finalEdges = new ArrayList<>();
            for (Edge edge : edgesToSave) {
                String key = edge.getSource().getId() + "->" + edge.getTarget().getId() +
                        ":" + edge.getWeight() + ":" + edge.isDirected();
                if (!uniqueKeys.contains(key)) {
                    finalEdges.add(edge);
                    uniqueKeys.add(key);
                }
            }

            writer.println("edges=" + finalEdges.size());

            for (Edge edge : finalEdges) {
                writer.println("e" + DELIMITER + edge.getSource().getId() + DELIMITER +
                        edge.getTarget().getId() + DELIMITER + edge.getWeight() +
                        DELIMITER + edge.isDirected());
            }
        }
    }

    private static Edge findOppositeEdge(Graph graph, Edge edge) {
        for (Edge e : graph.getEdges()) {
            if (e != edge &&
                    e.getSource().equals(edge.getTarget()) &&
                    e.getTarget().equals(edge.getSource())) {
                return e;
            }
        }
        return null;
    }

    public static Graph loadFromFile(File file) throws IOException {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        }

        if (lines.isEmpty()) {
            throw new IOException("Файл пуст");
        }

        int vertexCount = 0;
        int edgeCount = 0;
        List<Vertex> vertices = new ArrayList<>();
        List<EdgeData> edgeDataList = new ArrayList<>();
        boolean coordinatesMissing = false;

        int state = 0;
        String fieldSplitRegex = "[;,]";

        for (String line : lines) {
            if (line.startsWith("vertices=")) {
                String[] countParts = line.substring(9).split(fieldSplitRegex, -1);
                try {
                    vertexCount = Integer.parseInt(countParts[0].trim());
                } catch (NumberFormatException ex) {
                    throw new IOException("Неверный формат файла: некорректное число вершин: " + line);
                }
                state = 1;
                continue;
            }

            if (line.startsWith("v")) {
                if (state != 1) {
                    throw new IOException("Неверный формат файла: ожидались вершины");
                }
                String[] parts = line.split(fieldSplitRegex, -1);
                if (parts.length < 3) {
                    throw new IOException("Неверный формат вершины: " + line);
                }
                int id;
                try {
                    id = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException ex) {
                    throw new IOException("Неверный формат вершины: " + line);
                }
                String name = parts[2];
                double x = 0;
                double y = 0;
                boolean hasCoords = false;
                if (parts.length >= 5) {
                    try {
                        x = Double.parseDouble(parts[3].trim());
                        y = Double.parseDouble(parts[4].trim());
                        hasCoords = true;
                    } catch (NumberFormatException ex) {
                        hasCoords = false;
                    }
                }
                if (!hasCoords) {
                    coordinatesMissing = true;
                }
                Vertex vertex = new Vertex(id, name, x, y);
                vertices.add(vertex);
                continue;
            }

            if (line.startsWith("edges=")) {
                String[] countParts = line.substring(6).split(fieldSplitRegex, -1);
                try {
                    edgeCount = Integer.parseInt(countParts[0].trim());
                } catch (NumberFormatException ex) {
                    throw new IOException("Неверный формат файла: некорректное число рёбер: " + line);
                }
                state = 2;
                continue;
            }

            if (line.startsWith("e")) {
                if (state != 2 && state != 3) {
                    throw new IOException("Неверный формат файла: ожидались ребра");
                }
                state = 3;
                String[] parts = line.split(fieldSplitRegex, -1);
                if (parts.length != 5) {
                    throw new IOException("Неверный формат ребра: " + line);
                }
                int fromId = Integer.parseInt(parts[1].trim());
                int toId = Integer.parseInt(parts[2].trim());
                int weight = Integer.parseInt(parts[3].trim());
                boolean isDirected = Boolean.parseBoolean(parts[4].trim());
                edgeDataList.add(new EdgeData(fromId, toId, weight, isDirected));
            }
        }

        if (vertices.size() != vertexCount) {
            throw new IOException("Несоответствие количества вершин: ожидалось " + vertexCount +
                    ", загружено " + vertices.size());
        }

        Graph graph = new Graph(false);

        vertices.sort(Comparator.comparingInt(Vertex::getId));

        if (coordinatesMissing) {
            applyCircularLayout(vertices);
        }

        for (Vertex v : vertices) {
            Vertex newVertex = graph.addVertex(v.getName(), v.getX(), v.getY());
            newVertex.setId(v.getId());
            newVertex.setHighlighted(v.isHighlighted());
        }

        Map<Integer, Vertex> vertexMap = new HashMap<>();
        for (Vertex v : graph.getVertices()) {
            vertexMap.put(v.getId(), v);
        }

        for (EdgeData edgeData : edgeDataList) {
            Vertex source = vertexMap.get(edgeData.fromId);
            Vertex target = vertexMap.get(edgeData.toId);
            if (source == null || target == null) {
                throw new IOException("Ребро ссылается на несуществующую вершину: " +
                        edgeData.fromId + " -> " + edgeData.toId);
            }

            graph.addEdge(source, target, edgeData.weight, edgeData.isDirected);
        }

        return graph;
    }

    private static final double WORKSPACE_CENTER_X = 600;
    private static final double WORKSPACE_CENTER_Y = 250;
    private static final double LAYOUT_RADIUS = 200;

    private static void applyCircularLayout(List<Vertex> vertices) {
        int n = vertices.size();
        if (n == 0) {
            return;
        }
        if (n == 1) {
            vertices.get(0).setX(WORKSPACE_CENTER_X);
            vertices.get(0).setY(WORKSPACE_CENTER_Y);
            return;
        }
        double angleStep = 2 * Math.PI / n;
        for (int i = 0; i < n; i++) {
            double angle = -Math.PI / 2 + i * angleStep;
            double x = WORKSPACE_CENTER_X + LAYOUT_RADIUS * Math.cos(angle);
            double y = WORKSPACE_CENTER_Y + LAYOUT_RADIUS * Math.sin(angle);
            vertices.get(i).setX(x);
            vertices.get(i).setY(y);
        }
    }

    public static boolean saveGraph(Graph graph, javafx.stage.Window owner) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Сохранить граф");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("CSV файлы", "*.csv")
        );
        fileChooser.setInitialFileName("graph.csv");

        File file = fileChooser.showSaveDialog(owner);
        if (file == null) {
            return false;
        }

        try {
            saveToFile(graph, file);
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION
            );
            alert.setTitle("Сохранение");
            alert.setHeaderText(null);
            alert.setContentText("Граф успешно сохранен в файл:\n" + file.getAbsolutePath());
            alert.showAndWait();
            return true;
        } catch (IOException e) {
            showError("Ошибка при сохранении файла", e.getMessage());
            return false;
        }
    }

    public static Graph loadGraph(javafx.stage.Window owner) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Загрузить граф");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("CSV файлы", "*.csv")
        );

        File file = fileChooser.showOpenDialog(owner);
        if (file == null) {
            return null;
        }

        if (!file.getName().toLowerCase().endsWith(".csv")) {
            showError("Неверный формат файла",
                    "Ожидается файл с расширением .csv:\n" + file.getName() +
                            "\n\nВыберите файл, ранее сохранённый через кнопку «Сохранить».");
            return null;
        }

        try {
            Graph graph = loadFromFile(file);

            if (graph.getVertexCount() == 0 && graph.getEdgeCount() == 0) {
                showError("Файл не содержит граф",
                        "В файле не найдено ни одной вершины, ни одного ребра:\n" + file.getAbsolutePath() +
                                "\n\nПроверьте содержимое файла или выберите другой файл, ранее сохранённый через кнопку «Сохранить».");
                return null;
            }

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION
            );
            alert.setTitle("Загрузка");
            alert.setHeaderText(null);
            alert.setContentText("Граф успешно загружен из файла:\n" + file.getAbsolutePath());
            alert.showAndWait();
            return graph;
        } catch (Exception e) {
            showError("Ошибка при загрузке файла",
                    "Файл повреждён или имеет неверный формат:\n" + e.getMessage() +
                            "\n\nПроверьте содержимое файла или выберите другой файл, ранее сохранённый через кнопку «Сохранить».");
            return null;
        }
    }

    private static void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class EdgeData {
        int fromId;
        int toId;
        int weight;
        boolean isDirected;

        EdgeData(int fromId, int toId, int weight, boolean isDirected) {
            this.fromId = fromId;
            this.toId = toId;
            this.weight = weight;
            this.isDirected = isDirected;
        }
    }
}