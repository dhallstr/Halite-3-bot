package dhallstr;

import hlt.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;


public class PlannedLocations {

    public PlayerId me;

    private ParallelSlidingList<FutureNode>[][] cells;

    Map<EntityId, Intent> shipPlans = new LinkedHashMap<>();

    public void updateTurn(int turnNum) {
        ParallelSlidingList.location = turnNum;
    }

    @SuppressWarnings("unchecked")
    public PlannedLocations(int width, int height, PlayerId me) {
        this.me = me;
        cells = new ParallelSlidingList[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                cells[i][j] = new ParallelSlidingList<>(Constants.MAX_TURNS + 1);
                for (int k = 0; k < Constants.MAX_TURNS + 1; k++) {
                    cells[i][j].add(new FutureNode(), k);
                }
            }
        }
    }

    boolean isSafe(GameMap map, Position p, Ship ship, int turnOffset, boolean avoidEnemy) {
        EntityId e = get(map, p, turnOffset);
        EntityId id = (ship == null) ? null : ship.id;
        PlayerId playerId = (ship == null) ? null : ship.owner;
        return (map.at(p).structure == null || map.at(p).structure.owner.equals(me)) &&
                (e == null || e.equals(id)) &&
                (!avoidEnemy || turnOffset > 3 || !map.isEnemyWithin(p, 1, playerId) || (map.at(p).hasStructure() && map.at(p).structure.owner.equals(me)));
    }

    void addPlan(GameMap map, Ship s, Direction[] plan, Intent intent) {
        cancelPlan(map, s, 1);
        Log.log("Adding plan of ship " + s.id + " " + Arrays.toString(plan));
        shipPlans.put(s.id, intent);
        Position p = s.position;
        for (int i = 0; i < plan.length; i++) {
            if (plan[i] == null) break;
            p = p.directionalOffset(plan[i]);
            addLoc(map, p, s.id, i+1);
            if (plan[i] == Direction.STILL) {
                setWasMined(map, p, i, 1);
            }
        }
    }

    void cancelPlan(GameMap map, Ship s, int turnOffset) {
        Log.log("Cancelling plan of ship " + s.id);
        if (shipPlans.get(s.id) != null) shipPlans.put(s.id, Intent.NONE);
        Position p = getLocation(map, s, turnOffset);
        if (p != null) {
            cancelPlan(map, s, turnOffset + 1);
            set(map, p, turnOffset, null);
            setWasMined(map, p, turnOffset, 0);
        }
    }

    int getProjectedHalite(GameMap map, Position p, int turnOffset) {
        if (cells[0][0].size() - 1 < turnOffset)
            return 0;

        FutureNode future;
        MapCell cell = map.at(p);
        int halite = cell.halite;
        for (int i = 0; i < turnOffset; i++) {
            future = getFuture(map, p, i);
            if (future != null && future.wasMined == 1) {
                halite -= cell.minedAmount(halite);
            }
        }
        return halite;
    }

    private FutureNode getFuture(GameMap map, Position p, int turnOffset) {
        if (cells[0][0].size() - 1 < turnOffset)
            return null;
        Position norm = map.normalize(p);
        return cells[norm.y][norm.x].get(turnOffset);
    }

    public EntityId get(GameMap map, Position p, int turnOffset) {
        FutureNode f = getFuture(map, p, turnOffset);
        return f == null ? null : f.id;
    }

    public void set(GameMap map, Position p, int turnOffset, EntityId id) {
        if (cells[0][0].size() - 1 < turnOffset)
            return;
        Position norm = map.normalize(p);
        cells[norm.y][norm.x].get(turnOffset).id = id;
    }

    public void setWasMined(GameMap map, Position p, int turnOffset, int wasMined) {
        if (cells[0][0].size() - 1 < turnOffset)
            return;
        Position norm = map.normalize(p);
        cells[norm.y][norm.x].get(turnOffset).wasMined = wasMined;
    }

    private void addLoc(GameMap map, Position p, EntityId id, int turnOffset) {
        Position norm = map.normalize(p);
        if (cells[norm.y][norm.x].size() > turnOffset)
            cells[norm.y][norm.x].get(turnOffset).id = id;
    }

    Direction getNextStep(GameMap map, Ship s, int turnOffset) {
        return getNextStep(map, getLocation(map, s, turnOffset), s, turnOffset);
    }
    // p is position on previous turn
    private Direction getNextStep(GameMap map, Position p, Ship s, int turnOffset) {
        if (cells[0][0].size() - 1 < turnOffset + 1)
            return null;
        if (p == null) return null;
        if (s.id.equals(get(map, p, 1 + turnOffset)))
            return Direction.STILL;
        for (Direction d: Direction.ALL_CARDINALS) {
            if (s.id.equals(get(map, p.directionalOffset(d), 1 + turnOffset)))
                return d;
        }
        return null;
    }

    private Position getLocation(GameMap map, Ship s, int turnOffset) {
        if (turnOffset == 0) return s.position;
        Position p = s.position;
        for (int i = 0; i < turnOffset; i++) {
            Direction d = getNextStep(map, p, s, i);
            if (d == null) return null;
            p = p.directionalOffset(d);
        }
        return p;
    }

    private class FutureNode {
        EntityId id = null;
        int wasMined = 0;
    }
}
