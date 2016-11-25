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
        Matrix matrixStart = new Matrix(new Point2D(wizard.getX(), wizard.getY()), null);
        matrixStart.setPathCount(0);

        List<Point2D> findLine = growMatrix(Collections.singletonList(matrixStart), point);

        return findLine;
    }

    private List<Point2D> growMatrix(List<Matrix> stepPoints, Point2D findingWayPoint) {
        List<Matrix> newStepPoints = new ArrayList<>();

        for (Matrix stepMatrix : stepPoints) {
            for (short diffX = -1; diffX <= 1; diffX++) {
                for (short diffY = -1; diffY <= 1; diffY++) {
                    if (diffX == 0 && diffY == 0) continue;

                    MatrixPoint matrixPoint = new MatrixPoint(diffX, diffY);
                    if (stepMatrix.getMatrixPoints().contains(matrixPoint))
                        continue;

                    double newX = stepMatrix.getPoint().getX() + matrixStep * diffX;
                    double newY = stepMatrix.getPoint().getY() + matrixStep * diffY;
                    Point2D newPoint = new Point2D(newX, newY);

                    if (!checkPointPosition(newPoint, findingWayPoint)) continue;

                    Matrix newMatrix = new Matrix(newPoint, stepMatrix);
                    newMatrix.setPathCount(stepMatrix.getPathCount() + 1);

                    //TODO: checking free location
                    newStepPoints.add(newMatrix);
                    newMatrix.getMatrixPoints().add(matrixPoint);

                    if (newPoint.getDistanceTo(findingWayPoint) < matrixStep)
                        return findLineFromMatrix(stepMatrix);
                }
            }
        }

        if (!newStepPoints.isEmpty()) {
            growMatrix(newStepPoints, findingWayPoint);
        }

        return Collections.emptyList();
    }

    private List<Point2D> findLineFromMatrix(Matrix stepMatrix) {
        List<Point2D> findPoints = new ArrayList<>();

        int currentCount = stepMatrix.getPathCount();
        Matrix currentMatrix = stepMatrix;
        while (currentCount > 0) {
            findPoints.add(findPoints.size(), currentMatrix.getPoint());
            currentCount -= 1;

            currentMatrix = currentMatrix.getPreviousMatrix();
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
