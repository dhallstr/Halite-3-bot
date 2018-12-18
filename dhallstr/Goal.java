package dhallstr;

import hlt.*;

public abstract class Goal {

    public abstract boolean meetsGoal(MapCell cell);
    public abstract Direction[] orderDirections(GameMap map, MapCell cell);
    public abstract int getTurns();
    public abstract Goal getSimpleGoal();
    public abstract Goal setSimpleGoal(Goal g);
    public abstract boolean overrideUnsafe(MapCell cell);
    public abstract Intent getIntent();

    public abstract int rateTile(Game game, MapCell cell, Ship s, PlannedLocations plan);
    public abstract int getAutoAccept();
    public abstract int getMinScore();

    public boolean waitAfterNavigate() {
        return false;
    }

    public abstract int getNumberStays(Ship s, MapCell cell, PlannedLocations plan, GameMap map);

    protected Direction[] order(GameMap map, MapCell cell, boolean wantHalite) {
        int i = 0;
        int[] scores = new int[4];
        Direction[] dirs = new Direction[4];
        for (Direction d: Direction.ALL_CARDINALS) {
            int s = map.at(cell.position.directionalOffset(d)).halite;
            int j;
            for (j = i; j > 0; j--) {
                if ((!wantHalite && scores[j-1] < s)||
                    (wantHalite && scores[j-1] > s)) {
                    break;
                }
                scores[j] = scores[j-1];
                dirs[j] = dirs[j-1];
            }
            scores[j] = s;
            dirs[j] = d;
            i++;
        }
        return dirs;
    }
}
