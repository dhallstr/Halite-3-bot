package dhallstr;

import hlt.*;

public class TerrainGoal extends Goal {

    //PlayerId me;
    int neededHalite;
    int turns;

    public int getMaxTurns() {
        return turns;
    }

    public TerrainGoal(int halite, int turns) {
        neededHalite = halite;
        this.turns = turns;
        //this.me = me.owner;
    }

    @Override
    public int rateTile(Game game, MapCell cell, Ship s, PlannedLocations plan) {
        int totalHalite = s.halite - cell.cost + Math.max(plan.getProjectedHalite(game.gameMap, cell.position, cell.dist) - Magic.getCollectDownTo(game.gameMap), 0);
        if (totalHalite > Constants.MAX_HALITE) {
            totalHalite = Constants.MAX_HALITE - (totalHalite - Constants.MAX_HALITE);
        }

        int turns = cell.actualDist + getNumberStays(s, cell, plan, game.gameMap) + game.gameMap.calculateDistanceToDropoff(game.players.get(s.owner.id), cell.position);
        return (totalHalite - s.halite) / (turns == 0 ? 1 : turns);
    }

    @Override
    public int getNumberStays(Ship s, MapCell cell, PlannedLocations plan, GameMap map) {
        int halite = plan.getProjectedHalite(map, cell.position, cell.dist);
        int turnsStayed;
        int minedTotal = 0;
        for (turnsStayed = 0; ; turnsStayed++) {
            int mined = Math.min(cell.collectAmount(halite), Constants.MAX_HALITE - s.halite + cell.cost - minedTotal);
            halite -= mined;
            minedTotal += mined;
            if (mined < Magic.getCollectDownTo(map)) break;
        }
        return turnsStayed;
    }

    public Intent getIntent() {
        return Intent.GATHER;
    }

    public boolean meetsGoal(MapCell cell) {
        return (cell.halite >= neededHalite);
    }
}
