package dhallstr;

import hlt.*;

public class PositionGoal extends Goal {
    Position pos;
    Goal simple = null;
    Intent intent;
    public PositionGoal(Position p, Intent i) {
        pos = p;
        intent = i;
    }

    public boolean overrideUnsafe(MapCell cell) { return false; }

    public boolean meetsGoal(MapCell cell) {
        return pos.equals(cell.position);
    }

    public int getTurns() {
        return Constants.MAX_TURNS;
    }

    public Goal getSimpleGoal() {
        return simple;
    }
    public Goal setSimpleGoal(Goal g) {
        simple = g;
        return this;
    }

    @Override
    public int rateTile(Game game, MapCell cell, Ship s, PlannedLocations plan) {
        return cell.position.equals(pos) ? 100 : 0;
    }

    @Override
    public int getNumberStays(Ship s, MapCell cell, PlannedLocations plan, GameMap map) {
        return 0;
    }

    @Override
    public int getAutoAccept() {
        return 99;
    }

    @Override
    public int getMinScore() {
        return 50;
    }

    public Intent getIntent() {
        return intent;
    }

    public Direction[] orderDirections(GameMap map, MapCell cell) {
        return order(map, cell, false);
    }

}
