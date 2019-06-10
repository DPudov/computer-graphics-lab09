package sample;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class LineDrawer {
    private static boolean isPoint(double x0, double y0, double xe, double ye) {
        return x0 == xe && y0 == ye;
    }

    private static int round(double value) {
        return (int) (value + 0.5);
    }

    public static void DigitalDiffAnalyzeDraw(Canvas canvas, double x0, double y0, double xe, double ye, Color color) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(color);
        gc.strokeLine(x0, y0, xe, ye);
    }
}
