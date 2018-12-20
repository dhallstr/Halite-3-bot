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
        int totalHalite = s.halite - cell.lost + cell.gained + Math.max((plan.getProjectedHalite(game.gameMap, cell.position, cell.actualDist) - Magic.getCollectDownTo(game.gameMap)), 0);
        if (totalHalite > Constants.MAX_HALITE) {
            totalHalite = 2 * Constants.MAX_HALITE - s.halite + cell.lost - cell.gained - Math.max((plan.getProjectedHalite(game.gameMap, cell.position, cell.actualDist) - Magic.getCollectDownTo(game.gameMap)), 0);
        }
        int numStays = getNumberStays(s, cell, plan, game.gameMap);
        /*int halite = plan.getProjectedHalite(game.gameMap, cell.position, cell.actualDist);
        int myHalite = s.halite - cell.lost + cell.gained;
        for (int i = 0; i < numStays; i++) {
            int mined = Math.min(cell.minedAmount(halite), Constants.MAX_HALITE - myHalite);
            int collected = Math.min(cell.collectAmount(halite), Constants.MAX_HALITE - myHalite);
            halite -= mined;
            myHalite += collected;
        }
        totalHalite -= cell.moveCost(halite);*/


        int turns = cell.actualDist + numStays + game.gameMap.calculateDistanceToDropoff(game.players.get(s.owner.id), cell.position);
        return (totalHalite - s.halite) / (turns == 0 ? 1 : turns);
    }

    @Override
    public int getNumberStays(Ship s, MapCell cell, PlannedLocations plan, GameMap map) {
        int halite = plan.getProjectedHalite(map, cell.position, cell.dist);
        int myHalite = s.halite - cell.lost + cell.gained;
        if (halite <= Magic.getCollectDownTo(map) || myHalite == Constants.MAX_HALITE) return 0;
        int turnsStayed;
        for (turnsStayed = 1; ; turnsStayed++) {
            int mined = Math.min(cell.minedAmount(halite), Constants.MAX_HALITE - myHalite);
            int collected = Math.min(cell.collectAmount(halite), Constants.MAX_HALITE - myHalite);
            halite -= mined;
            myHalite += collected;
            if (halite <= Magic.getCollectDownTo(map) || myHalite == Constants.MAX_HALITE) break;
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
