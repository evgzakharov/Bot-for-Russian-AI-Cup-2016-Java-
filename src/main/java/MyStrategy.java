import model.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.StrictMath.*;

public final class MyStrategy implements Strategy {
    private static final double WAYPOINT_RADIUS = 100.0D;

    private static final double LOW_HP_FACTOR = 0.25D;
    private static final double LOW_BUIDING_FACTOR = 0.1D;
    private static final double LOW_MINION_FACTOR = 0.35D;

    private static final double MIN_CLOSEST_DISTANCE = 5D;

    /**
     * Ключевые точки для каждой линии, позволяющие упростить управление перемещением волшебника.
     * <p>
     * Если всё хорошо, двигаемся к следующей точке и атакуем противников.
     * Если осталось мало жизненной энергии, отступаем к предыдущей точке.
     */
    private final Map<LaneType, Point2D[]> waypointsByLane = new EnumMap<>(LaneType.class);

    private Random random;

    private LaneType lane;
    private Point2D[] waypoints;

    private Wizard self;
    private World world;
    private Game game;
    private Move move;
    private GameHelper gameHelper;


    /**
     * Основной метод стратегии, осуществляющий управление волшебником.
     * Вызывается каждый тик для каждого волшебника.
     *
     * @param self  Волшебник, которым данный метод будет осуществлять управление.
     * @param world Текущее состояние мира.
     * @param game  Различные игровые константы.
     * @param move  Результатом работы метода является изменение полей данного объекта.
     */
    @Override
    public void move(Wizard self, World world, Game game, Move move) {
        initializeStrategy(self, game);
        initializeTick(self, world, game, move);

        if (self.getLife() < self.getMaxLife() * LOW_HP_FACTOR) {
            goTo(getPreviousWaypoint());
            return;
        }

        Optional<LivingUnit> nearestTarget = getNearestEnemy();
        if (isNeedToMoveBack()) {
            goWithoutTurn(getPreviousWaypoint());

            if (nearestTarget.isPresent())
                shootToTarget(self, game, move, nearestTarget.get(), true);

            return;
        } else {
            goWithoutTurn(getNextWaypoint());

            if (nearestTarget.isPresent()) {
                shootToTarget(self, game, move, nearestTarget.get(), true);
                return;
            }
        }

        goTo(getNextWaypoint());

        Optional<Tree> nearestTree = gameHelper.getAllTrees()
                .filter(tree -> self.getAngleTo(tree) < game.getStaffSector())
                .filter(tree -> self.getDistanceTo(tree) < self.getRadius() + tree.getRadius() + MIN_CLOSEST_DISTANCE)
                .findAny();

        if (nearestTree.isPresent())
            move.setAction(ActionType.STAFF);
    }

    private void shootToTarget(Wizard self, Game game, Move move, LivingUnit nearestTarget, boolean withTurn) {
        double distance = self.getDistanceTo(nearestTarget);

        double angle = self.getAngleTo(nearestTarget);

        if (withTurn)
            move.setTurn(angle);

        if (distance > self.getCastRange()) return;

        if (abs(angle) < game.getStaffSector() / 2.0D) {
            move.setAction(ActionType.MAGIC_MISSILE);
            move.setCastAngle(angle);
            move.setMinCastDistance(distance - nearestTarget.getRadius() + game.getMagicMissileRadius());
        }
    }

    private void initializeStrategy(Wizard self, Game game) {
        if (random == null) {
            random = new Random(game.getRandomSeed());

            double mapSize = game.getMapSize();

            waypointsByLane.put(LaneType.MIDDLE, new Point2D[]{
                    new Point2D(100.0D, mapSize - 100.0D),
                    random.nextBoolean()
                            ? new Point2D(600.0D, mapSize - 200.0D)
                            : new Point2D(200.0D, mapSize - 600.0D),
                    new Point2D(800.0D, mapSize - 800.0D),
                    new Point2D(mapSize - 600.0D, 600.0D)
            });

            waypointsByLane.put(LaneType.TOP, new Point2D[]{
                    new Point2D(100.0D, mapSize - 100.0D),
                    new Point2D(100.0D, mapSize - 400.0D),
                    new Point2D(200.0D, mapSize - 800.0D),
                    new Point2D(200.0D, mapSize * 0.75D),
                    new Point2D(200.0D, mapSize * 0.5D),
                    new Point2D(200.0D, mapSize * 0.25D),
                    new Point2D(200.0D, 200.0D),
                    new Point2D(mapSize * 0.25D, 200.0D),
                    new Point2D(mapSize * 0.5D, 200.0D),
                    new Point2D(mapSize * 0.75D, 200.0D),
                    new Point2D(mapSize - 200.0D, 200.0D)
            });

            waypointsByLane.put(LaneType.BOTTOM, new Point2D[]{
                    new Point2D(100.0D, mapSize - 100.0D),
                    new Point2D(400.0D, mapSize - 100.0D),
                    new Point2D(800.0D, mapSize - 200.0D),
                    new Point2D(mapSize * 0.25D, mapSize - 200.0D),
                    new Point2D(mapSize * 0.5D, mapSize - 200.0D),
                    new Point2D(mapSize * 0.75D, mapSize - 200.0D),
                    new Point2D(mapSize - 200.0D, mapSize - 200.0D),
                    new Point2D(mapSize - 200.0D, mapSize * 0.75D),
                    new Point2D(mapSize - 200.0D, mapSize * 0.5D),
                    new Point2D(mapSize - 200.0D, mapSize * 0.25D),
                    new Point2D(mapSize - 200.0D, 200.0D)
            });

            switch ((int) self.getId()) {
                case 1:
                case 2:
                case 6:
                case 7:
                    lane = LaneType.TOP;
                    break;
                case 3:
                case 8:
                    lane = LaneType.MIDDLE;
                    break;
                case 4:
                case 5:
                case 9:
                case 10:
                    lane = LaneType.BOTTOM;
                    break;
                default:
            }

            waypoints = waypointsByLane.get(lane);
        }
    }

    private void initializeTick(Wizard self, World world, Game game, Move move) {
        this.self = self;
        this.world = world;
        this.game = game;
        this.move = move;
        this.gameHelper = new GameHelper(world, game, self);
    }

    private Point2D getNextWaypoint() {
        int lastWaypointIndex = waypoints.length - 1;
        Point2D lastWaypoint = waypoints[lastWaypointIndex];

        for (int waypointIndex = 0; waypointIndex < lastWaypointIndex; ++waypointIndex) {
            Point2D waypoint = waypoints[waypointIndex];

            if (waypoint.getDistanceTo(self) <= WAYPOINT_RADIUS) {
                return waypoints[waypointIndex + 1];
            }

            if (lastWaypoint.getDistanceTo(waypoint) < lastWaypoint.getDistanceTo(self)) {
                return waypoint;
            }
        }

        return lastWaypoint;
    }

    private Point2D getPreviousWaypoint() {
        Point2D firstWaypoint = waypoints[0];

        for (int waypointIndex = waypoints.length - 1; waypointIndex > 0; --waypointIndex) {
            Point2D waypoint = waypoints[waypointIndex];

            if (waypoint.getDistanceTo(self) <= WAYPOINT_RADIUS) {
                return waypoints[waypointIndex - 1];
            }

            if (firstWaypoint.getDistanceTo(waypoint) < firstWaypoint.getDistanceTo(self)) {
                return waypoint;
            }
        }

        return firstWaypoint;
    }

    private void goTo(Point2D point) {
        Point2D correctedPoint = correctPoint(point);

        double angle = self.getAngleTo(correctedPoint.getX(), correctedPoint.getY());
        move.setTurn(angle);

        goWithoutTurn(point);
    }

    private void goWithoutTurn(Point2D point) {
        Point2D correctedPoint = correctPoint(point);

        double diffAngle = self.getAngleTo(correctedPoint.getX(), correctedPoint.getY());

        double backCoef = cos(diffAngle);
        double strickCoef = sin(diffAngle);

        if (abs(diffAngle) > PI / 2) {
            move.setSpeed(game.getWizardBackwardSpeed() * backCoef);
        } else {
            move.setSpeed(game.getWizardForwardSpeed() * backCoef);
        }
        move.setStrafeSpeed(game.getWizardStrafeSpeed() * strickCoef);
    }

    private Point2D correctPoint(Point2D point2D) {
        WayFinder wayFinder = new WayFinder(self, world, game);
        List<Point2D> way = wayFinder.findWay(point2D);

        if (way != null && way.size() > 0) {
            return way.get(0);
        }

        return point2D;
    }

    /**
     * Находим ближайшую цель для атаки, независимо от её типа и других характеристик.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private Boolean isNeedToMoveBack() {

        long toCloseMinions = gameHelper.getAllMinions(true, true).stream()
                .filter(minion -> {
                    if (minion.getType() == MinionType.FETISH_BLOWDART)
                        return self.getDistanceTo(minion) <= game.getFetishBlowdartAttackRange() * 1.1;
                    else if (minion.getType() == MinionType.ORC_WOODCUTTER)
                        return self.getDistanceTo(minion) <= game.getOrcWoodcutterAttackRange() * 3;

                    return false;
                }).count();

        boolean minionsCondition = toCloseMinions > 0;

        if (minionsCondition) return true;

        List<Wizard> enemyWizards = gameHelper.getAllWizards(true, true);
        List<Wizard> enemiesLookingToMe = enemyWizards.stream()
                .filter(unit -> {
                    double distanceTo = self.getDistanceTo(unit);
                    return (distanceTo < game.getWizardCastRange() * 1.1 && abs(unit.getAngleTo(self)) <= game.getStaffSector() * 1.2);
                })
                .collect(Collectors.toList());

        boolean multiEnemiesCondition = false;
        if (enemiesLookingToMe.size() > 1) {
            Wizard enemyWithBiggestHP = enemiesLookingToMe.stream()
                    .max(Comparator.comparingInt(Wizard::getLife)).get();

            boolean hpIsLow = self.getLife() < self.getMaxLife() * (LOW_HP_FACTOR * 3)
                    && self.getLife() * (1 - LOW_HP_FACTOR / 2) < enemyWithBiggestHP.getLife();

            if (hpIsLow)
                multiEnemiesCondition = true;
        }

        if (multiEnemiesCondition)
            return true;

        Optional<Wizard> enemyWithSmallestHP = enemyWizards.stream()
                .filter(unit -> self.getDistanceTo(unit) < game.getWizardCastRange())
                .min(Comparator.comparingInt(Wizard::getLife));

        boolean singleEnemyCondition = false;
        if (enemyWithSmallestHP.isPresent()) {
            boolean enemyIsToClose = enemyWithSmallestHP.get().getDistanceTo(self) <= game.getWizardCastRange() * 0.8;

            boolean hpIsToLow = self.getLife() < (LOW_HP_FACTOR * 2) * self.getMaxLife()
                    && self.getLife() * (1 - LOW_HP_FACTOR / 2) < enemyWithSmallestHP.get().getLife()
                    && enemyWithSmallestHP.get().getAngleTo(self) <= game.getStaffSector() * 2;

            if (enemyIsToClose || hpIsToLow)
                singleEnemyCondition = true;
        }
        if (singleEnemyCondition) return true;

        Optional<Building> nearestBuilding = gameHelper.getAllBuldings(true).stream()
                .min(Comparator.comparingDouble(self::getDistanceTo));

        boolean buldingCondition = false;
        if (nearestBuilding.isPresent()) {
            double demageRadius = 0;
            if (nearestBuilding.get().getType() == BuildingType.FACTION_BASE)
                demageRadius = game.getFactionBaseAttackRange() + MIN_CLOSEST_DISTANCE;
            if (nearestBuilding.get().getType() == BuildingType.GUARDIAN_TOWER)
                demageRadius = game.getGuardianTowerAttackRange() + MIN_CLOSEST_DISTANCE;

            double distanceToBuilding = self.getDistanceTo(nearestBuilding.get());
            if (distanceToBuilding < demageRadius) {

                Optional<LivingUnit> nearestFriendToBuilding = gameHelper.getAllMovingUnits(true, true).stream()
                        .filter(unit -> unit.getLife() / unit.getMaxLife() < self.getLife() / self.getMaxLife())
                        .min(Comparator.comparingDouble(nearestBuilding.get()::getDistanceTo));

                boolean noFriends = nearestFriendToBuilding
                        .map(livingUnit -> distanceToBuilding < livingUnit.getDistanceTo(nearestBuilding.get()))
                        .orElse(true);

                boolean buldingIsToClose = (demageRadius - distanceToBuilding) >= game.getWizardRadius() * 4;

                boolean hgIsLow = self.getLife() < (1 - LOW_HP_FACTOR) * self.getMaxLife();

                boolean buldingWillShoot = nearestBuilding.get().getRemainingActionCooldownTicks() < 75;

                if ((noFriends && hgIsLow && buldingWillShoot) || buldingIsToClose)
                    buldingCondition = true;
            }
        }

        if (buldingCondition)
            return true;

        return false;
    }

    private double getLowHpFactorToUnit(LivingUnit unit) {
        if (unit instanceof Wizard) return LOW_HP_FACTOR;
        if (unit instanceof Building) return LOW_BUIDING_FACTOR;
        if (unit instanceof Minion) return LOW_MINION_FACTOR;

        return LOW_HP_FACTOR;
    }

    private Optional<LivingUnit> getNearestEnemy() {
        Optional<LivingUnit> nearestWizard = getNearestTarget(world.getWizards());

        if (nearestWizard.isPresent()) return nearestWizard;

        Optional<LivingUnit> nearestBuilding = getNearestTarget(world.getBuildings());

        if (nearestBuilding.isPresent()) return nearestBuilding;

        else return getNearestTarget(world.getMinions());
    }

    private Optional<LivingUnit> getNearestTarget(LivingUnit[] targets) {
        List<LivingUnit> nearestTargets = new ArrayList<>();

        for (LivingUnit target : targets) {
            if (!gameHelper.isEnemy(self.getFaction(), target)) {
                continue;
            }

            if (abs(self.getX() - target.getX()) > game.getWizardCastRange() * 3) continue;
            if (abs(self.getY() - target.getY()) > game.getWizardCastRange() * 3) continue;

            double distance = self.getDistanceTo(target);

            if (distance < self.getCastRange()) {
                nearestTargets.add(target);
            }
        }

        return nearestTargets
                .stream()
                .min(Comparator.comparingInt(LivingUnit::getLife));
    }

    /**
     * Вспомогательный класс для хранения позиций на карте.
     */

}