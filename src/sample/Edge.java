package sample;

public class Edge {
    private Point begin;
    private Point end;
    private boolean beginInit;
    private boolean endInit;

    public Edge() {
        this.beginInit = false;
        this.endInit = false;
    }

    public Edge(Point begin, Point end) {
        this.begin = begin;
        this.end = end;
        this.beginInit = false;
        this.endInit = false;
    }

    public static boolean isPointInsideEdge(Edge e, Point ref, Point p) {
        boolean ret = true;
        Vector ve = e.end.sub(e.begin);
        Vector vr = ref.sub(e.begin);
        Vector vp = p.sub(e.begin);

        Vector A = ve.cross(vr);
        Vector B = ve.cross(vp);

        ret = (A.getZ() < 0 && B.getZ() < 0) || (A.getZ() > 0 && B.getZ() > 0);
        return ret;
    }

    public Point getBegin() {
        return begin;
    }

    public void setBegin(Point begin) {
        this.begin = begin;
    }

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        this.end = end;
    }


    public boolean isBeginInit() {
        return beginInit;
    }

    public void setBeginInit(boolean beginInit) {
        this.beginInit = beginInit;
    }

    public boolean isEndInit() {
        return endInit;
    }

    public void setEndInit(boolean endInit) {
        this.endInit = endInit;
    }

    public void clear() {
        setEndInit(false);
        setBeginInit(false);
    }


    public Point computeIntersection(Point s, Point p) {
        double edgex = begin.getX() - end.getX();
        double linex = s.getX() - p.getX();

        double m1 = (s.getY() - p.getY()) / (s.getX() - p.getX());
        double m2 = (begin.getY() - end.getY()) / (begin.getX() - end.getX());
        double b1 = s.getY() - m1 * s.getX();
        double b2 = begin.getY() - m2 * end.getX();

        double x = edgex == 0.0f ? begin.getX() :
                linex == 0.0f ? p.getX() : (b2 - b1) / (m1 - m2);
        double y = linex == 0.0f ? m2 * x + b2 : m1 * x + b1;

        return new Point(x, y, 0);
    }

}
