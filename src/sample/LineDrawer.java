package sample;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class LineDrawer {

    public static void DrawLine(Canvas canvas, double x0, double y0, double xe, double ye, Color color) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(color);
        gc.setLineWidth(1);
        gc.strokeLine(x0, y0, xe, ye);
    }

    public static void DrawLineD(Canvas canvas, double x0, double y0, double xe, double ye, Color color) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(color);
        gc.setLineWidth(3);
        gc.strokeLine(x0, y0, xe, ye);
    }
}
