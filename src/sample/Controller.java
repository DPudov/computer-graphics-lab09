package sample;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

public class Controller {
    @FXML
    Button edgeDrawButton;
    @FXML
    Button cutButton;
    @FXML
    Button clearCutter;
    @FXML
    Canvas canvas;
    @FXML
    TextField inputYField;
    @FXML
    TextField inputXField;
    @FXML
    Button addCutterButton;
    @FXML
    Label cursorLabel;
    @FXML
    ColorPicker linePicker;
    @FXML
    ColorPicker cutterPicker;
    @FXML
    ColorPicker visiblePicker;
    @FXML
    Button clearAllButton;
    @FXML
    Button addLineButton;

    private Cutter cutter = new Cutter();
    private final LinkedList<Edge> allEdges = new LinkedList<>();
    private LinkedList<Edge> edges = new LinkedList<>();
    private LinkedList<Cutter> cutters = new LinkedList<>();
    private final LinkedList<Edge> currentPolygon = new LinkedList<>();
    private final ArrayList<Polygon> polygons = new ArrayList<>();
    private Edge currentEdge = new Edge(new Point(0, 0), new Point(0, 0));
    private int direction = -1;

    private int edgeIndex = 0;
    private boolean drawingOnEdge = false;

    @FXML
    public void initialize() {
        setupColors();
        setupCanvasListeners();


        clearAllButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            clearCanvas();
            clearData();
        });

        addLineButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            try {
                Point p1 = new Point(Integer.parseInt(inputXField.getText()),
                        Integer.parseInt(inputYField.getText()));
                addPoint(p1.getX(), p1.getY());
                doUpdate();
            } catch (NumberFormatException e) {
                setAlert("Введены неверные данные для нового отрезка");
            }
        });

        addCutterButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            try {
                if (cutters.size() == 0) {
                    Point p = new Point(Integer.parseInt(inputXField.getText()),
                            Integer.parseInt(inputYField.getText()));
                    addCutterPoint(p);
                } else {
                    setAlert("Уже есть отсекатель!");
                }
            } catch (NumberFormatException e) {
                setAlert("Введены неверные данные для нового отсекателя");
            }
        });

        clearCutter.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            cutters.clear();
            cutter.clear();
            clearCanvas();
            for (Edge e : allEdges) {
                drawLine(e);
            }
        });

        cutButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            cut();
        });

        edgeDrawButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            if (cutters.size() != 0) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Введите номер ребра");
                dialog.setContentText("Номер");
                dialog.setHeaderText("Введите номер ребра");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(number -> {
                    this.edgeIndex = Integer.parseInt(number);
                    this.drawingOnEdge = true;
                });
            } else {
                setAlert("Нет отсекателя");
            }
        });
    }

    private void setupCanvasListeners() {
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            MouseButton b = e.getButton();
            boolean hasShift = e.isShiftDown();
            boolean hasControl = e.isControlDown();
            if (!drawingOnEdge) {
                if (b == MouseButton.PRIMARY && hasShift && hasControl) {
                    //Прямая
                    addPoint((int) e.getX(), (int) e.getY());
                } else if (b == MouseButton.PRIMARY && hasShift) {
                    // горизонтальная
                    addPointHorizontal((int) e.getX(), (int) e.getY());
                } else if (b == MouseButton.PRIMARY && hasControl) {
                    // вертикальная
                    addPointVertical((int) e.getX(), (int) e.getY());
                } else if (b == MouseButton.PRIMARY) {
                    addPoint((int) e.getX(), (int) e.getY());
                } else if (b == MouseButton.SECONDARY && hasControl) {
                    closeCutter();
                } else if (b == MouseButton.SECONDARY && hasShift) {
                    closePolygon();
                } else if (b == MouseButton.SECONDARY) {
                    addCutterPoint(new Point(e.getX(), e.getY()));
                }
            } else {
                drawingOnEdge = false;
                addPointOnEdge((int) e.getX(), (int) e.getY());
            }


//            doUpdate();

        });

        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, mouseEvent ->
                cursorLabel.setText("Координата курсора: " + mouseEvent.getX() + " ; " + mouseEvent.getY()));
    }

    private void addPointOnEdge(int x, int y) {
        if (edgeIndex >= 0 && edgeIndex < cutter.getEdges().size()) {
            Edge edge = cutter.getEdges().get(edgeIndex);
            Point beg = edge.getBegin();
            Point end = edge.getEnd();
            double tan = (end.getY() - beg.getY()) / (end.getX() - beg.getX());
            addPoint(x, beg.getY() + tan * (x - beg.getX()));
        }
    }

    private void addCutterPoint(Point point) {
        if (cutters.size() == 0) {
            cutter.add(point);
            int size = cutter.size();
            if (size > 1) {
                Point beg = cutter.get(size - 2);
                Point end = cutter.get(size - 1);
                LineDrawer.DrawLine(canvas, beg.getX(), beg.getY(),
                        end.getX(), end.getY(), cutterPicker.getValue());
            }
        } else {
            setAlert("Уже есть отсекатель!");
        }
    }

    private void closeCutter() {
        int size = cutter.size();
        if (size > 2) {
            addCutterPoint(cutter.get(0));
            boolean isConvex = cutter.isConvex();
            int sign = cutter.getSign();
            if (isConvex) {
                cutters.add(cutter);
                this.direction = sign;
            } else {
                setAlert("ОТСЕКАТЕЛЬ ДОЛЖЕН БЫТЬ ВЫПУКЛЫМ!");
            }
        }
        doUpdate();
    }

    private void closePolygon() {
        if (currentPolygon.size() > 1) {
            addPoint(currentPolygon.get(0).getBegin().getX(), currentPolygon.get(0).getBegin().getY());
            LinkedList<Edge> copy = new LinkedList<>(currentPolygon);
            polygons.add(new Polygon(copy));
            currentPolygon.clear();
            currentEdge.setBeginInit(false);
            currentEdge.setEndInit(false);
        }
    }

    private void setupColors() {
        cutterPicker.setValue(Color.BLACK);
        visiblePicker.setValue(Color.LIME);
        linePicker.setValue(Color.BLUE);
        clearCanvas();
    }

    private void clearCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }


    private void clearData() {
        currentEdge.clear();
        edges.clear();
        cutters.clear();
        cutter.clear();
        currentPolygon.clear();
        polygons.clear();
        allEdges.clear();
    }

    private void addPointHorizontal(int x, int y) {
        if (currentEdge.isEndInit()) {
            addPoint(x, currentEdge.getEnd().getY());
        } else if (currentEdge.isBeginInit()) {
            addPoint(x, currentEdge.getBegin().getY());
        } else {
            addPoint(x, y);
        }
    }

    private void addPointVertical(int x, int y) {
        if (currentEdge.isEndInit()) {
            addPoint(currentEdge.getEnd().getX(), y);
        } else if (currentEdge.isBeginInit()) {
            addPoint(currentEdge.getBegin().getX(), y);
        } else {
            addPoint(x, y);
        }
    }

    private void addPoint(double x, double y) {
        if (!currentEdge.isBeginInit()) {
            currentEdge.setBegin(new Point(x, y));
            currentEdge.setBeginInit(true);
        } else if (!currentEdge.isEndInit()) {
            if (!(currentEdge.getBegin().getX() == x && currentEdge.getBegin().getY() == y)) {
                currentEdge.setEnd(new Point(x, y));
                currentEdge.setEndInit(true);
                doUpdatePolygon();
            }
        } else {
            currentEdge.setBegin(currentEdge.getEnd());
            currentEdge.setEnd(new Point(x, y));
            doUpdatePolygon();
        }
    }

    private void doUpdatePolygon() {
        Edge copy = new Edge(currentEdge.getBegin(), currentEdge.getEnd());
        allEdges.add(copy);
        currentPolygon.add(copy);
        drawLine(copy);
        currentEdge = new Edge(new Point(currentEdge.getBegin()), currentEdge.getEnd());
        currentEdge.setBeginInit(true);
        currentEdge.setEndInit(true);
    }

    private void doUpdate() {
        clearCanvas();
        cut();
    }

    private void drawLine(Edge edge) {
        Point beg = edge.getBegin();
        Point end = edge.getEnd();
        drawLine(beg.getX(), beg.getY(), end.getX(), end.getY());
    }

    private void drawLine(double xBegin, double yBegin, double xEnd, double yEnd) {
        LineDrawer.DrawLine(canvas, xBegin, yBegin, xEnd, yEnd, linePicker.getValue());
    }


    private void drawCutter(Cutter c) {

        for (int i = 0; i < c.size() - 1; i++) {
            Point beg = c.get(i);
            Point end = c.get(i + 1);
            LineDrawer.DrawLine(canvas,
                    beg.getX(), beg.getY(),
                    end.getX(), end.getY(), cutterPicker.getValue());
        }
        Point beg = c.get(c.size() - 1);
        Point end = c.get(0);

        LineDrawer.DrawLine(canvas,
                beg.getX(), beg.getY(),
                end.getX(), end.getY(), cutterPicker.getValue());
    }

    private void drawUnfinished(Cutter c) {
        for (int i = 0; i < c.size() - 1; i++) {
            Point beg = c.get(i);
            Point end = c.get(i + 1);
            LineDrawer.DrawLine(canvas,
                    beg.getX(), beg.getY(),
                    end.getX(), end.getY(), cutterPicker.getValue());
        }
    }

    private void cut() {
        for (Edge e : allEdges) {
            drawLine(e);
        }

        if (!cutter.isFull()) {
            drawUnfinished(cutter);
        }

        for (Cutter c : cutters) {
            drawCutter(c);
        }

        for (Cutter c : cutters) {
            for (Polygon p : polygons) {
                cutSutherlandHodgman(c, p);
            }
        }

    }

    private boolean checkVisible(Point a, Point b, Point c, int normal) {
        double x1 = a.getX() - b.getX();
        double y1 = a.getY() - b.getY();
        double x2 = c.getX() - b.getX();
        double y2 = c.getY() - b.getY();
        double vectorCross = x1 * y2 - x2 * y1;
        return normal * vectorCross < 0;
    }

    private boolean hasIntersection(Edge a, Edge b, int normal) {
        boolean visibleBegin = checkVisible(a.getBegin(), b.getBegin(), b.getEnd(), normal);
        boolean visibleEnd = checkVisible(a.getEnd(), b.getBegin(), b.getEnd(), normal);
        return (visibleBegin && !visibleEnd) || (!visibleBegin && visibleEnd);
    }

    private Point intersection(Edge a, Edge b) {
        Point p1 = a.getBegin();
        Point p2 = a.getEnd();
        Point q1 = b.getBegin();
        Point q2 = b.getEnd();
        double x1 = p1.getX(), x2 = p2.getX(), x3 = q1.getX(), x4 = q2.getX();
        double y1 = p1.getY(), y2 = p2.getY(), y3 = q1.getY(), y4 = q2.getY();
        double intersectionCoef = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3))
                / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));

        return new Point(x1 + intersectionCoef * (x2 - x1), y1 + intersectionCoef * (y2 - y1));
    }

    private void cutSutherlandHodgman(Cutter cutter, Polygon polygon) {
        if (!cutter.isConvex()) {
            return;
        }

        int normal = cutter.getSign();
        ArrayList<Point> cutterVertices = cutter.getVertices();
        ArrayList<Point> polygonVertices = polygon.getVertices();
        Point S = new Point(0, 0);
        Point F = new Point(0, 0);
        for (int i = 0; i < cutterVertices.size() - 1; i++) {
            ArrayList<Point> output = new ArrayList<>();
            for (int j = 0; j < polygonVertices.size(); j++) {
                if (j == 0) {
                    F = polygonVertices.get(j);
                } else {
                    Edge a = new Edge(S, polygonVertices.get(j));
                    Edge b = new Edge(cutterVertices.get(i), cutterVertices.get(i + 1));
                    if (hasIntersection(a, b, normal)) {
                        output.add(intersection(a, b));
                    }
                }
                S = polygonVertices.get(j);
                if (checkVisible(S, cutterVertices.get(i), cutterVertices.get(i + 1), normal)) {
                    output.add(S);
                }

            }
            if (output.size() != 0) {
                Edge a = new Edge(S, F);
                Edge b = new Edge(cutterVertices.get(i), cutterVertices.get(i + 1));
                if (hasIntersection(a, b, normal)) {
                    output.add(intersection(a, b));
                }
            }

            if (output.size() != 0)
                polygonVertices = (ArrayList<Point>) output.clone();

        }
        drawVisible(polygonVertices);
    }

    private void drawVisible(ArrayList<Point> input) {
        if (input.size() > 1) {
            for (int i = 0; i < input.size() - 1; i++) {
                LineDrawer.DrawLineD(canvas,
                        input.get(i).getX(), input.get(i).getY(),
                        input.get(i + 1).getX(), input.get(i + 1).getY(),
                        visiblePicker.getValue());
            }
            LineDrawer.DrawLineD(canvas, input.get(input.size() - 1).getX(), input.get(input.size() - 1).getY(),
                    input.get(0).getX(), input.get(0).getY(), visiblePicker.getValue());
        }
    }


    private void setAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.setTitle("Произошла ошибка :(");
        alert.setHeaderText("ОШИБКА");
        alert.show();
    }

}
