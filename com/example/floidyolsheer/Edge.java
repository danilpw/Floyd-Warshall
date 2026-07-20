package com.example.floidyolsheer;

import java.util.Objects;

public class Edge {
    private int id;
    private Vertex source;
    private Vertex target;
    private int weight;
    private boolean directed;
    private boolean highlighted;
    private String highlightColor;

    public Edge(int id, Vertex source, Vertex target, int weight, boolean directed) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.weight = weight;
        this.directed = directed;
        this.highlighted = false;
        this.highlightColor = null;
    }

    public int getId() { return id; }
    public Vertex getSource() { return source; }
    public Vertex getTarget() { return target; }
    public int getWeight() { return weight; }
    public boolean isDirected() { return directed; }
    public boolean isHighlighted() { return highlighted; }
    public String getHighlightColor() { return highlightColor; }

    public void setId(int id) { this.id = id; }
    public void setWeight(int weight) { this.weight = weight; }
    public void setHighlighted(boolean highlighted) { this.highlighted = highlighted; }
    public void setHighlightColor(String color) { this.highlightColor = color; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return id == edge.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return source.getName() + " -> " + target.getName() + " (" + weight + ")";
    }
}