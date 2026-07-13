package com.example.floidyolsheer;

public class VisualizationController {

    private GraphEditor graphEditor;
    private MatrixPanel matrixPanel;
    private LogPanel logPanel;

    private boolean isRunning = false;
    private int currentStep = 0;
    private int maxStep = 0;
    private String currentVertex = "C";

    public VisualizationController() {
        // Инициализация
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

    public void initDemoData() {
        if (logPanel != null) {
            logPanel.addLogMessage("Загружены демонстрационные данные.");
            logPanel.addLogMessage("Граф с 4 вершинами (A, B, C, D)");
        }
    }

    public void onLoadButtonClick() {
        if (logPanel != null) {
            logPanel.addLogMessage("заглушка");
        }
    }

    public void onSaveButtonClick() {
        if (logPanel != null) {
            logPanel.addLogMessage("заглушка");
        }
    }

    public void onStartButtonClick() {
        if (!isRunning) {
            isRunning = true;
            currentStep = 0;
            maxStep = 5;
            if (logPanel != null) {
                logPanel.clearLog();
                logPanel.addLogMessage("Запуск");
                logPanel.addLogMessage("Начальная инициализация матрицы расстояний.");
            }
            performStep();
        } else {
            if (logPanel != null) {
                logPanel.addLogMessage("Алгоритм уже запущен.");
            }
        }
    }

    public void onNextButtonClick() {
        if (isRunning && currentStep < maxStep) {
            currentStep++;
            performStep();
        } else if (!isRunning) {
            if (logPanel != null) {
                logPanel.addLogMessage("Сначала нажмите 'Старт'.");
            }
        } else {
            if (logPanel != null) {
                logPanel.addLogMessage("Алгоритм завершен.");
            }
        }
    }

    public void onPreviousButtonClick() {
        if (isRunning && currentStep > 0) {
            currentStep--;
            if (logPanel != null) {
                logPanel.addLogMessage("Шаг назад: " + currentStep);
            }
            // Здесь должна быть логика возврата к предыдущему состоянию
        } else {
            if (logPanel != null) {
                logPanel.addLogMessage("Невозможно выполнить шаг назад.");
            }
        }
    }

    public void onResetButtonClick() {
        isRunning = false;
        currentStep = 0;
        if (logPanel != null) {
            logPanel.clearLog();
            logPanel.addLogMessage("Алгоритм сброшен.");
        }
        if (graphEditor != null) {
            graphEditor.clearHighlight();
        }
        if (logPanel != null) {
            logPanel.addLogMessage("Граф сброшен в начальное состояние.");
        }
    }

    public void onAuthorsButtonClick() {
        if (logPanel != null) {
            logPanel.addLogMessage("Информация о разработчиках");
        }
    }

    private void performStep() {
        if (logPanel != null) {
            logPanel.setCurrentStep(currentStep);
        }

        switch (currentStep) {
            case 1:
                currentVertex = "A";
                if (logPanel != null) {
                    logPanel.setCurrentVertex(currentVertex);
                    logPanel.addLogMessage("Обработка вершины A как промежуточной.");
                }
                if (graphEditor != null) {
                    GraphEditor.Vertex v1 = new GraphEditor.Vertex(0, "A", 0, 0);
                    GraphEditor.Vertex v2 = new GraphEditor.Vertex(1, "B", 0, 0);
                    graphEditor.highlightEdge(v1, v2);
                }
                break;
            case 2:
                currentVertex = "B";
                if (logPanel != null) {
                    logPanel.setCurrentVertex(currentVertex);
                    logPanel.setPathCheck("A", "C", "B", 4, 5, 8);
                    logPanel.setNewDistance(7);
                }
                if (matrixPanel != null) {
                    matrixPanel.updateCell(0, 2, 7);
                }
                break;
            case 3:
                currentVertex = "C";
                if (logPanel != null) {
                    logPanel.setCurrentVertex(currentVertex);
                    logPanel.addLogMessage("Обработка вершины C как промежуточной.");
                }
                if (graphEditor != null) {
                    GraphEditor.Vertex v1 = new GraphEditor.Vertex(1, "B", 0, 0);
                    GraphEditor.Vertex v2 = new GraphEditor.Vertex(3, "D", 0, 0);
                    graphEditor.highlightEdge(v1, v2);
                }
                break;
            case 4:
                currentVertex = "D";
                if (logPanel != null) {
                    logPanel.setCurrentVertex(currentVertex);
                    logPanel.addLogMessage("Обработка вершины D как промежуточной.");
                }
                if (matrixPanel != null) {
                    matrixPanel.highlightCell(0, 3);
                }
                break;
            default:
                if (logPanel != null) {
                    logPanel.addLogMessage("Алгоритм завершен. Конечная матрица расстояний построена.");
                }
                if (graphEditor != null) {
                    graphEditor.clearHighlight();
                }
                isRunning = false;
                break;
        }
    }
}