package com.example.floidyolsheer;

import java.util.Objects;

public class Vertex {
    private int id;
    private String name;
    private double x;
    private double y;
    private boolean highlighted;
    private String highlightColor;

    public Vertex(int id, String name, double x, double y) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.highlighted = false;
        this.highlightColor = null;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isHighlighted() { return highlighted; }
    public String getHighlightColor() { return highlightColor; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setHighlighted(boolean highlighted) { this.highlighted = highlighted; }
    public void setHighlightColor(String highlightColor) { this.highlightColor = highlightColor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return id == vertex.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name;
    }
}