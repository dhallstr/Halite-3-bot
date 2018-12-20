package dhallstr;

import hlt.*;

import java.lang.invoke.ConstantCallSite;

public class DropoffGoal extends Goal {
    PlayerId id;
    boolean crashOkay;

    public DropoffGoal(PlayerId me, boolean crashOkay) {
        id = me;
        this.crashOkay = crashOkay;
    }

    public boolean overrideUnsafe(MapCell cell) { return crashOkay && meetsGoal(cell); }

    @Override
    public int rateTile(Game game, MapCell cell, Ship s, PlannedLocations plan) {
        return meetsGoal(cell) ? (300 - (int)(cell.lost * (Strategy.IS_TWO_PLAYER ? 1.5 : 2)) + cell.gained - 6 * cell.actualDist) : Integer.MIN_VALUE;
    }

    @Override
    public int getNumberStays(Ship s, MapCell cell, PlannedLocations plan, GameMap map) {
        if (crashOkay) return 0;
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

    public boolean meetsGoal(MapCell cell) {
        return cell.structure != null && cell.structure.owner.equals(id);
    }

    public int getMaxTurns() {
        return Constants.MAX_TURNS;
    }

    public Intent getIntent() {
        return crashOkay ? Intent.CRASH_HOME : Intent.DROPOFF;
    }
}
