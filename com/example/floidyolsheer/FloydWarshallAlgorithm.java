package com.example.floidyolsheer;

import java.util.ArrayList;
import java.util.List;

public class FloydWarshallAlgorithm {

    private Graph graph;
    private int n;
    private double[][] distances;
    private int[][] next;
    private List<AlgorithmStep> steps;
    private boolean hasNegativeCycle;

    public FloydWarshallAlgorithm(Graph graph) {
        this.graph = graph;
        this.n = graph.getVertexCount();
        this.distances = new double[n][n];
        this.next = new int[n][n];
        this.steps = new ArrayList<>();
        this.hasNegativeCycle = false;
    }

    public List<AlgorithmStep> execute() {

        initializeMatrices();

        steps.add(new AlgorithmStep(-1, -1, -1, copyDistances(),
                "Инициализация матрицы расстояний из графа"));

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i == k || k == j) {
                        continue;
                    }

                    double distIK = distances[i][k];
                    double distKJ = distances[k][j];
                    double currentDist = distances[i][j];
                    double newDist = distIK + distKJ;

                    if (distIK != Double.POSITIVE_INFINITY && distKJ != Double.POSITIVE_INFINITY) {
                        String checkMsg = "Проверка: dist[" + (i+1) + "][" + (k+1) + "] + dist[" + (k+1) + "][" + (j+1) + "] = " +
                                formatDist(distIK) + " + " + formatDist(distKJ) + " = " + formatDist(newDist) +
                                " vs текущее " + formatDist(currentDist);

                        if (newDist < currentDist) {
                            distances[i][j] = newDist;
                            next[i][j] = next[i][k];

                            steps.add(new AlgorithmStep(k, i, j, copyDistances(),
                                    checkMsg + " -> ОБНОВЛЕНО на " + formatDist(newDist), true));
                        } else {
                            steps.add(new AlgorithmStep(k, i, j, copyDistances(),
                                    checkMsg + " -> НЕ ОБНОВЛЕНО", false));
                        }
                    } else {
                        if (distIK != Double.POSITIVE_INFINITY || distKJ != Double.POSITIVE_INFINITY) {
                            String msg = "Проверка: dist[" + (i+1) + "][" + (k+1) + "] = " +
                                    formatDist(distIK) + ", dist[" + (k+1) + "][" + (j+1) + "] = " +
                                    formatDist(distKJ) + " -> путь не существует";
                            steps.add(new AlgorithmStep(k, i, j, copyDistances(), msg, false));
                        }
                    }
                }
            }

            if (k < n - 1) {
                steps.add(new AlgorithmStep(k, -1, -1, copyDistances(),
                        "Итерация с посредником " + (k + 1) + " завершена"));
            }
        }

        List<Integer> negativeCycleVertices = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (distances[i][i] < 0) {
                negativeCycleVertices.add(i);
            }
        }

        if (!negativeCycleVertices.isEmpty()) {
            hasNegativeCycle = true;

            StringBuilder sb = new StringBuilder("ОБНАРУЖЕН ОТРИЦАТЕЛЬНЫЙ ЦИКЛ! dist[i][i] < 0 для вершин: ");
            for (int idx = 0; idx < negativeCycleVertices.size(); idx++) {
                int v = negativeCycleVertices.get(idx);
                if (idx > 0) sb.append(", ");
                sb.append(v + 1).append(" (dist=").append(formatDist(distances[v][v])).append(")");
            }
            sb.append(". Кратчайшие пути через эти вершины не определены, т.к. их можно уменьшать бесконечно.");

            AlgorithmStep negativeCycleStep = new AlgorithmStep(-1, -1, -1, copyDistances(), sb.toString());
            negativeCycleStep.setNegativeCycleVertices(negativeCycleVertices);
            steps.add(negativeCycleStep);
            return steps;
        }

        steps.add(new AlgorithmStep(-1, -1, -1, copyDistances(),
                "Алгоритм Флойда-Уоршелла успешно завершен"));

        return steps;
    }

    private void initializeMatrices() {

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distances[i][j] = 0;
                } else {
                    distances[i][j] = Double.POSITIVE_INFINITY;
                }
                next[i][j] = j;
            }
        }

        List<Vertex> vertices = graph.getVertices();

        for (Edge edge : graph.getEdges()) {
            int from = vertices.indexOf(edge.getSource());
            int to = vertices.indexOf(edge.getTarget());

            if (from >= 0 && to >= 0) {
                double weight = edge.getWeight();

                if (weight < distances[from][to]) {
                    distances[from][to] = weight;
                    next[from][to] = to;
                }
            }
        }
    }

    private double[][] copyDistances() {
        double[][] copy = new double[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(distances[i], 0, copy[i], 0, n);
        }
        return copy;
    }

    private String formatDist(double d) {
        if (Double.isInfinite(d)) {
            return "∞";
        }
        if (d == (long) d) {
            return String.valueOf((long) d);
        }
        return String.format("%.1f", d);
    }

    public boolean hasNegativeCycle() {
        return hasNegativeCycle;
    }
}