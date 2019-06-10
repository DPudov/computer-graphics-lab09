package sample;

import java.util.ArrayList;

public class Cutter {
    private ArrayList<Point> vertices;
    private int sign;
    private boolean isFull;

    public ArrayList<Point> getVertices() {
        return vertices;
    }

    public Cutter() {
        vertices = new ArrayList<>();
    }


    public boolean isConvex() {
        int size = vertices.size();
        ArrayList<Double> vectors = new ArrayList<>();
        int sign = 0;
        if (size < 3) {
            setSign(sign);
            return false;
        }
        for (int i = 1; i < size; i++) {

            Point pointCur;
            Point pointPrev;
            Point pointNext;
            Point vectorAb;
            Point vectorBc;
            if (i < size - 1) {
                pointCur = vertices.get(i);
                pointPrev = vertices.get(i - 1);
                pointNext = vertices.get(i + 1);
                vectorAb = new Point(pointCur.getX() - pointPrev.getX(),
                        pointCur.getY() - pointPrev.getY());
                vectorBc = new Point(pointNext.getX() - pointCur.getX(),
                        pointNext.getY() - pointCur.getY());

            } else {
                pointCur = vertices.get(i);
                pointPrev = vertices.get(i - 1);
                vectorAb = new Point(pointCur.getX() - pointPrev.getX(),
                        pointCur.getY() - pointPrev.getY());
                vectorBc = new Point(vertices.get(1).getX() - vertices.get(0).getX(),
                        vertices.get(1).getY() - vertices.get(0).getY());
            }

            vectors.add(vectorAb.getX() * vectorBc.getY() - vectorAb.getY() * vectorBc.getX());
        }

        boolean exist = false;
        for (Double d : vectors) {
            if (d == 0) {
                continue;
            }
            if (exist) {
                if (sign(d) != sign) {
                    setSign(sign);
                    return false;
                }
            } else {
                sign = sign(d);
                exist = true;
            }
        }
        setSign(sign);
        return true;
    }

    private int sign(Double d) {
        if (d == 0) {
            return 0;
        }

        return d > 0 ? 1 : -1;
    }

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }

    public int size() {
        return vertices.size();
    }

    public Point get(int index) {
        return vertices.get(index);
    }

    public boolean isFull() {
        return isFull;
    }

    public void setFull(boolean full) {
        isFull = full;
    }

    public void add(Point p) {
        vertices.add(p);
    }

    public void clear() {
        vertices.clear();
        isFull = false;
    }

    public ArrayList<Edge> getEdges() {
        ArrayList<Edge> result = new ArrayList<>();
        for (int i = 0; i < vertices.size() - 1; i++) {
            result.add(new Edge(vertices.get(i), vertices.get(i + 1)));
        }
        result.add(new Edge(vertices.get(vertices.size() - 1), vertices.get(0)));
        return result;
    }
}
