package dhallstr;

import hlt.*;

public class TerrainGoal extends Goal {

    PlayerId me;
    int myHalite;
    int neededHalite;
    int turns;

    Goal simple;

    public int getTurns() {
        return turns;
    }

    public Goal getSimpleGoal() {
        return simple;
    }

    public Goal setSimpleGoal(Goal g) {
        simple = g;
        return this;
    }

    public TerrainGoal(int halite, int turns, Ship me) {
        neededHalite = halite;
        this.turns = turns;
        this.me = me.owner;
        myHalite = me.halite;
        simple = null;
    }

    public boolean overrideUnsafe(MapCell cell) { return false; }

    @Override
    public int rateTile(Game game, MapCell cell, Ship s, PlannedLocations plan) {
        int totalHalite = s.halite - cell.cost + Math.max(plan.getProjectedHalite(game.gameMap, cell.position, cell.dist) - Magic.MIN_GATHER_WAIT_HALITE, 0);
        if (totalHalite > Constants.MAX_HALITE) {
            totalHalite = (2 * Constants.MAX_HALITE - totalHalite);
        }

        int turns = cell.actualDist + getNumberStays(s, cell, plan, game.gameMap) + game.gameMap.calculateDistanceToDropoff(game.players.get(s.owner.id), cell.position);
        return (totalHalite - s.halite) / turns;
    }

    @Override
    public int getNumberStays(Ship s, MapCell cell, PlannedLocations plan, GameMap map) {
        int halite = plan.getProjectedHalite(map, cell.position, cell.dist);
        int turnsStayed;
        int minedTotal = 0;
        for (turnsStayed = 0; ; turnsStayed++) {
            int mined = Math.min(cell.isInspired ? halite / Constants.INSPIRED_EXTRACT_RATIO : halite / Constants.EXTRACT_RATIO, Constants.MAX_HALITE - s.halite + cell.cost - minedTotal);
            halite -= mined;
            minedTotal += mined;
            if (mined < Magic.MIN_GATHER_WAIT_HALITE) break;
        }
        return turnsStayed;
    }

    @Override
    public int getAutoAccept() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMinScore() {
        return Integer.MIN_VALUE;
    }

    public Intent getIntent() {
        return Intent.GATHER;
    }

    public boolean meetsGoal(MapCell cell) {
        return (cell.halite >= neededHalite);
    }

    public Direction[] orderDirections(GameMap map, MapCell cell) {
        return order(map, cell, true);
    }
}
