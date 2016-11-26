import model.LaneType;
import model.Wizard;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineHelper {

    private double mapSize;
    private Wizard wizard;

    private Map<LaneType, List<Point2D>> mainLines = new HashMap<>();

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

    public LineHelper(Wizard wizard, double mapSize) {
        this.mapSize = mapSize;
        this.wizard = wizard;

        mainLines.put(LaneType.TOP, topLine);
        mainLines.put(LaneType.MIDDLE, middleLine);
        mainLines.put(LaneType.BOTTOM, bottomLine);
    }

    public Point2D getNextWaypoint(LaneType laneType, boolean attack) {
        //TODO;
        return null;
    }

    public Point2D getPreviousWaypoint(LaneType laneType, boolean attack) {
        return null;
    }

}
