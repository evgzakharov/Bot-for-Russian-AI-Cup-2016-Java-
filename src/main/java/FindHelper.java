import model.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.StrictMath.abs;

public class FindHelper {

    private Wizard wizard;
    private World world;
    private Game game;

    public FindHelper(World world, Game game, Wizard wizard) {
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

    public Optional<LivingUnit> getNearestEnemy() {
        Optional<LivingUnit> nearestWizard = getNearestTarget(world.getWizards());

        if (nearestWizard.isPresent()) return nearestWizard;

        Optional<LivingUnit> nearestBuilding = getNearestTarget(world.getBuildings());

        if (nearestBuilding.isPresent()) return nearestBuilding;

        else return getNearestTarget(world.getMinions());
    }

    public Optional<LivingUnit> getNearestTarget(LivingUnit[] targets) {
        List<LivingUnit> nearestTargets = new ArrayList<>();

        for (LivingUnit target : targets) {
            if (!isEnemy(wizard.getFaction(), target)) {
                continue;
            }

            if (abs(wizard.getX() - target.getX()) > game.getWizardCastRange() * 2) continue;
            if (abs(wizard.getY() - target.getY()) > game.getWizardCastRange() * 2) continue;

            double distance = wizard.getDistanceTo(target);

            if (distance < wizard.getCastRange()) {
                nearestTargets.add(target);
            }
        }

        return nearestTargets
                .stream()
                .min(Comparator.comparingInt(LivingUnit::getLife));
    }
}
