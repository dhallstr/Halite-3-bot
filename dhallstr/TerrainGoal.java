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
        int totalHalite = s.halite - (int)(cell.lost) + cell.gained;

        int distToDropoff = game.gameMap.calculateDistanceToDropoff(game.players.get(s.owner.id), cell, true);
        if (distToDropoff == 0 || cell.actualDist == 0) return Integer.MIN_VALUE;

        int turns = (int)(cell.actualDist + distToDropoff / 2);
        totalHalite -= cell.moveCost(cell.haliteAfterXTurns(plan.getProjectedHalite(game.gameMap, cell, cell.actualDist), totalHalite, cell.actualDist - cell.dist));
        return 10000 * (totalHalite - s.halite + cell.extra) / (turns);
    }

    @Override
    public int getNumberStays(Ship s, MapCell cell, PlannedLocations plan, GameMap map) {
        int halite = plan.getProjectedHalite(map, cell, cell.dist);
        int myHalite = s.halite - cell.lost + cell.gained;
        if (halite <= Magic.getCollectDownTo(map, cell, myHalite) || myHalite == Constants.MAX_HALITE) return 0;
        int turnsStayed;
        for (turnsStayed = 1; ; turnsStayed++) {
            int mined = Math.min(cell.minedAmount(halite), Constants.MAX_HALITE - myHalite);
            int collected = Math.min(cell.collectAmount(halite), Constants.MAX_HALITE - myHalite);
            halite -= mined;
            myHalite += collected;
            if (halite <= Magic.getCollectDownTo(map, cell, myHalite) || myHalite >= Constants.MAX_HALITE - Magic.getMinHaliteMined(map, cell, myHalite)) break;
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
