package dhallstr;

import hlt.*;

import java.util.ArrayList;

public abstract class Goal {

    public Intent getIntent() { return Intent.NONE; }
    public int getMaxTurns() { return Constants.MAX_TURNS; }
    public boolean overrideUnsafe(MapCell cell) { return false; }

    public abstract boolean meetsGoal(MapCell cell);
    public abstract int rateTile(Game game, MapCell cell, Ship s, PlannedLocations plan);
    public abstract int getNumberStays(Ship s, MapCell cell, PlannedLocations plan, GameMap map);

    public abstract ArrayList<Direction> sort(GameMap map, MapCell curr, ArrayList<Direction> directions);

}
