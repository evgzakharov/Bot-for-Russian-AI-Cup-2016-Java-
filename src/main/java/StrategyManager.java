import model.*;

import java.util.HashMap;
import java.util.Map;

public class StrategyManager {

    private Wizard self;
    private World world;
    private Game game;
    private Move move;

    private LaneType laneType;
    private ActionMode actionMode;

    private Map<ActionMode, ActionManager> gameManagers;

    public void nextTick(Wizard self, World world, Game game, Move move) {
        this.self = self;
        this.world = world;
        this.game = game;
        this.move = move;

        initializeDefault();

        makeDecision();
    }

    private void makeDecision() {
        ActionManager actionManager = gameManagers.get(actionMode);

        actionManager.init(self, world, game, move, this);

        actionMode = actionManager.move();
    }

    private void initializeDefault() {
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

        if (actionMode == null)
            actionMode = ActionMode.ATTACK;

        if (gameManagers == null)
            gameManagers = new HashMap<>();

        gameManagers.put(ActionMode.ATTACK, new AttackActionManager());
    }

    public LaneType getLaneType() {
        return laneType;
    }

    public ActionMode getActionMode() {
        return actionMode;
    }
}
