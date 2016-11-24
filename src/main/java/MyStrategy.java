import model.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.StrictMath.*;

public final class MyStrategy implements Strategy {
    private static final double WAYPOINT_RADIUS = 100.0D;

    private static final double LOW_HP_FACTOR = 0.25D;
    private static final double LOW_BUIDING_FACTOR = 0.1D;
    private static final double LOW_MINION_FACTOR = 0.35D;

    private static final double MIN_DISTANCE_TO_ENEMY = 50D;

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

            if (nearestTarget.isPresent()) {
                shootToTarget(self, game, move, nearestTarget.get(), false);
                return;
            }
        } else {
            if (nearestTarget.isPresent()) {
                shootToTarget(self, game, move, nearestTarget.get(), true);
                return;
            }
        }

        // shoot forest, if can

        goTo(getNextWaypoint());
    }

    private void shootToTarget(Wizard self, Game game, Move move, LivingUnit nearestTarget, boolean withTurn) {
        double distance = self.getDistanceTo(nearestTarget);

        // ... и он в пределах досягаемости наших заклинаний, ...
        double angle = self.getAngleTo(nearestTarget);

        // ... то поворачиваемся к цели.
        if (withTurn)
            move.setTurn(angle);

        // Если цель перед нами, ...
        if (abs(angle) < game.getStaffSector() / 2.0D) {
            // ... то атакуем.
            move.setAction(ActionType.MAGIC_MISSILE);
            move.setCastAngle(angle);
            move.setMinCastDistance(distance - nearestTarget.getRadius() + game.getMagicMissileRadius());
        }
    }

    /**
     * Инциализируем стратегию.
     * <p>
     * Для этих целей обычно можно использовать конструктор, однако в данном случае мы хотим инициализировать генератор
     * случайных чисел значением, полученным от симулятора игры.
     */
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

            // Наша стратегия исходит из предположения, что заданные нами ключевые точки упорядочены по убыванию
            // дальности до последней ключевой точки. Сейчас проверка этого факта отключена, однако вы можете
            // написать свою проверку, если решите изменить координаты ключевых точек.

            /*Point2D lastWaypoint = waypoints[waypoints.length - 1];

            Preconditions.checkState(ArrayUtils.isSorted(waypoints, (waypointA, waypointB) -> Double.compare(
                    waypointB.getDistanceTo(lastWaypoint), waypointA.getDistanceTo(lastWaypoint)
            )));*/
        }
    }

    /**
     * Сохраняем все входные данные в полях класса для упрощения доступа к ним.
     */
    private void initializeTick(Wizard self, World world, Game game, Move move) {
        this.self = self;
        this.world = world;
        this.game = game;
        this.move = move;
    }

    /**
     * Данный метод предполагает, что все ключевые точки на линии упорядочены по уменьшению дистанции до последней
     * ключевой точки. Перебирая их по порядку, находим первую попавшуюся точку, которая находится ближе к последней
     * точке на линии, чем волшебник. Это и будет следующей ключевой точкой.
     * <p>
     * Дополнительно проверяем, не находится ли волшебник достаточно близко к какой-либо из ключевых точек. Если это
     * так, то мы сразу возвращаем следующую ключевую точку.
     */
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

    /**
     * Действие данного метода абсолютно идентично действию метода {@code getNextWaypoint}, если перевернуть массив
     * {@code waypoints}.
     */
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

    /**
     * Простейший способ перемещения волшебника.
     */
    private void goTo(Point2D point) {
        double angle = self.getAngleTo(point.getX(), point.getY());
        move.setTurn(angle);

        if (abs(angle) < game.getStaffSector() / 4.0D) {
            move.setSpeed(game.getWizardForwardSpeed());
        }

        Optional<LivingUnit> nearestUnit = getAllUnits(true).stream()
                .min(Comparator.comparingDouble(self::getDistanceTo));

        if (nearestUnit.isPresent() &&
                (self.getDistanceTo(nearestUnit.get()) - self.getRadius() - nearestUnit.get().getRadius()) <= MIN_CLOSEST_DISTANCE) {
            double angleToUnit = self.getAngleTo(nearestUnit.get());

            move.setStrafeSpeed((angleToUnit >= 0) ? game.getWizardStrafeSpeed() : -game.getWizardStrafeSpeed());
            move.setTurn(-angleToUnit);
            move.setSpeed(game.getWizardForwardSpeed());
        }
    }

    private void goWithoutTurn(Point2D point) {
        double diffAngle = self.getAngleTo(point.getX(), point.getY());

        double backCoef = cos(diffAngle);
        double strickCoef = sin(diffAngle);

        if (abs(diffAngle) > PI / 2) {
            move.setSpeed(game.getWizardBackwardSpeed() * backCoef);
        } else {
            move.setSpeed(game.getWizardForwardSpeed() * backCoef);
        }
        move.setStrafeSpeed(game.getWizardStrafeSpeed() * strickCoef);
    }

    /**
     * Находим ближайшую цель для атаки, независимо от её типа и других характеристик.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private Boolean isNeedToMoveBack() {
        List<LivingUnit> units = getAllUnits(false);

        Optional<LivingUnit> nearestEnemy = units.stream()
                .filter(this::isEnemy)
                .min(Comparator.comparingDouble(self::getDistanceTo));

        if (!nearestEnemy.isPresent()) return false;

        LivingUnit nearestFrendToEnemy = units.stream()
                .filter(unit -> self.getFaction() == unit.getFaction() && !unit.equals(self))
                .filter(unit -> unit.getLife() > unit.getMaxLife() * getLowHpFactorToUnit(unit))
                .min(Comparator.comparingDouble(nearestEnemy.get()::getDistanceTo))
                .get();


        double distanceToEnemy = self.getDistanceTo(nearestEnemy.get());
        double distanceFromEnemyToFriend = nearestFrendToEnemy.getDistanceTo(nearestEnemy.get());
        return distanceToEnemy <= distanceFromEnemyToFriend || distanceToEnemy < MIN_DISTANCE_TO_ENEMY;
    }

    private List<LivingUnit> getAllUnits(boolean withTrees) {
        List<LivingUnit> units = new ArrayList<>();

        List<Wizard> wizards = Arrays.stream(world.getWizards())
                .filter(wizard -> !wizard.isMe())
                .collect(Collectors.toList());

        units.addAll(wizards);
        units.addAll(Arrays.asList(world.getBuildings()));
        units.addAll(Arrays.asList(world.getMinions()));

        if (withTrees)
            units.addAll(Arrays.asList(world.getTrees()));

        return units;
    }

    private boolean isEnemy(LivingUnit unit) {
        return self.getFaction() != unit.getFaction() && unit.getFaction() != Faction.NEUTRAL;
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
            if (!isEnemy(target)) {
                continue;
            }

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
    private static final class Point2D {
        private final double x;
        private final double y;

        private Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getDistanceTo(double x, double y) {
            return StrictMath.hypot(this.x - x, this.y - y);
        }

        public double getDistanceTo(Point2D point) {
            return getDistanceTo(point.x, point.y);
        }

        public double getDistanceTo(Unit unit) {
            return getDistanceTo(unit.getX(), unit.getY());
        }
    }
}