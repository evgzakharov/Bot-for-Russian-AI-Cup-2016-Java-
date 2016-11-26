import java.util.Arrays;
import java.util.List;

public class LineHelper {

    private double mapSize;

    public LineHelper(double mapSize) {
        this.mapSize = mapSize;
    }

    public List<Point2D> middleLine = Arrays.asList(
            new Point2D(100.0D, mapSize - 100.0D),
            new Point2D(800.0D, mapSize - 800.0D),
            new Point2D(mapSize - 200.0D, 200.0D)
    );

    public List<Point2D> topLine = Arrays.asList(
            new Point2D(100.0D, mapSize - 100.0D),
            new Point2D(200.0D, 200.0D),
            new Point2D(mapSize - 200.0D, 200.0D)
    );

    public List<Point2D> bottomLine = Arrays.asList(
            new Point2D(100.0D, mapSize - 100.0D),
            new Point2D(mapSize - 200.0D, mapSize - 200.0D),
            new Point2D(mapSize - 200.0D, 200.0D)
    );

    public List<Point2D> artifactLine = Arrays.asList(
            new Point2D(100.0D, mapSize - 100.0D),
            new Point2D(mapSize - 200.0D, 200.0D)
    );
}
