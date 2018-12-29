package dhallstr;

import hlt.*;

import java.util.ArrayList;

public class TerrainGoal extends Goal {

    int neededHalite;
    int turns;

    public int getMaxTurns() {
        return turns;
    }

    public TerrainGoal(int halite, int turns) {
        neededHalite = halite;
        this.turns = turns;
    }

    @Override
    public int rateTile(Game game, MapCell cell, Ship s, PlannedLocations plan) {
        int totalHalite = s.halite - cell.lost + cell.gained;
        int distToDropoff = game.gameMap.calculateDistanceToDropoff(game.players.get(s.owner.id), cell);
        if (distToDropoff == 0) return Integer.MIN_VALUE;
        int turns = cell.actualDist + distToDropoff * 3 / 4 + 3;
        int halite = plan.getProjectedHalite(game.gameMap, cell, cell.actualDist);
        int myHalite = totalHalite;
        for (int i = cell.dist; i < cell.actualDist; i++) {

            halite -= Math.min(cell.minedAmount(halite), Constants.MAX_HALITE - myHalite);
            myHalite += Math.min(cell.collectAmount(halite), Constants.MAX_HALITE - myHalite);
        }
        totalHalite -= (cell.moveCost(game.gameMap.percentileHaliteNearMyDropoffs) * (distToDropoff - 1) + cell.moveCost(halite)) * 3 / 4;
        return (totalHalite - s.halite / 2) / (turns);
    }

    @Override
    public int getNumberStays(Ship s, MapCell cell, PlannedLocations plan, GameMap map) {
        int halite = plan.getProjectedHalite(map, cell, cell.dist);
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

    public ArrayList<Direction> sort(GameMap map, MapCell curr, ArrayList<Direction> dirs) {
        dirs.sort((d1, d2) -> map.at(curr.directionalOffset(d2)).halite - map.at(curr.directionalOffset(d1)).halite);
        return dirs;
    }
}
