import model.Game;
import model.Move;
import model.Wizard;
import model.World;

import java.util.HashMap;
import java.util.Map;

public final class MyStrategy implements Strategy {

    private GameMode gameMode;
    private Map<GameMode, GameManager> gameManagers;

    @Override
    public void move(Wizard self, World world, Game game, Move move) {
        initialize();

        GameManager gameManager = gameManagers.get(gameMode);

        gameManager.init(self, world, game, move);

        gameMode = gameManager.move();
    }

    private void initialize() {
        if (gameMode == null)
            gameMode = GameMode.ATTACK;

        if (gameManagers == null)
            gameManagers = new HashMap<>();

        if (gameManagers.get(gameMode) == null) {
            gameManagers.put(GameMode.ATTACK, new AttackGameManager());
        }
    }

//    private void initializeStrategy(Wizard self, Game game) {
//        if (random == null) {
//            random = new Random(game.getRandomSeed());
//
//            double mapSize = game.getMapSize();
//
//            waypointsByLane.put(LaneType.MIDDLE, new Point2D[]{
//                    new Point2D(100.0D, mapSize - 100.0D),
//                    random.nextBoolean()
//                            ? new Point2D(600.0D, mapSize - 200.0D)
//                            : new Point2D(200.0D, mapSize - 600.0D),
//                    new Point2D(800.0D, mapSize - 800.0D),
//                    new Point2D(mapSize - 600.0D, 600.0D)
//            });
//
//            waypointsByLane.put(LaneType.TOP, new Point2D[]{
//                    new Point2D(100.0D, mapSize - 100.0D),
//                    new Point2D(100.0D, mapSize - 400.0D),
//                    new Point2D(200.0D, mapSize - 800.0D),
//                    new Point2D(200.0D, mapSize * 0.75D),
//                    new Point2D(200.0D, mapSize * 0.5D),
//                    new Point2D(200.0D, mapSize * 0.25D),
//                    new Point2D(200.0D, 200.0D),
//                    new Point2D(mapSize * 0.25D, 200.0D),
//                    new Point2D(mapSize * 0.5D, 200.0D),
//                    new Point2D(mapSize * 0.75D, 200.0D),
//                    new Point2D(mapSize - 200.0D, 200.0D)
//            });
//
//            waypointsByLane.put(LaneType.BOTTOM, new Point2D[]{
//                    new Point2D(100.0D, mapSize - 100.0D),
//                    new Point2D(400.0D, mapSize - 100.0D),
//                    new Point2D(800.0D, mapSize - 200.0D),
//                    new Point2D(mapSize * 0.25D, mapSize - 200.0D),
//                    new Point2D(mapSize * 0.5D, mapSize - 200.0D),
//                    new Point2D(mapSize * 0.75D, mapSize - 200.0D),
//                    new Point2D(mapSize - 200.0D, mapSize - 200.0D),
//                    new Point2D(mapSize - 200.0D, mapSize * 0.75D),
//                    new Point2D(mapSize - 200.0D, mapSize * 0.5D),
//                    new Point2D(mapSize - 200.0D, mapSize * 0.25D),
//                    new Point2D(mapSize - 200.0D, 200.0D)
//            });
//
//            switch ((int) self.getId()) {
//                case 1:
//                case 2:
//                case 6:
//                case 7:
//                    lane = LaneType.TOP;
//                    break;
//                case 3:
//                case 8:
//                    lane = LaneType.MIDDLE;
//                    break;
//                case 4:
//                case 5:
//                case 9:
//                case 10:
//                    lane = LaneType.BOTTOM;
//                    break;
//                default:
//            }
//
//            waypoints = waypointsByLane.get(lane);
//        }
//    }

}