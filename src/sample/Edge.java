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


}
