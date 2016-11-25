import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GameHelper {

    private World world;
    private Game game;

    public GameHelper(World world, Game game) {
        this.world = world;
        this.game = game;
    }

    public List<LivingUnit> getAllUnits(boolean withTrees) {
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

    public boolean isEnemy(Faction self, LivingUnit unit) {
        return self != unit.getFaction() && unit.getFaction() != Faction.NEUTRAL;
    }
}
