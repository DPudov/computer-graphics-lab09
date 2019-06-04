package sample;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.LinkedList;
import java.util.Optional;

public class Controller {
    @FXML
    Button parallelButton;
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
    TextField inputX2Field;
    @FXML
    TextField inputY2Field;
    @FXML
    TextField inputY1Field;
    @FXML
    Button addLineButton;

    private Cutter cutter = new Cutter();
    private Edge currentLine = new Edge();
    private LinkedList<Edge> edges = new LinkedList<>();
    private LinkedList<Cutter> cutters = new LinkedList<>();
    private boolean isConvex;
    private int direction = -1;
    private int edgeNumber = 0;
    private boolean begParallelInit = false;
    private double tan = 0;
    private int xb;
    private int yb;

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
                Point p2 = new Point(Integer.parseInt(inputX2Field.getText()),
                        Integer.parseInt(inputY2Field.getText()));
                edges.add(new Edge(p1, p2));
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
        parallelButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            if (cutters.size() != 0) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Введите номер ребра");
                dialog.setContentText("Номер");
                dialog.setHeaderText("Введите номер ребра");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(number -> {
                    this.edgeNumber = Integer.parseInt(number);
                    Point beg;
                    Point end;

                    if (edgeNumber < cutter.size() - 1) {
                        beg = cutter.get(edgeNumber);
                        end = cutter.get(edgeNumber + 1);
                        tan = (end.getY() - beg.getY()) / (end.getX() - beg.getX());
                    } else if (edgeNumber == cutter.size() - 1) {
                        beg = cutter.get(edgeNumber);
                        end = cutter.get(0);
                        tan = (end.getY() - beg.getY()) / (end.getX() - beg.getX());
                    }
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

            if (!begParallelInit) {
                if (b == MouseButton.PRIMARY && hasShift && hasControl) {
                    //Прямая
                    addPointParallel((int) e.getX(), (int) e.getY());
                } else if (b == MouseButton.PRIMARY && hasShift) {
                    // горизонтальная
                    addPointHorizontal((int) e.getX(), (int) e.getY());
                } else if (b == MouseButton.PRIMARY && hasControl) {
                    // вертикальная
                    addPointVertical((int) e.getX(), (int) e.getY());
                } else if (b == MouseButton.PRIMARY) {
                    addPoint((int) e.getX(), (int) e.getY());
                    // Прямая
                } else if (b == MouseButton.SECONDARY && hasControl) {
                    addCutterPoint(new Point(e.getX(), e.getY()));
                } else if (b == MouseButton.SECONDARY) {
                    closeCutter();
                }
            } else {
                addPointParallel((int) e.getX(), (int) e.getY());
            }

            doUpdate();

        });

        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, mouseEvent -> {
            cursorLabel.setText("Координата курсора: " + mouseEvent.getX() + " ; " + mouseEvent.getY());
        });
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
                this.isConvex = true;
                this.direction = sign;
            } else {
                this.isConvex = false;
                setAlert("МНОГОУГОЛЬНИК ДОЛЖЕН БЫТЬ ВЫПУКЛЫМ!");
            }
        }
        doUpdate();
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
        currentLine.clear();
        edges.clear();
        cutters.clear();
        cutter.clear();
    }

    private void addPointParallel(int x, int y) {
        if (cutters.size() > 0) {

            if (!begParallelInit) {
                addPoint(x, y);
                begParallelInit = true;
                this.xb = x;
                this.yb = y;
            } else {
                addPoint(x, yb + tan * (x - xb));
                begParallelInit = false;
            }
        }
    }

    private void addPointHorizontal(int x, int y) {
        if (currentLine.isEndInit()) {
            addPoint(x, currentLine.getEnd().getY());
        } else if (currentLine.isBeginInit()) {
            addPoint(x, currentLine.getBegin().getY());
        } else {
            addPoint(x, y);
        }
    }

    private void addPointVertical(int x, int y) {
        if (currentLine.isEndInit()) {
            addPoint(currentLine.getEnd().getX(), y);
        } else if (currentLine.isBeginInit()) {
            addPoint(currentLine.getBegin().getX(), y);
        } else {
            addPoint(x, y);
        }
    }

    private void addPoint(double x, double y) {
        if (!currentLine.isBeginInit()) {
            currentLine.setBegin(new Point(x, y));
            currentLine.setBeginInit(true);
        } else if (!currentLine.isEndInit()) {
            edges.add(new Edge(currentLine.getBegin(), new Point(x, y)));
            doUpdate();
            currentLine.clear();
        }
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
            for (Edge e : edges) {
                cutCyrusBeck(c, e, direction);
            }
        }

    }

    private double scalarMulti(Point a, Point b) {
        return a.getX() * b.getX() + a.getY() * b.getY();
    }

    private void cutCyrusBeck(Cutter cutter, Edge edge, int n) {
        double tb = 0;
        double te = 1;
        Point D = new Point(edge.getEnd().getX() - edge.getBegin().getX(),
                edge.getEnd().getY() - edge.getBegin().getY());

        for (int i = 0; i < cutter.size() - 1; i++) {
            Point W = new Point(edge.getBegin().getX() - cutter.get(i).getX(),
                    edge.getBegin().getY() - cutter.get(i).getY());
            Point N = new Point(-n * (cutter.get(i + 1).getY() - cutter.get(i).getY()),
                    n * (cutter.get(i + 1).getX() - cutter.get(i).getX()));

            double Dscalar = scalarMulti(D, N);
            double Wscalar = scalarMulti(W, N);

            if (Dscalar == 0) {
                if (Wscalar < 0) {
                    return;
                }
            } else {
                double t = -Wscalar / Dscalar;
                if (Dscalar > 0) {
                    if (t > 1) {
                        return;
                    } else {
                        tb = Math.max(tb, t);
                    }
                } else if (Dscalar < 0) {
                    if (t < 0) {
                        return;
                    } else {
                        te = Math.min(te, t);
                    }
                }
            }
        }

        if (tb <= te) {
            double xBegin = edge.getBegin().getX() + (edge.getEnd().getX() - edge.getBegin().getX()) * te;
            double yBegin = edge.getBegin().getY() + (edge.getEnd().getY() - edge.getBegin().getY()) * te;
            double xEnd = edge.getBegin().getX() + (edge.getEnd().getX() - edge.getBegin().getX()) * tb;
            double yEnd = edge.getBegin().getY() + (edge.getEnd().getY() - edge.getBegin().getY()) * tb;
            LineDrawer.DigitalDiffAnalyzeDraw(canvas, xBegin, yBegin, xEnd, yEnd, visiblePicker.getValue());
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
