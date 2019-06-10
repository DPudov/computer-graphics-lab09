package sample;

import java.util.ArrayList;
import java.util.LinkedList;

public class Polygon {
    private LinkedList<Edge> edges;

    public Polygon() {
        this.edges = new LinkedList<>();
    }

    public Polygon(LinkedList<Edge> edges) {
        this.edges = edges;
    }

    public LinkedList<Edge> getEdges() {
        return edges;
    }

    public ArrayList<Point> getVertices() {
        ArrayList<Point> result = new ArrayList<>();
        if (edges.size() == 1) {
            result.add(edges.getFirst().getBegin());
            result.add(edges.getFirst().getEnd());
            return result;
        }

        for (Edge e : edges) {
            result.add(e.getBegin());
        }
        return result;
    }

    public void setEdges(LinkedList<Edge> edges) {
        this.edges = edges;
    }
}
