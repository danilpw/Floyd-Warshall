package com.example.floidyolsheer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Graph {
    private List<Vertex> vertices;
    private List<Edge> edges;
    private int nextVertexId;
    private int nextEdgeId;
    private boolean directed;

    public Graph(boolean directed) {
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.nextVertexId = 0;
        this.nextEdgeId = 0;
        this.directed = directed;
    }

    public Vertex addVertex(String name, double x, double y) {
        Vertex vertex = new Vertex(nextVertexId++, name, x, y);
        vertices.add(vertex);
        return vertex;
    }

    public boolean removeVertex(Vertex vertex) {
        if (!vertices.contains(vertex)) return false;

        edges.removeIf(edge ->
                edge.getSource().equals(vertex) || edge.getTarget().equals(vertex)
        );

        vertices.remove(vertex);
        renumberAll();

        return true;
    }

    private void renumberAll() {
        for (int i = 0; i < vertices.size(); i++) {
            Vertex v = vertices.get(i);
            v.setId(i);
            v.setName(String.valueOf(i + 1));
        }
        nextVertexId = vertices.size();
    }

    public Optional<Vertex> getVertexById(int id) {
        return vertices.stream().filter(v -> v.getId() == id).findFirst();
    }

    public List<Vertex> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    public int getVertexCount() {
        return vertices.size();
    }

    public Edge addEdge(Vertex source, Vertex target, int weight) {
        if (!vertices.contains(source) || !vertices.contains(target)) {
            throw new IllegalArgumentException("Vertex not in graph");
        }
        if (source.equals(target)) {
            throw new IllegalArgumentException("Cannot add edge to itself");
        }

        Edge edge = new Edge(nextEdgeId++, source, target, weight, directed);
        edges.add(edge);

        if (!directed) {
            Edge reverseEdge = new Edge(nextEdgeId++, target, source, weight, false);
            edges.add(reverseEdge);
        }

        return edge;
    }

    public Edge addEdge(Vertex source, Vertex target, int weight, boolean isDirected) {
        if (!vertices.contains(source) || !vertices.contains(target)) {
            throw new IllegalArgumentException("Vertex not in graph");
        }
        if (source.equals(target)) {
            throw new IllegalArgumentException("Cannot add edge to itself");
        }

        Edge edge = new Edge(nextEdgeId++, source, target, weight, isDirected);
        edges.add(edge);

        if (!isDirected) {
            Edge reverseEdge = new Edge(nextEdgeId++, target, source, weight, false);
            edges.add(reverseEdge);
        }

        return edge;
    }

    public Edge addEdge(int sourceId, int targetId, int weight) {
        Optional<Vertex> source = getVertexById(sourceId);
        Optional<Vertex> target = getVertexById(targetId);

        if (source.isPresent() && target.isPresent()) {
            return addEdge(source.get(), target.get(), weight);
        }
        throw new IllegalArgumentException("Vertex not found");
    }

    public Edge addEdge(int sourceId, int targetId, int weight, boolean isDirected) {
        Optional<Vertex> source = getVertexById(sourceId);
        Optional<Vertex> target = getVertexById(targetId);

        if (source.isPresent() && target.isPresent()) {
            return addEdge(source.get(), target.get(), weight, isDirected);
        }
        throw new IllegalArgumentException("Vertex not found");
    }

    public boolean removeEdge(Edge edge) {
        boolean removed = edges.remove(edge);
        if (removed && !edge.isDirected()) {

            edges.removeIf(e ->
                    e.getSource().equals(edge.getTarget()) &&
                            e.getTarget().equals(edge.getSource()) &&
                            e.getWeight() == edge.getWeight()
            );
        }
        return removed;
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public List<Edge> getUniqueEdges() {
        Set<String> uniqueKeys = new HashSet<>();
        List<Edge> uniqueEdges = new ArrayList<>();

        for (Edge edge : edges) {

            if (edge.isDirected()) {
                uniqueEdges.add(edge);
                continue;
            }

            String key;
            String v1 = edge.getSource().getName();
            String v2 = edge.getTarget().getName();
            if (v1.compareTo(v2) <= 0) {
                key = v1 + "-" + v2;
            } else {
                key = v2 + "-" + v1;
            }

            if (!uniqueKeys.contains(key)) {
                uniqueKeys.add(key);
                uniqueEdges.add(edge);
            }
        }

        return Collections.unmodifiableList(uniqueEdges);
    }

    public int getEdgeCount() {
        return getUniqueEdges().size();
    }

    public boolean isDirected() {
        return directed;
    }

    public void clear() {
        vertices.clear();
        edges.clear();
        nextVertexId = 0;
        nextEdgeId = 0;
    }

    public double[][] getAdjacencyMatrix() {
        int n = vertices.size();
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = Double.POSITIVE_INFINITY;
            }
            matrix[i][i] = 0;
        }

        for (Edge edge : edges) {
            int i = vertices.indexOf(edge.getSource());
            int j = vertices.indexOf(edge.getTarget());
            if (i >= 0 && j >= 0) {
                matrix[i][j] = Math.min(matrix[i][j], edge.getWeight());
            }
        }
        return matrix;
    }
}