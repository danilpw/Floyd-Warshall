package com.example.floidyolsheer;

public class AlgorithmStep {
    private int k;
    private int i;
    private int j;
    private double[][] distances;
    private String message;
    private boolean updated;

    public AlgorithmStep(int k, int i, int j, double[][] distances, String message) {
        this(k, i, j, distances, message, false);
    }

    public AlgorithmStep(int k, int i, int j, double[][] distances, String message, boolean updated) {
        this.k = k;
        this.i = i;
        this.j = j;
        this.distances = distances;
        this.message = message;
        this.updated = updated;
    }

    public int getK() { return k; }
    public int getI() { return i; }
    public int getJ() { return j; }
    public double[][] getDistances() { return distances; }
    public String getMessage() { return message; }

    public boolean isUpdated() { return updated; }

    private java.util.List<Integer> negativeCycleVertices;
    public java.util.List<Integer> getNegativeCycleVertices() { return negativeCycleVertices; }
    public void setNegativeCycleVertices(java.util.List<Integer> vertices) { this.negativeCycleVertices = vertices; }

    public boolean isNegativeCycleStep() { return negativeCycleVertices != null && !negativeCycleVertices.isEmpty(); }
}