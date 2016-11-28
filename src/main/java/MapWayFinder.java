import model.Game;
import model.LaneType;
import model.Wizard;
import model.World;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.StrictMath.abs;

public class MapWayFinder {

    private World world;
    private Game game;
    private Wizard wizard;
    private MapHelper mapHelper;

    public static final double NEXT_LINE_DISTANCE = 120D;
    public static final double NEXT_LINE_DISTANCE_MULTIPLIER = 1.1;

    public MapWayFinder(World world, Game game, Wizard wizard) {
        this.wizard = wizard;
        this.mapHelper = new MapHelper(world, game, wizard);
    }

    public Point2D getPointTo(Point2D point, boolean safeWay) {
        List<LinePosition> wizardPositions = mapHelper.getLinePositions(wizard);

        return getPointTo(point, safeWay, wizardPositions);
    }

    public Point2D getNextWaypoint(LaneType laneType) {
        List<LinePosition> wizardPositions = mapHelper.getLinePositions(wizard);
        Optional<LinePosition> wizardLinePosition = mapHelper.getWizardLinePosition(wizardPositions, laneType);

        if (wizardLinePosition.isPresent())
            return findNextPoint(wizardLinePosition.get(), laneType);

        return getStartLinePoint(wizardPositions, laneType);
    }

    public Point2D getPreviousWaypoint(LaneType laneType) {
        List<LinePosition> wizardPositions = mapHelper.getLinePositions(wizard);
        Optional<LinePosition> wizardLinePosition = mapHelper.getWizardLinePosition(wizardPositions, laneType);

        if (wizardLinePosition.isPresent())
            return findPreviousPoint(wizardLinePosition.get(), laneType);

        return getStartLinePoint(wizardPositions, laneType);
    }

    private Point2D findNextPoint(LinePosition wizardLinePosition, LaneType laneType) {
        //TODO: add checking of enemyPosition

        if (wizardLinePosition.getPosition() > wizardLinePosition.getMapLine().getLineLength() - NEXT_LINE_DISTANCE) {
            Optional<MapLine> nextLine = wizardLinePosition.getMapLine().getEndLines().stream()
                    .filter(line -> line.getLaneType() == laneType).findFirst();

            if (nextLine.isPresent())
                return mapHelper.getPointInLine(nextLine.get(), NEXT_LINE_DISTANCE);
            else
                return wizardLinePosition.getMapLine().getEndPoint();
        } else
            return wizardLinePosition.getMapLine().getEndPoint();
    }

    private Point2D findPreviousPoint(LinePosition wizardLinePosition, LaneType laneType) {
        if (wizardLinePosition.getPosition() < NEXT_LINE_DISTANCE) {
            Optional<MapLine> previousLine = wizardLinePosition.getMapLine().getStartLines().stream()
                    .filter(line -> line.getLaneType() == laneType).findFirst();

            if (previousLine.isPresent())
                return mapHelper.getPointInLine(previousLine.get(), previousLine.get().getLineLength() - NEXT_LINE_DISTANCE);
            else
                return wizardLinePosition.getMapLine().getStartPoint();
        } else
            return wizardLinePosition.getMapLine().getStartPoint();
    }

    private Point2D getStartLinePoint(List<LinePosition> wizardPositions, LaneType laneType) {
        MapLine friendLine = MapHelper.mapLines.stream()
                .filter(line -> line.getLaneType().equals(laneType) && line.getStartPoint().equals(MapHelper.friendBasePoint))
                .findAny().get();

        if (friendLine.getMapLineStatus() == MapLineStatus.GREEN)
            return getPointTo(friendLine.getEndPoint(), true, wizardPositions);
        else
            return getPointTo(friendLine.getStartPoint(), true, wizardPositions);
    }

    private Point2D getPointTo(Point2D point, boolean safeWay, List<LinePosition> wizardPositions) {
        double pointDistance = point.getDistanceTo(wizard);

        if (pointDistance < NEXT_LINE_DISTANCE)
            return point;

        if (wizardPositions == null || wizardPositions.isEmpty()) {
            return mapHelper.getNearestPointInLine(wizard, true);
        } else {
            boolean wizardEnemySector = wizardPositions.stream()
                    .allMatch(linePosition -> linePosition.getMapLine().getEnemy());

            if (wizardEnemySector)
                return wizardPositions.get(0).getMapLine().getStartPoint();

            List<LinePosition> pointLinePositions = mapHelper.getLinePositions(point.getX(), point.getY());

            Optional<Pair<LinePosition, LinePosition>> commonLine = wizardPositions.stream()
                    .flatMap(wizardPosition -> pointLinePositions.stream()
                            .map(pointLinePosition -> getCommonLinePosition(wizardPosition, pointLinePosition))
                    ).filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();

            if (commonLine.isPresent()) {
                if (!safeWay) {
                    return mapHelper.getPointInLine(commonLine.get().getSecond().getMapLine(), commonLine.get().getSecond().getPosition());
                } else {
                    double wizardLinePosition = commonLine.get().getFirst().getPosition();
                    double pointLinePosition = commonLine.get().getSecond().getPosition();

                    double start = StrictMath.min(wizardLinePosition, pointLinePosition);
                    double end = StrictMath.min(wizardLinePosition, pointLinePosition);

                    boolean isSafe = commonLine.get().getFirst().getMapLine()
                            .getEnemyWizardPositions().values().stream()
                            .allMatch(enemyWizardsLocation -> enemyWizardsLocation <= start && enemyWizardsLocation >= end);

                    if (isSafe)
                        return mapHelper.getPointInLine(commonLine.get().getSecond());
                }
            }

            return findMapWayPoint(wizardPositions, pointLinePositions, safeWay);
        }
    }

    private Optional<Pair<LinePosition, LinePosition>> getCommonLinePosition(LinePosition wizardPosition, LinePosition pointLinePosition) {
        if (wizardPosition.getMapLine().equals(pointLinePosition.getMapLine()))
            return Optional.of(new Pair<>(wizardPosition, pointLinePosition));
        else
            return Optional.empty();
    }

    private Point2D findMapWayPoint(List<LinePosition> wizardPositions, List<LinePosition> pointLinePositions, boolean safeWay) {
        Optional<Pair<Double, Point2D>> findPoint = findMapWayPoint(wizardPositions.get(0), pointLinePositions, safeWay);

        if (findPoint.isPresent())
            return findPoint.get().getSecond();
        else if (safeWay) {
            Optional<Pair<Double, Point2D>> findValue = findMapWayPoint(wizardPositions.get(0), pointLinePositions, false);

            if (findValue.isPresent())
                return findValue.get().getSecond();
        }

        return mapHelper.friendBasePoint;
    }

    private Optional<Pair<Double, Point2D>> findMapWayPoint(LinePosition wizardLinePosition, List<LinePosition> pointLinePositions, boolean safeWay) {
        List<MapLine> startLines = wizardLinePosition.getMapLine().getStartLines();
        List<MapLine> endLines = wizardLinePosition.getMapLine().getEndLines();
        MapLine wizardMapLine = wizardLinePosition.getMapLine();

        if (safeWay) {
            Map<Point2D, Double> enemyWizardPositions = wizardMapLine.getEnemyWizardPositions();

            if (enemyWizardPositions.size() > 0) {
                boolean isStartSafe = enemyWizardPositions.values().stream()
                        .allMatch(enemyWizardsLocation -> enemyWizardsLocation >= wizardLinePosition.getPosition());

                if (!isStartSafe)
                    startLines = null;

                boolean isEndSafe = enemyWizardPositions.values().stream()
                        .allMatch(enemyWizardsLocation -> enemyWizardsLocation <= wizardLinePosition.getPosition());

                if (!isEndSafe)
                    endLines = null;
            }
        }

        List<Pair<Double, MapLine>> findWays = new ArrayList<>();
        if (startLines != null) {
            Point2D startPoint = wizardMapLine.getStartPoint();
            double startPosition = wizardLinePosition.getPosition();

            List<Pair<Double, MapLine>> startWays = getWays(
                    pointLinePositions, safeWay, startLines, startPoint, startPosition, wizardMapLine);

            findWays.addAll(startWays);
        }

        if (endLines != null) {
            List<Pair<Double, MapLine>> stopWays = getWays(
                    pointLinePositions, safeWay, endLines, wizardMapLine.getEndPoint(),
                    wizardMapLine.getLineLength() - wizardLinePosition.getPosition(), wizardMapLine);

            findWays.addAll(stopWays);
        }

        Optional<Pair<Double, Point2D>> minValue = findWays.stream()
                .min(Comparator.comparing(Pair::getFirst))
                .map(pair -> new Pair<>(pair.getFirst(), getPointFromMapLine(wizardLinePosition, pair.getSecond())));

        return minValue;
    }

    private Point2D getPointFromMapLine(LinePosition wizardLinePosition, MapLine line) {
        Double positionToLinePoint = wizardLinePosition.getPosition();
        Point2D wayPoint = wizardLinePosition.getMapLine().getStartPoint();
        if (!wizardLinePosition.getMapLine().getStartLines().contains(line)) {
            positionToLinePoint = wizardLinePosition.getMapLine().getLineLength() - positionToLinePoint;
            wayPoint = wizardLinePosition.getMapLine().getEndPoint();
        }

        if (positionToLinePoint >= NEXT_LINE_DISTANCE) {
            return wayPoint;
        } else {
            double linePosition = NEXT_LINE_DISTANCE * NEXT_LINE_DISTANCE_MULTIPLIER;
            if (line.getEndPoint().equals(wayPoint))
                linePosition = line.getLineLength() - linePosition;

            return mapHelper.getPointInLine(new LinePosition(line, linePosition));
        }
    }


    private List<Pair<Double, MapLine>> getWays(List<LinePosition> pointLinePositions, boolean safeWay, List<MapLine> startLines, Point2D startPoint, double startPosition, MapLine wizardLine) {
        return startLines.stream()
                .map(mapLine -> {
                    List<MapLine> wayLines = new ArrayList<>();
                    wayLines.add(wizardLine);
                    wayLines.add(mapLine);

                    WayParams wayParams = new WayParams(
                            mapLine,
                            mapLine,
                            startPoint,
                            startPosition,
                            pointLinePositions,
                            safeWay,
                            wayLines
                    );

                    return findMapWayPoint(wayParams);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<Pair<Double, MapLine>> findMapWayPoint(WayParams wayParams) {
        if (wayParams.getWayLines().size() > 3) return Optional.empty();

        MapLine mapLine = wayParams.getMapLine();

        List<MapLine> furtherLines = mapLine.getStartLines();
        if (wayParams.getStartPoint().equals(mapLine.getEndPoint()))
            furtherLines = mapLine.getEndLines();

        List<MapLine> filteredFurtherLines = furtherLines.stream()
                .filter(furtherLine -> !wayParams.getWayLines().contains(furtherLine))
                .collect(Collectors.toList());

        if (filteredFurtherLines.isEmpty()) return Optional.empty();

        Optional<LinePosition> findWay = wayParams.getPointLinePositions().stream()
                .filter(linePosition -> {
                    MapLine checkedMapLine = linePosition.getMapLine();
                    if (filteredFurtherLines.contains(checkedMapLine)) {
                        if (!wayParams.isSafeWay()) return true;

                        if (checkedMapLine.getMapLineStatus() == MapLineStatus.GREEN) return true;

                        if (checkedMapLine.getStartPoint().equals(wayParams.getStartPoint()))
                            return checkedMapLine.getEnemyWizardPositions().values().stream()
                                    .anyMatch(position -> linePosition.getPosition() < position);
                        else
                            return checkedMapLine.getEnemyWizardPositions().values().stream()
                                    .anyMatch(position -> linePosition.getPosition() > position);

                    } else
                        return false;
                })
                .findFirst();

        if (findWay.isPresent()) {
            double newLength = wayParams.getStartDestination();
            if (wayParams.getStartPoint().equals(findWay.get().getMapLine().getStartPoint()))
                newLength += findWay.get().getPosition();
            else
                newLength += findWay.get().getMapLine().getLineLength() - findWay.get().getPosition();

            return Optional.of(new Pair<>(newLength, wayParams.getStartMapLine()));
        } else {
            return filteredFurtherLines.stream()
                    .map(furtherLine -> {
                        List<MapLine> newWayLines = new ArrayList<>();
                        newWayLines.addAll(wayParams.getWayLines());
                        newWayLines.add(furtherLine);

                        Point2D nextPoint = furtherLine.getStartPoint();
                        if (!furtherLine.getStartPoint().equals(wayParams.getMapLine().getEndPoint()))
                            nextPoint = furtherLine.getEndPoint();

                        WayParams newWayParams = new WayParams(
                                wayParams.getStartMapLine(),
                                furtherLine,
                                nextPoint,
                                wayParams.getStartDestination() + wayParams.getMapLine().getLineLength(),
                                wayParams.getPointLinePositions(),
                                wayParams.isSafeWay(),
                                newWayLines
                        );

                        return findMapWayPoint(wayParams);
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .min(Comparator.comparing(Pair::getFirst));
        }

    }


}
