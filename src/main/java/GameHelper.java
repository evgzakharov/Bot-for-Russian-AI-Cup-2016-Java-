import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.StrictMath.abs;

public class GameHelper {

    private Wizard wizard;
    private World world;
    private Game game;

    public GameHelper(World world, Game game, Wizard wizard) {
        this.world = world;
        this.game = game;
        this.wizard = wizard;
    }

    public List<LivingUnit> getAllUnits(boolean withTrees, boolean onlyEnemy, boolean onlyNearest) {
        List<LivingUnit> units = new ArrayList<>();

        units.addAll(getAllWizards(onlyEnemy, onlyNearest));
        units.addAll(getAllBuldings(onlyEnemy));
        units.addAll(getAllMinions(onlyEnemy, onlyNearest));

        if (withTrees)
            units.addAll(Arrays.asList(world.getTrees()));

        return units;
    }

    public List<LivingUnit> getAllMovingUnits(boolean onlyEnemy, boolean onlyNearest) {
        List<LivingUnit> units = new ArrayList<>();

        units.addAll(getAllWizards(onlyEnemy, onlyNearest));
        units.addAll(getAllMinions(onlyEnemy, onlyNearest));

        return units;
    }

    public List<Wizard> getAllWizards(boolean onlyEnemy, boolean onlyNearest) {
        return Arrays.stream(world.getWizards())
                .filter(wizard -> !wizard.isMe())
                .filter(filterLivingUnits(onlyEnemy, onlyNearest))
                .collect(Collectors.toList());
    }

    private Predicate<LivingUnit> filterLivingUnits(boolean onlyEnemy, boolean onlyNearest) {
        return unit -> (!onlyEnemy || isEnemy(wizard.getFaction(), unit))
                && (!onlyNearest || abs(unit.getX() - wizard.getX()) < game.getWizardCastRange() * 3)
                && (!onlyNearest || abs(unit.getY() - wizard.getY()) < game.getWizardCastRange() * 3);

    }

    public List<Building> getAllBuldings(boolean onlyEnemy) {
        return Arrays.stream(world.getBuildings())
                .filter(filterLivingUnits(onlyEnemy, false))
                .collect(Collectors.toList());
    }

    public List<Minion> getAllMinions(boolean onlyEnemy, boolean onlyNearest) {
        return Arrays.stream(world.getMinions())
                .filter(filterLivingUnits(onlyEnemy, onlyNearest))
                .collect(Collectors.toList());
    }

    public Stream<Tree> getAllTrees() {
        return Arrays.stream(world.getTrees())
                .filter(filterLivingUnits(false, true));
    }


    public boolean isEnemy(Faction self, LivingUnit unit) {
        return self != unit.getFaction() && unit.getFaction() != Faction.NEUTRAL;
    }
}
