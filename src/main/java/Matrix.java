import java.util.HashMap;
import java.util.Map;

public class Matrix {

    private Map<WayPoint, Matrix> nearestMatrix = new HashMap<>();

    private int pathCount = 0;
    private boolean isFree = true;

    private Point2D point;

    public Matrix(Point2D point) {
        this.point = point;
    }

    public Point2D getPoint() {
        return point;
    }

    public void addMatrix(WayPoint wayPoint, Matrix matrix) {
        nearestMatrix.put(wayPoint, matrix);
    }

    public boolean matrixExist(WayPoint wayPoint) {
        if (nearestMatrix.containsKey(wayPoint)) return true;

        return nearestMatrix.entrySet().stream()
                .anyMatch(entry -> checkPosition(entry, wayPoint));
    }

    private boolean checkPosition(Map.Entry<WayPoint, Matrix> entry, WayPoint wayPoint) {
        WayPoint parentWayPoint = entry.getKey();

        return //FIXME
    }

    public Map<WayPoint, Matrix> getNearestMatrix() {
        return nearestMatrix;
    }

    public int getPathCount() {
        return pathCount;
    }

    public void setPathCount(int pathCount) {
        this.pathCount = pathCount;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean free) {
        isFree = free;
    }
}
