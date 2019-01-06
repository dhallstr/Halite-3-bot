package dhallstr;

import hlt.*;

import java.util.ArrayList;
import java.util.Comparator;

public class BuildDropoffGoal extends Goal {
    Position p;
    public BuildDropoffGoal(Position p) {
        this.p = p;
    }
    @Override
    public boolean meetsGoal(MapCell cell) {
        return p.equals(cell);
    }

    @Override
    public int rateTile(Game game, MapCell cell, Ship s, PlannedLocations plan) {
        return p.equals(cell) ? 1 : 0;
    }

    @Override
    public int getNumberStays(Ship s, MapCell cell, PlannedLocations plan, GameMap map) {
        return 0;
    }

    @Override
    public Intent getIntent() {
        return Intent.BUILD_DROPOFF;
    }

    public ArrayList<Direction> sort (GameMap map, MapCell curr, ArrayList<Direction> dirs) {
        dirs.sort(Comparator.comparingInt(d -> map.at(curr.directionalOffset(d)).halite));
        return dirs;
    }
}

