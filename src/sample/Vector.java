package sample;

public class Vector {

    protected double x;
    protected double y;
    protected double z;

    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(double val) {
        x = val;
    }

    public double getY() {
        return y;
    }

    public void setY(double val) {
        y = val;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double val) {
        z = val;
    }

    public Vector add(Vector v) {
        return new Vector(x + v.x, y + v.y, z + v.z);
    }

    public Vector sub(Vector v) {
        return new Vector(x - v.x, y - v.y, z - v.z);
    }

    public Vector mul(Vector v) {
        return new Vector(x * v.x, y * v.y, z * v.z);
    }

    public Vector div(Vector v) {
        return new Vector(x / v.x, y / v.y, z / v.z);
    }

    public Vector add(double s) {
        return new Vector(x + s, y + s, z + s);
    }

    public Vector sub(double s) {
        return new Vector(x - s, y - s, z - s);
    }

    public Vector mul(double s) {
        return new Vector(x * s, y * s, z * s);
    }

    public Vector div(double s) {
        double inv = 1.0f / s;
        return new Vector(x * inv, y * inv, z * inv);
    }

    public double length() {
        return (double) Math.sqrt(x * x + y * y + z * z);
    }

    public double length2() {
        return (x * x + y * y + z * z);
    }

    public Vector cross(Vector v) {
        return new Vector(y * v.z - z * v.y,
                z * v.x - x * v.z,
                x * v.y - y * v.x);
    }

    public double dot(Vector v) {
        return (x * v.x + y * v.y + z * v.z);
    }

    public Point toPoint() {
        return new Point(x, y, z);
    }

    public Vector getInverse() {
        return new Vector(1.0f / x, 1.0f / y, 1.0f / z);
    }

    public double normalize() {
        double len = length();
        double inv = 1.0f / len;

        x *= inv;
        y *= inv;
        z *= inv;

        return len;
    }

    public double angleBetween(Vector v) {
        Vector v1 = new Vector(x, y, z);
        Vector v2 = new Vector(v.x, v.y, v.z);
        v1.normalize();
        v2.normalize();
        return Math.acos(v1.dot(v2));
    }
}
