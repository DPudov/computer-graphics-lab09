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

public class Controller {
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
    TextField inputX1Field;
    @FXML
    TextField inputY1Field;
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
                Point p1 = new Point(Integer.parseInt(inputX1Field.getText()),
                        Integer.parseInt(inputY1Field.getText()));
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
    }

    private void setupCanvasListeners() {
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            MouseButton b = e.getButton();
            boolean hasShift = e.isShiftDown();
            boolean hasControl = e.isControlDown();

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

//            doUpdate();

        });

        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, mouseEvent ->
                cursorLabel.setText("Координата курсора: " + mouseEvent.getX() + " ; " + mouseEvent.getY()));
    }

    private void addCutterPoint(Point point) {
        if (cutters.size() == 0) {
            cutter.add(point);
            int size = cutter.size();
            if (size > 1) {
                Point beg = cutter.get(size - 2);
                Point end = cutter.get(size - 1);
                LineDrawer.DigitalDiffAnalyzeDraw(canvas, beg.getX(), beg.getY(),
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
//        clearCanvas();
        cut();
    }

    private void drawLine(Edge edge) {
        Point beg = edge.getBegin();
        Point end = edge.getEnd();
        drawLine(beg.getX(), beg.getY(), end.getX(), end.getY());
    }

    private void drawLine(double xBegin, double yBegin, double xEnd, double yEnd) {
        LineDrawer.DigitalDiffAnalyzeDraw(canvas, xBegin, yBegin, xEnd, yEnd, linePicker.getValue());
    }


    private void drawCutter(Cutter c) {

        for (int i = 0; i < c.size() - 1; i++) {
            Point beg = c.get(i);
            Point end = c.get(i + 1);
            LineDrawer.DigitalDiffAnalyzeDraw(canvas,
                    beg.getX(), beg.getY(),
                    end.getX(), end.getY(), cutterPicker.getValue());
        }
        Point beg = c.get(c.size() - 1);
        Point end = c.get(0);

        LineDrawer.DigitalDiffAnalyzeDraw(canvas,
                beg.getX(), beg.getY(),
                end.getX(), end.getY(), cutterPicker.getValue());
    }

    private void drawUnfinished(Cutter c) {
        for (int i = 0; i < c.size() - 1; i++) {
            Point beg = c.get(i);
            Point end = c.get(i + 1);
            LineDrawer.DigitalDiffAnalyzeDraw(canvas,
                    beg.getX(), beg.getY(),
                    end.getX(), end.getY(), cutterPicker.getValue());
        }
    }

    private void cut() {
        for (Edge e : edges) {
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

    private void cutSutherlandHodgman(Cutter cutter, Polygon polygon) {
        ArrayList<Edge> edges = cutter.getEdges();
        ArrayList<Point> input = polygon.getVertices();
        if (input.isEmpty() || edges.isEmpty()) {
            return;
        }
        ArrayList<Point> output = new ArrayList<>();
        int caseNumber = 0;
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            Point r = edges.get((i + 2) % edges.size()).getBegin();
            Point s = input.get(input.size() - 1);
            for (Point p : input) {
                if (Edge.isPointInsideEdge(edge, r, p)) {
                    if (Edge.isPointInsideEdge(edge, r, s)) {
                        caseNumber = 1;
                    } else {
                        caseNumber = 4;
                    }
                } else {
                    if (Edge.isPointInsideEdge(edge, r, s)) {
                        caseNumber = 2;
                    } else {
                        caseNumber = 3;
                    }
                }
                switch (caseNumber) {
                    case 1:
                        output.add(p);
                        break;
                    case 2:
                        Point pi = edge.computeIntersection(s, p);
                        output.add(pi);
                        break;
                    case 3:
                        break;
                    case 4:
                        Point pa = edge.computeIntersection(s, p);
                        output.add(pa);
                        output.add(p);
                        break;
                }
                s = p;
            }
            if (output.isEmpty()) {
                drawVisible(input);
                return;
            }

            input = (ArrayList<Point>) output.clone();

            output.clear();
        }
        drawVisible(input);
    }

    private void drawVisible(ArrayList<Point> input) {
        if (input.size() > 1) {
            for (int i = 0; i < input.size() - 1; i++) {
                LineDrawer.DigitalDiffAnalyzeDraw(canvas,
                        input.get(i).getX(), input.get(i).getY(),
                        input.get(i + 1).getX(), input.get(i + 1).getY(),
                        visiblePicker.getValue());
            }
            LineDrawer.DigitalDiffAnalyzeDraw(canvas, input.get(input.size() - 1).getX(), input.get(input.size() - 1).getY(),
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
