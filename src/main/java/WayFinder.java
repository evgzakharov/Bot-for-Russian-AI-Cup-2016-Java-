import model.*;

import static java.lang.StrictMath.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WayFinder {

    private Wizard wizard;
    private World world;
    private Game game;

    private double matrixStep;

    private static final double MAX_RANGE = 100;

    public WayFinder(Wizard wizard, World world, Game game, double matrixStep) {
        this.wizard = wizard;
        this.world = world;
        this.game = game;
        this.matrixStep = matrixStep;
    }

    public List<Point2D> findWay(Point2D point) {
        Matrix matrixStart = new Matrix(new Point2D(wizard.getX(), wizard.getY()));
        matrixStart.setFree(true);
        matrixStart.setPathCount(0);

        List<Point2D> findLine = growMatrix(Collections.singletonList(matrixStart), point);

        return findLine;
    }

    private List<Point2D> growMatrix(List<Matrix> stepPoints, Point2D findingWayPoint) {
        List<Matrix> newStepPoints = new ArrayList<>();

        for (Matrix stepMatrix : stepPoints) {
            for (WayX wayX : WayX.values()) {
                for (WayY wayY : WayY.values()) {
                    if (wayX == WayX.NONE && wayY == WayY.NONE) continue;

                    WayPoint newWayPoint = new WayPoint(wayX, wayY);
                    if (stepMatrix.matrixExist(newWayPoint)) continue;

                    double newX = stepMatrix.getPoint().getX() + matrixStep * wayX.getDiff();
                    double newY = stepMatrix.getPoint().getY() + matrixStep * wayY.getDiff();
                    Point2D newPoint = new Point2D(newX, newY);

                    if (!checkPointPosition(newPoint, findingWayPoint)) continue;

                    Matrix newMatrix = new Matrix(newPoint);
                    newMatrix.setPathCount(stepMatrix.getPathCount() + 1);

                    newMatrix.setFree(true); //TODO add checking
                    newStepPoints.add(newMatrix);

                    stepMatrix.addMatrix(new WayPoint(wayX, wayY), newMatrix);
                    newMatrix.addMatrix(reverse(new WayPoint(wayX, wayY)), stepMatrix);

                    if (newPoint.getDistanceTo(findingWayPoint) < matrixStep)
                        return findLineFromMatrix(stepMatrix);
                }
            }

            fixRelations(stepMatrix);
        }

        if (!newStepPoints.isEmpty()) {
            growMatrix(newStepPoints, findingWayPoint);
        }

        return Collections.emptyList();
    }

    private void fixRelations(Matrix stepMatrix) {

    }

    private WayPoint reverse(WayPoint wayPoint) {
        return new WayPoint(WayX.fromValue(wayPoint.getWayX().getDiff() * (-1)), WayY.fromValue(wayPoint.getWayY().getDiff() * (-1)));
    }

    private List<Point2D> findLineFromMatrix(Matrix stepMatrix) {
        List<Point2D> findPoints = new ArrayList<>();

        int currentCount = stepMatrix.getPathCount();
        Matrix currentMatrix = stepMatrix;
        while (currentCount > 0) {
            findPoints.add(currentMatrix.getPoint());
            currentCount -= 1;

            final int findCountValue = currentCount;
            currentMatrix = currentMatrix.getNearestMatrix().values().stream()
                    .filter(matrix -> matrix.getPathCount() == findCountValue)
                    .findFirst()
                    .orElse(null);
        }

        return findPoints;
    }

    private boolean checkPointPosition(Point2D newPoint, Point2D wayPoint) {
        return inRange(newPoint.getX(), wayPoint.getX(), wizard.getX()) &&
                inRange(newPoint.getY(), wayPoint.getY(), wizard.getY());
    }

    private boolean inRange(double newValue, double wayPointValue, double wizardValue) {
        double min = min(wizardValue, wayPointValue);
        double max = max(wizardValue, wayPointValue);

        double minDiff = min(abs(newValue - wayPointValue), abs(newValue - wizardValue));

        return (newValue >= min && newValue <= max) || minDiff <= MAX_RANGE;
    }

}
