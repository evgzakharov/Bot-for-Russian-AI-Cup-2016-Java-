import model.LaneType;
import model.Wizard;

import java.util.*;
import java.util.stream.Collectors;

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
        List<Point2D> points = mainLines.get(laneType);

        int start = 0;
        int diff = 1;
        if (!attack) {
            start = points.size() - 1;
            diff = -1;
        }

        for (int index = start; index > 0 && index <= points.size(); index += diff) {

        }

        return null;
    }

    public Point2D getPreviousWaypoint(LaneType laneType, boolean attack) {
        return null;
    }

}
