import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Matrix {

    private int pathCount = 0;

    private Point2D point;
    private Matrix previousMatrix;
    private Set<MatrixPoint> matrixPoints;

    public Matrix(Point2D point, Matrix previousMatrix) {
        this.point = point;
        this.previousMatrix = previousMatrix;

        if (previousMatrix != null)
            this.matrixPoints = previousMatrix.getMatrixPoints();
        else
            this.matrixPoints = new HashSet<>();
    }

    public int getPathCount() {
        return pathCount;
    }

    public void setPathCount(int pathCount) {
        this.pathCount = pathCount;
    }

    public Point2D getPoint() {
        return point;
    }

    public Matrix getPreviousMatrix() {
        return previousMatrix;
    }

    public Set<MatrixPoint> getMatrixPoints() {
        return matrixPoints;
    }
}
