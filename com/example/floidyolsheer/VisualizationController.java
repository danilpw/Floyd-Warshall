package com.example.floidyolsheer;

import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Window;

public class VisualizationController {

    private GraphEditor graphEditor;
    private MatrixPanel matrixPanel;
    private LogPanel logPanel;
    private Window owner;

    private boolean isRunning = false;
    private boolean isAlgorithmExecuted = false;
    private int currentStepIndex = -1;
    private List<AlgorithmStep> steps;
    private FloydWarshallAlgorithm algorithm;
    private double[][] initialDistances;

    private AnimationTimer animationTimer;
    private long lastUpdate = 0;
    private static final long FRAME_DELAY = 100_000_000;
    private boolean isAnimating = false;
    private boolean isAutoPlayFinished = false;

    public VisualizationController() {

    }

    public void setGraphEditor(GraphEditor graphEditor) {
        this.graphEditor = graphEditor;
    }

    public void setMatrixPanel(MatrixPanel matrixPanel) {
        this.matrixPanel = matrixPanel;
    }

    public void setLogPanel(LogPanel logPanel) {
        this.logPanel = logPanel;
    }

    public void setOwner(Window owner) {
        this.owner = owner;
    }

    private void unlockGraph() {
        if (graphEditor != null) {
            graphEditor.setLocked(false);
        }
    }

    private void lockGraph() {
        if (graphEditor != null) {
            graphEditor.setLocked(true);
        }
    }

    public void onLoadButtonClick() {
        if (owner == null) {
            if (logPanel != null) {
                logPanel.addLogMessage("Ошибка: окно не инициализировано");
            }
            return;
        }

        Graph loadedGraph = GraphIO.loadGraph(owner);
        if (loadedGraph != null && graphEditor != null) {
            graphEditor.setGraph(loadedGraph);
            if (logPanel != null) {
                logPanel.addLogMessage("Граф загружен");
            }
            if (matrixPanel != null) {
                matrixPanel.updateFromGraph(loadedGraph);
            }
            isAlgorithmExecuted = false;
            currentStepIndex = -1;
            steps = null;
            isAutoPlayFinished = false;
            stopAnimation();
            unlockGraph();
        }
    }

    public void onSaveButtonClick() {
        if (graphEditor == null) return;
        if (owner == null) {
            if (logPanel != null) {
                logPanel.addLogMessage("Ошибка: окно не инициализировано");
            }
            return;
        }

        Graph graph = graphEditor.getGraph();
        if (graph != null && graph.getVertexCount() > 0) {
            boolean saved = GraphIO.saveGraph(graph, owner);
            if (saved && logPanel != null) {
                logPanel.addLogMessage("Граф сохранен");
            }
        } else {
            if (logPanel != null) {
                logPanel.addLogMessage("Невозможно сохранить пустой граф");
            }
        }
    }

    public void onStartButtonClick() {
        if (isAlgorithmExecuted && steps != null && !steps.isEmpty() && isAutoPlayFinished) {
            if (logPanel != null) {
                logPanel.addLogMessage("Перезапуск автоматической визуализации...");
            }
            lockGraph();
            currentStepIndex = 0;
            isAutoPlayFinished = false;
            startAnimation();
            return;
        }

        if (isAlgorithmExecuted && isAnimating) {
            if (logPanel != null) {
                logPanel.addLogMessage("Автоматическая визуализация уже выполняется.");
            }
            return;
        }

        if (isAlgorithmExecuted && steps != null && !steps.isEmpty()) {
            if (logPanel != null) {
                logPanel.addLogMessage("Алгоритм уже выполнен. Используйте кнопки 'Шаг вперед/назад' для навигации.");
            }
            return;
        }

        if (!isAlgorithmExecuted) {
            lockGraph();
            executeAlgorithmWithAnimation();
        }
    }

    private void executeAlgorithmWithAnimation() {
        if (isRunning || isAnimating) {
            if (logPanel != null) {
                logPanel.addLogMessage("Алгоритм уже выполняется.");
            }
            return;
        }

        Graph graph = graphEditor.getGraph();
        if (graph.getVertexCount() < 2) {
            if (logPanel != null) {
                logPanel.addLogMessage("Ошибка: граф должен содержать как минимум 2 вершины!");
            }
            unlockGraph();
            return;
        }

        stopAnimation();
        isRunning = true;
        isAlgorithmExecuted = false;
        currentStepIndex = -1;
        isAutoPlayFinished = false;

        if (logPanel != null) {
            logPanel.clearLog();
            logPanel.addLogMessage("Запуск алгоритма Флойда-Уоршелла...");
            logPanel.addLogMessage("Количество вершин: " + graph.getVertexCount());
        }

        initialDistances = graph.getAdjacencyMatrix();
        if (matrixPanel != null) {
            matrixPanel.updateMatrix(initialDistances);
            matrixPanel.clearHighlight();
        }

        new Thread(() -> {
            try {
                algorithm = new FloydWarshallAlgorithm(graph);
                steps = algorithm.execute();

                Platform.runLater(() -> {
                    isAlgorithmExecuted = true;
                    isRunning = false;
                    isAutoPlayFinished = false;

                    if (algorithm.hasNegativeCycle()) {
                        if (logPanel != null) {
                            logPanel.addLogMessage("ОБНАРУЖЕН ОТРИЦАТЕЛЬНЫЙ ЦИКЛ!");
                            logPanel.addLogMessage("Алгоритм выполнен до момента обнаружения. Сгенерировано " + steps.size() + " шагов.");
                            logPanel.addLogMessage("Запуск автоматической визуализации...");
                        }
                        showNegativeCycleAlert();
                        currentStepIndex = 0;
                        startAnimation();
                        return;
                    } else if (logPanel != null) {
                        logPanel.addLogMessage("Алгоритм выполнен. Сгенерировано " + steps.size() + " шагов.");
                        logPanel.addLogMessage("Запуск автоматической визуализации...");
                    }

                    currentStepIndex = 0;
                    startAnimation();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (logPanel != null) {
                        logPanel.addLogMessage("Ошибка при выполнении алгоритма: " + e.getMessage());
                    }
                    isRunning = false;
                    unlockGraph();
                });
            }
        }).start();
    }

    private void executeAlgorithmInBackground() {
        if (isRunning || isAnimating) {
            return;
        }

        Graph graph = graphEditor.getGraph();
        if (graph.getVertexCount() < 2) {
            if (logPanel != null) {
                logPanel.addLogMessage("Ошибка: граф должен содержать как минимум 2 вершины!");
            }
            return;
        }

        stopAnimation();
        isRunning = true;
        isAlgorithmExecuted = false;
        currentStepIndex = -1;
        isAutoPlayFinished = false;

        if (logPanel != null) {
            logPanel.clearLog();
            logPanel.addLogMessage("Выполнение алгоритма Флойда-Уоршелла...");
        }

        initialDistances = graph.getAdjacencyMatrix();
        if (matrixPanel != null) {
            matrixPanel.updateMatrix(initialDistances);
            matrixPanel.clearHighlight();
        }

        lockGraph();

        new Thread(() -> {
            try {
                algorithm = new FloydWarshallAlgorithm(graph);
                steps = algorithm.execute();

                Platform.runLater(() -> {
                    isAlgorithmExecuted = true;
                    isRunning = false;
                    isAutoPlayFinished = false;

                    if (algorithm.hasNegativeCycle()) {
                        if (logPanel != null) {
                            logPanel.addLogMessage("ОБНАРУЖЕН ОТРИЦАТЕЛЬНЫЙ ЦИКЛ!");
                            logPanel.addLogMessage("Алгоритм выполнен до момента обнаружения. Сгенерировано " + steps.size() + " шагов.");
                            logPanel.addLogMessage("Переход в ручной режим просмотра.");
                        }
                        showNegativeCycleAlert();
                    } else if (logPanel != null) {
                        logPanel.addLogMessage("Алгоритм выполнен. Сгенерировано " + steps.size() + " шагов.");
                        logPanel.addLogMessage("Переход в ручной режим просмотра.");
                    }

                    currentStepIndex = 0;
                    applyStep(currentStepIndex);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (logPanel != null) {
                        logPanel.addLogMessage("Ошибка при выполнении алгоритма: " + e.getMessage());
                    }
                    isRunning = false;
                    unlockGraph();
                });
            }
        }).start();
    }

    private void startAnimation() {
        if (isAnimating) {
            return;
        }

        isAnimating = true;
        isAutoPlayFinished = false;
        lastUpdate = System.nanoTime();

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isAnimating || steps == null) {
                    stop();
                    return;
                }

                if (now - lastUpdate >= FRAME_DELAY) {
                    lastUpdate = now;

                    if (currentStepIndex < steps.size() - 1) {
                        currentStepIndex++;
                        applyStep(currentStepIndex);
                    } else {
                        stopAnimation();
                        isAutoPlayFinished = true;
                        if (logPanel != null) {
                            logPanel.addLogMessage("Автоматическая визуализация завершена.");
                            logPanel.addLogMessage("Используйте кнопки 'Шаг вперед/назад' для ручного просмотра.");
                        }
                    }
                }
            }
        };

        animationTimer.start();
        if (currentStepIndex >= 0 && currentStepIndex < steps.size()) {
            applyStep(currentStepIndex);
        }
    }

    private void stopAnimation() {
        isAnimating = false;
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
    }

    public void onNextButtonClick() {
        if (!isAlgorithmExecuted) {
            executeAlgorithmInBackground();
            return;
        }

        if (isRunning) {
            if (logPanel != null) {
                logPanel.addLogMessage("Алгоритм еще выполняется, подождите...");
            }
            return;
        }

        if (steps == null || steps.isEmpty()) {
            return;
        }

        if (isAutoPlayFinished) {
            if (isAnimating) {
                stopAnimation();
            }
            isAutoPlayFinished = false;
            currentStepIndex = 0;
            if (logPanel != null) {
                logPanel.addLogMessage("Ручной режим: начало просмотра с первого шага.");
            }
            applyStep(currentStepIndex);
            return;
        }

        if (isAnimating) {
            stopAnimation();
            if (logPanel != null) {
                logPanel.addLogMessage("Автоматическая визуализация остановлена. Переход в ручной режим.");
            }
        }

        if (currentStepIndex >= steps.size() - 1) {
            if (logPanel != null) {
                logPanel.addLogMessage("Достигнут последний шаг алгоритма.");
            }
            return;
        }

        currentStepIndex++;
        applyStep(currentStepIndex);
    }

    public void onPreviousButtonClick() {
        if (!isAlgorithmExecuted) {
            executeAlgorithmInBackground();
            return;
        }

        if (isRunning) {
            if (logPanel != null) {
                logPanel.addLogMessage("Алгоритм еще выполняется, подождите...");
            }
            return;
        }

        if (steps == null || steps.isEmpty()) {
            return;
        }

        if (isAutoPlayFinished) {
            isAutoPlayFinished = false;
            if (logPanel != null) {
                logPanel.addLogMessage("Возврат к последнему шагу алгоритма.");
            }
            return;
        }

        if (isAnimating) {
            stopAnimation();
            if (logPanel != null) {
                logPanel.addLogMessage("Автоматическая визуализация остановлена. Переход в ручной режим.");
            }
        }

        if (currentStepIndex <= 0) {
            if (logPanel != null) {
                logPanel.addLogMessage("Достигнут первый шаг алгоритма.");
            }
            return;
        }

        currentStepIndex--;
        applyStep(currentStepIndex);
    }

    public void onResetButtonClick() {
        stopAnimation();
        isRunning = false;
        isAlgorithmExecuted = false;
        isAnimating = false;
        isAutoPlayFinished = false;
        currentStepIndex = -1;
        steps = null;
        algorithm = null;

        if (logPanel != null) {
            logPanel.clearLog();
            logPanel.addLogMessage("Алгоритм сброшен.");
            logPanel.addLogMessage("Граф сброшен в начальное состояние.");
        }

        if (graphEditor != null) {
            graphEditor.clearHighlight();
            unlockGraph();
            graphEditor.draw();
        }

        if (matrixPanel != null && initialDistances != null) {
            matrixPanel.updateMatrix(initialDistances);
            matrixPanel.clearHighlight();
        } else if (matrixPanel != null && graphEditor != null) {
            matrixPanel.updateFromGraph(graphEditor.getGraph());
        }
    }

    public void onAuthorsButtonClick() {
        if (logPanel != null) {
            logPanel.addLogMessage("О РАЗРАБОТЧИКАХ:");
            logPanel.addLogMessage("Гайнутдинова З.Р. - группа 4381");
            logPanel.addLogMessage("Ишамчурин Д.И. - группа 4381");
            logPanel.addLogMessage("Тишкевич К.В. - группа 4388");
            logPanel.addLogMessage("Руководитель: Фирсов М.А.");
            logPanel.addLogMessage("СПбГЭТУ 'ЛЭТИ', 2026");
        }
    }

    private void showNegativeCycleAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Отрицательный цикл");
        alert.setHeaderText("Обнаружен отрицательный цикл");
        alert.setContentText("В графе есть цикл с отрицательным суммарным весом. " +
                "Кратчайшие пути через вершины этого цикла не определены, т.к. их можно " +
                "уменьшать бесконечно, повторно проходя по циклу. " +
                "Алгоритм покажет шаги вплоть до момента обнаружения.");
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }

    private void applyStep(int index) {
        if (steps == null || index < 0 || index >= steps.size()) {
            return;
        }

        AlgorithmStep step = steps.get(index);

        if (matrixPanel != null) {
            matrixPanel.updateMatrix(step.getDistances());
            if (step.getI() >= 0 && step.getJ() >= 0) {
                String color = step.isUpdated() ? "#00CC66" : "#ff6b6b";
                matrixPanel.highlightCell(step.getI(), step.getJ(), color);
            } else {
                matrixPanel.clearHighlight();
            }
        }

        if (logPanel != null) {
            logPanel.addLogMessage("--- Шаг " + (index + 1) + " из " + steps.size() + " ---");
            logPanel.addLogMessage(step.getMessage());

            if (step.getK() >= 0 && step.getI() >= 0 && step.getJ() >= 0) {
                logPanel.setCurrentVertex("k=" + (step.getK() + 1) + ", i=" + (step.getI() + 1) + ", j=" + (step.getJ() + 1));
            } else if (step.getK() >= 0) {
                logPanel.addLogMessage("Завершена итерация с посредником " + (step.getK() + 1));
            }
        }

        if (graphEditor != null) {
            graphEditor.clearHighlight();

            if (step.getK() >= 0 && step.getI() >= 0 && step.getJ() >= 0) {
                Graph graph = graphEditor.getGraph();
                List<Vertex> vertices = graph.getVertices();

                if (step.getK() < vertices.size() && step.getI() < vertices.size() && step.getJ() < vertices.size()) {
                    Vertex vertexK = vertices.get(step.getK());
                    Vertex vertexI = vertices.get(step.getI());
                    Vertex vertexJ = vertices.get(step.getJ());

                    graphEditor.highlightVertex(vertexK, "#FF6B35");

                    graphEditor.highlightVertex(vertexI, "#3B82F6");
                    if (!vertexJ.equals(vertexI)) {
                        graphEditor.highlightVertex(vertexJ, "#3B82F6");
                    }

                    if (step.isUpdated()) {
                        graphEditor.highlightEdges(vertexI, vertexK, "#00CC66");
                        graphEditor.highlightEdges(vertexK, vertexJ, "#00CC66");
                        graphEditor.highlightEdges(vertexI, vertexJ, "#FF0000");
                    } else {
                        graphEditor.highlightEdges(vertexI, vertexK, "#FF0000");
                        graphEditor.highlightEdges(vertexK, vertexJ, "#FF0000");
                        graphEditor.highlightEdges(vertexI, vertexJ, "#FF0000");
                    }
                }
            } else if (step.isNegativeCycleStep()) {
                Graph graph = graphEditor.getGraph();
                List<Vertex> vertices = graph.getVertices();
                for (int idx : step.getNegativeCycleVertices()) {
                    if (idx < vertices.size()) {
                        graphEditor.highlightVertex(vertices.get(idx), "#B00020");
                    }
                }
            }

            graphEditor.draw();
        }
    }

    public boolean isAlgorithmExecuted() {
        return isAlgorithmExecuted;
    }

    public boolean isAnimating() {
        return isAnimating;
    }
}