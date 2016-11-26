import model.*;

import java.util.List;
import java.util.Optional;


public class AttackGameManager extends GameManager {
    private LaneType laneType;

    @Override
    public void initialize() {
        if (laneType == null) {
            switch ((int) self.getId()) {
                case 1:
                case 2:
                case 6:
                case 7:
                    laneType = LaneType.TOP;
                    break;
                case 3:
                case 8:
                    laneType = LaneType.MIDDLE;
                    break;
                case 4:
                case 5:
                case 9:
                case 10:
                    laneType = LaneType.BOTTOM;
                    break;
                default:
            }
        }
    }

    @Override
    public GameMode move() {
        if (self.getLife() < self.getMaxLife() * LOW_HP_FACTOR) {
            moveHelper.goTo(getPreviousWaypoint());
            return GameMode.ATTACK;
        }

        Optional<LivingUnit> nearestTarget = findHelper.getNearestEnemy();
        if (isNeedToMoveBack()) {
            moveHelper.goWithoutTurn(getPreviousWaypoint());

            nearestTarget.ifPresent(livingUnit -> shootHelder.shootToTarget(livingUnit));

            return GameMode.ATTACK;
        } else {
            moveHelper.goWithoutTurn(getNextWaypoint());

            if (nearestTarget.isPresent()) {
                shootHelder.shootToTarget(nearestTarget.get());
                return GameMode.ATTACK;
            }
        }

        moveHelper.goTo(getNextWaypoint());

        Optional<Tree> nearestTree = findHelper.getAllTrees()
                .filter(tree -> self.getAngleTo(tree) < game.getStaffSector())
                .filter(tree -> self.getDistanceTo(tree) < self.getRadius() + tree.getRadius() + MIN_CLOSEST_DISTANCE)
                .findAny();

        nearestTree.ifPresent(tree -> move.setAction(ActionType.STAFF));

        return GameMode.ATTACK;
    }

    private Point2D getNextWaypoint() {
        //TODO;
        return null;
    }

    private Point2D getPreviousWaypoint() {
        return null;
    }

    private boolean isNeedToMoveBack() {
        boolean minionsCondition = minionConditions();
        if (minionsCondition) return true;

        List<Wizard> enemyWizards = findHelper.getAllWizards(true, true);

        boolean multiEnemiesCondition = multiEnemiesCondition(enemyWizards);
        if (multiEnemiesCondition) return true;

        boolean singleEnemyCondition = singleEnemyCondition(enemyWizards);
        if (singleEnemyCondition) return true;

        boolean buldingCondition = buldingCondition();
        if (buldingCondition) return true;

        return false;
    }

    @Override
    public GameMode getMode() {
        return GameMode.ATTACK;
    }
}
