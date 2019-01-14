package dhallstr;

import hlt.*;

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

    boolean isSafe(Game game, Position p, Ship ship, int turnOffset, boolean avoidEnemy) {
        int actualCollision = Collisions.AVOID, collision = Collisions.COLLIDE;
        EntityId e = get(game.gameMap, p, turnOffset);

        if (turnOffset > 5) avoidEnemy = false;

        if (avoidEnemy) {
            Ship shipOnTile = game.gameMap.at(p).ship;
            boolean enemyOnTile = shipOnTile != null && shipOnTile.owner.id != ship.owner.id;
            Ship[] enemies = game.gameMap.getEnemiesNextTo(p, me);
            int numEnemies = 0;
            for (Ship enemy : enemies) {
                if (enemy != null) numEnemies++;
            }
            if (shipOnTile != null && enemyOnTile) actualCollision = Collisions.COLLIDE;
            else if (numEnemies > 0) actualCollision = Collisions.ALLOW;

            if (enemyOnTile)
                collision = Collisions.shouldCollide(ship, shipOnTile, game);
            for (Ship enemy : enemies) {
                if (enemy != null)
                    collision = Math.min(collision, Collisions.shouldCollide(ship, enemy, game));
            }
        }
        return (game.gameMap.at(p).structure == null || game.gameMap.at(p).structure.owner.equals(me)) &&
                (e == null || e.id == ship.id.id) &&
                ((!avoidEnemy || actualCollision <= collision) || (game.gameMap.at(p).hasStructure() && game.gameMap.at(p).structure.owner.equals(me)));
    }

    void addPlan(GameMap map, Ship s, Direction[] plan, Intent intent) {
        cancelPlan(map, s, 1);
        shipPlans.put(s.id, intent);
        Position p = s;
        for (int i = 0; i < plan.length; i++) {
            p = p.directionalOffset(plan[i]);
            addLoc(map, p, s.id, i+1);
            if (plan[i] == Direction.STILL) {
                setWasMined(map, p, i, 1);
            }
        }
        if (intent == Intent.GATHER) {
            addIntent(map, p, plan.length - 1, Intent.DROPOFF);
        }
    }

    void cancelPlan(GameMap map, Ship s, int turnOffset) {
        if (shipPlans.get(s.id) != null) shipPlans.put(s.id, Intent.NONE);
        Position p = getLocation(map, s, turnOffset);
        if (p != null) {
            cancelPlan(map, s, turnOffset + 1);
            set(map, p, turnOffset, null);
            setWasMined(map, p, turnOffset, 0);
            addIntent(map, p, turnOffset, null);
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

    public Intent getIntent(GameMap map, Position p, int turnOffset) {
        FutureNode f = getFuture(map, p, turnOffset);
        return f == null ? null : f.nextIntent;
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

    private void addIntent(GameMap map, Position p, int turnOffset, Intent intent) {
        Position norm = map.normalize(p);
        if (cells[norm.y][norm.x].size() > turnOffset)
            cells[norm.y][norm.x].get(turnOffset).nextIntent = intent;
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
        if (turnOffset == 0) return s;
        Position p = s;
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
        Intent nextIntent = null;
    }
}
