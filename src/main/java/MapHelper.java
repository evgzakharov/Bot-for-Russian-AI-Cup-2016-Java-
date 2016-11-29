import model.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.StrictMath.*;

public class MapHelper {

    public static final List<MapLine> mapLines = new ArrayList<>();
    public static final List<Point2D> mapPoints = new ArrayList<>();

    public static double mapSize = 4000D;

    public static final Point2D friendBasePoint = new Point2D(100.0D, mapSize - 100.0D);
    public static Point2D topPoint = new Point2D(200.0D, 200.0D);
    public static Point2D middlePoint = new Point2D(1876.0D, 2026);
    public static Point2D bottomPoint = new Point2D(mapSize - 100.0D, mapSize - 100.0D);
    public static Point2D enemyBasePoint = new Point2D(mapSize - 100.0D, 100.0D);

    public static MapLine topFriendLine = new MapLine(friendBasePoint, topPoint, LaneType.TOP, false);
    public static MapLine topEnemyLine = new MapLine(topPoint, enemyBasePoint, LaneType.TOP, true);

    public static MapLine middleFriendLine = new MapLine(friendBasePoint, middlePoint, LaneType.MIDDLE, false);
    public static MapLine middleEnemyLine = new MapLine(middlePoint, enemyBasePoint, LaneType.MIDDLE, true);

    public static MapLine bottomFriendLine = new MapLine(friendBasePoint, bottomPoint, LaneType.BOTTOM, false);
    public static MapLine bottomEnemyLine = new MapLine(bottomPoint, enemyBasePoint, LaneType.BOTTOM, true);

    public static MapLine artifactTopLine = new MapLine(topPoint, middlePoint, null);
    public static MapLine artifactBottomLine = new MapLine(middlePoint, bottomPoint, null);

    static {
        topFriendLine.getStartLines().addAll(Arrays.asList(middleFriendLine, bottomFriendLine));
        topFriendLine.getEndLines().addAll(Arrays.asList(topEnemyLine, artifactTopLine));

        topEnemyLine.getStartLines().addAll(Arrays.asList(topFriendLine, artifactTopLine));
        topEnemyLine.getEndLines().addAll(Arrays.asList(middleEnemyLine, bottomEnemyLine));

        middleFriendLine.getStartLines().addAll(Arrays.asList(topFriendLine, bottomFriendLine));
        middleFriendLine.getEndLines().addAll(Arrays.asList(topEnemyLine, artifactBottomLine, artifactTopLine));

        middleEnemyLine.getStartLines().addAll(Arrays.asList(topFriendLine, artifactBottomLine, artifactTopLine));
        middleEnemyLine.getEndLines().addAll(Arrays.asList(topEnemyLine, bottomEnemyLine));

        bottomFriendLine.getStartLines().addAll(Arrays.asList(topFriendLine, middleFriendLine));
        bottomFriendLine.getEndLines().addAll(Arrays.asList(artifactBottomLine, bottomEnemyLine));

        bottomEnemyLine.getStartLines().addAll(Arrays.asList(artifactBottomLine, bottomFriendLine));
        bottomEnemyLine.getEndLines().addAll(Arrays.asList(topEnemyLine, middleEnemyLine));

        artifactTopLine.getStartLines().addAll(Arrays.asList(topFriendLine, topEnemyLine));
        artifactTopLine.getEndLines().addAll(Arrays.asList(middleFriendLine, middleEnemyLine, artifactBottomLine));

        artifactBottomLine.getStartLines().addAll(Arrays.asList(middleFriendLine, middleEnemyLine, artifactTopLine));
        artifactBottomLine.getEndLines().addAll(Arrays.asList(bottomFriendLine, bottomEnemyLine));

        mapLines.addAll(Arrays.asList(
                topFriendLine, topEnemyLine, middleFriendLine, middleEnemyLine, bottomFriendLine, bottomEnemyLine, artifactTopLine, artifactBottomLine
        ));

        mapPoints.addAll(Arrays.asList(
                friendBasePoint, topPoint, middlePoint, bottomPoint, enemyBasePoint
        ));
    }

    public static final double LINE_RESOLVING_POSITION = 100D;
    public static final double LINE_RESOLVING_DISTANCE = 200D;

    public static final double DEAD_TOWER_HP_FACTOR = 0.1D;

    public static Map<Point2D, Building> deadGuardTowers = new HashMap<>();

    public Wizard wizard;
    public FindHelper findHelper;

    public MapHelper(World world, Game game, Wizard wizard) {
        this.wizard = wizard;
        this.findHelper = new FindHelper(world, game, wizard);

        updateLineInfo();
    }

    private void updateLineInfo() {
        clearLinesInfo();

        updateTowerInfo();
        updateWizardPositions();
        updateMinionPositions();
        updateStatuses();
    }

    private void clearLinesInfo() {
        mapLines.forEach(mapLine -> {
            mapLine.getEnemyWizardPositions().clear();
            mapLine.getFriendWizardPositions().clear();

            mapLine.setEnemyPosition(-1);
            mapLine.setFriendPosition(-1);

            mapLine.setDeadEnemyTowerCount(0);
            mapLine.setDeadFriendTowerCount(0);
        });
    }

    private void updateStatuses() {
        mapLines.forEach(line -> {
            if (line.getEnemyPosition() > 0) {
                line.setMapLineStatus(MapLineStatus.YELLOW);
                return;
            }

            if (line.getEnemyWizardPositions().size() > 0) {
                line.setMapLineStatus(MapLineStatus.RED);
                return;
            }

            line.setMapLineStatus(MapLineStatus.GREEN);
        });
    }

    private void updateTowerInfo() {
        List<Building> allBuldings = findHelper.getAllBuldings(false);

        allBuldings.stream()
                .filter(tower -> tower.getType().equals(BuildingType.GUARDIAN_TOWER))
                .filter(tower -> tower.getLife() < tower.getMaxLife() * DEAD_TOWER_HP_FACTOR)
                .forEach(tower -> {
                    Point2D towerPoint = new Point2D(tower.getX(), tower.getY());

                    deadGuardTowers.put(towerPoint, tower);
                });

        deadGuardTowers.forEach((point2D, tower) -> {
            List<LinePosition> linePositions = getLinePositions(tower).stream()
                    .filter(linePosition -> linePosition.getMapLine().getLaneType() != null)
                    .collect(Collectors.toList());

            if (linePositions.size() != 1)
                throw new RuntimeException("error in function getLinePositions, guard tower exist in two lines");

            MapLine towerLine = linePositions.get(0).getMapLine();

            if (findHelper.isEnemy(wizard.getFaction(), tower))
                towerLine.setDeadEnemyTowerCount(towerLine.getDeadEnemyTowerCount() + 1);
            else
                towerLine.setDeadFriendTowerCount(towerLine.getDeadFriendTowerCount() + 1);
        });
    }

    private void updateWizardPositions() {
        List<Wizard> allWizards = findHelper.getAllWizards(false, false);

        allWizards.forEach(someWizard -> {
            List<LinePosition> linePositions = getLinePositions(someWizard);

            linePositions.forEach(linePosition -> {
                MapLine wizardLine = linePosition.getMapLine();
                Point2D wizardPoint = new Point2D(someWizard.getX(), someWizard.getY());

                if (findHelper.isEnemy(wizard.getFaction(), someWizard))
                    wizardLine.getEnemyWizardPositions().put(wizardPoint, linePosition.getPosition());
                else
                    wizardLine.getFriendWizardPositions().put(wizardPoint, linePosition.getPosition());
            });
        });
    }

    private void updateMinionPositions() {
        clearMinionPosition();
        List<Minion> allMinions = findHelper.getAllMinions(false, false);

        allMinions.stream()
                .filter(minion -> minion.getType().equals(MinionType.FETISH_BLOWDART))
                .forEach(minion -> {
                    List<LinePosition> linePositions = getLinePositions(minion);

                    linePositions.forEach(linePosition -> {
                        MapLine minionLine = linePosition.getMapLine();
                        if (findHelper.isEnemy(minion.getFaction(), minion)) {
                            if (minionLine.getFriendPosition() > linePosition.getPosition())
                                minionLine.setEnemyPosition(linePosition.getPosition());
                        } else {
                            if (minionLine.getFriendPosition() < linePosition.getPosition())
                                minionLine.setFriendPosition(linePosition.getPosition());
                        }
                    });
                });
    }

    private void clearMinionPosition() {
        mapLines.forEach(mapLine -> {
            mapLine.setEnemyPosition(0);
            mapLine.setEnemyPosition(0);
        });
    }

    public List<LinePosition> getLinePositions(double x, double y) {
        Point2D searchingPoint = new Point2D(x, y);

        return mapLines.stream()
                .map(line -> {
                    Pair<Double, Double> distance = getDistanceFromLine(searchingPoint, line);

                    double distanceFromLine = distance.getFirst();
                    double linePosition = distance.getSecond();

                    Optional<LinePosition> resultLine;
                    if (distanceFromLine <= LINE_RESOLVING_DISTANCE
                            && linePosition >= -LINE_RESOLVING_POSITION && linePosition <= (line.getLineLength() + LINE_RESOLVING_POSITION)) {
                        resultLine = Optional.of(new LinePosition(line, linePosition));
                    } else
                        resultLine = Optional.empty();

                    return resultLine;
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<LinePosition> getLinePositions(LivingUnit unit) {
        return getLinePositions(unit.getX(), unit.getY());
    }

    public List<LinePosition> getWizardLinePosition(List<LinePosition> wizardPositions, LaneType laneType) {
        return wizardPositions.stream()
                .filter(linePosition -> linePosition.getMapLine().getLaneType() != null && linePosition.getMapLine().getLaneType().equals(laneType))
                .sorted(Comparator.comparing(value -> value.getMapLine().getEnemy()))
                .collect(Collectors.toList());
    }

    public Point2D getPointInLine(LinePosition linePosition) {
        return getPointInLine(linePosition.getMapLine(), linePosition.getPosition());
    }

    public Point2D getPointInLine(MapLine line, double nextLineDistance) {
        double newX = line.getStartPoint().getX() + cos(line.getAngle()) * nextLineDistance;
        double newY = line.getStartPoint().getY() + sin(-line.getAngle()) * nextLineDistance;

        return new Point2D(newX, newY);
    }

    public Point2D getNearestPointInLine(LivingUnit unit, boolean safeWay) {
        return getNearestPointInLine(new Point2D(unit.getX(), unit.getY()), safeWay);
    }

    public Point2D getNearestPointInLine(Point2D point, boolean safeWay) {
        Point2D nearestPoint = mapPoints.stream()
                .min(Comparator.comparing(pointInLine -> pointInLine.getDistanceTo(point)))
                .get();

        //TODO fix safe calculating
        List<MapLine> nearestLines = mapLines.stream()
                .filter(line -> line.getStartPoint().equals(nearestPoint) || line.getEndPoint().equals(nearestPoint))
                .collect(Collectors.toList());

        if (safeWay) {
            List<MapLine> safeLines = mapLines.stream()
                    .filter(line -> line.getMapLineStatus() == MapLineStatus.GREEN)
                    .collect(Collectors.toList());

            if (!safeLines.isEmpty())
                nearestLines = safeLines;
        }

        MapLine mapLine = nearestLines.stream()
                .min(Comparator.comparing(line -> ((Double) getDistanceFromLine(point, line).getFirst())))
                .get();

        return getPointInLine(mapLine, getDistanceFromLine(point, mapLine).getSecond());
    }

    private Pair<Double, Double> getDistanceFromLine(Point2D point, MapLine line) {
        double angleToPoint = getAngleTo(line, point);
        double distanceToPoint = point.getDistanceTo(line.getStartPoint());

        double distanceFromLine = abs(sin(angleToPoint) * distanceToPoint);
        double linePosition = cos(angleToPoint) * distanceToPoint;

        return new Pair<>(distanceFromLine, linePosition);
    }

    private double getAngleTo(MapLine mapLine, Point2D point) {
        double deltaX = point.getX() - mapLine.getStartPoint().getX();
        double deltaY = point.getY() - mapLine.getStartPoint().getY();

        double absoluteAngleTo;
        if (deltaX == 0) {
            if (deltaY > 0) absoluteAngleTo = -PI / 2;
            else absoluteAngleTo = PI / 2;
        } else
            absoluteAngleTo = atan(deltaY / deltaX);

        return abs(absoluteAngleTo - mapLine.getAngle());
    }
}
