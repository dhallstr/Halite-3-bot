package hlt;

import dhallstr.Magic;

import java.util.LinkedList;

public class GameMap {
    public final int width;
    public final int height;
    private final MapCell[][] cells;

    public int haliteOnMap = 0;
    public int percentileHalite, percentileHaliteNearMyDropoffs;

    public GameMap(final int width, final int height) {
        this.width = width;
        this.height = height;

        cells = new MapCell[height][];
        for (int y = 0; y < height; ++y) {
            cells[y] = new MapCell[width];
        }
    }

    public MapCell at(final Position position) {
        final Position normalized = normalize(position);
        return cells[normalized.y][normalized.x];
    }

    public MapCell offset(Position p, Direction d) {
        return at(p.directionalOffset(d));
    }

    public int calculateDistance(final Position source, final Position target) {
        final Position normalizedSource = normalize(source);
        final Position normalizedTarget = normalize(target);

        final int dx = Math.abs(normalizedSource.x - normalizedTarget.x);
        final int dy = Math.abs(normalizedSource.y - normalizedTarget.y);

        final int toroidal_dx = Math.min(dx, width - dx);
        final int toroidal_dy = Math.min(dy, height - dy);

        return toroidal_dx + toroidal_dy;
    }

    public int calculateDistanceToDropoff(Player p, Position pos) {
        int min = Integer.MAX_VALUE;
        for (Dropoff d: p.dropoffs.values()) {
            int dist = calculateDistance(pos, d);
            if (dist < min) min = dist;
        }
        return min;
    }

    public Ship[] getEnemiesNextTo(Position pos, PlayerId me) {
        Ship[] enemies = new Ship[Direction.ALL_CARDINALS.size()];

        MapCell m = at(pos);
        for (int i = 0; i < enemies.length; i++) {
            MapCell c = offset(m, Direction.ALL_CARDINALS.get(i));
            if (c.ship != null && c.ship.owner.id != me.id) enemies[i] = c.ship;
        }
        return enemies;
    }

    void updateInRange(Game game, PlayerId me) {
        haliteOnMap = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < height; j++) {
                cells[i][j].enemyShipsInInspirationRange = 0;
                cells[i][j].friendlyShipsNearby = 0;
                cells[i][j].enemyShipsNearby = 0;
                haliteOnMap += cells[i][j].halite;
            }
        }

        for (Player p: game.players) {
            for (Ship s: p.ships.values()) {
                if (p.id.id != me.id) {
                    for (int[] offset : Magic.INSPIRE_OFFSET) {
                        cells[(s.y + offset[0] + height) % height][(s.x + offset[1] + width) % width].enemyShipsInInspirationRange++;
                    }
                }
                for (int[] offset : Magic.NEARBY_SHIP_OFFSET) {
                    if (p.id.id == me.id)
                        cells[(s.y + offset[0] + height) % height][(s.x + offset[1] + width) % width].friendlyShipsNearby++;
                    else
                        cells[(s.y + offset[0] + height) % height][(s.x + offset[1] + width) % width].enemyShipsNearby++;
                }
            }
        }
        int numInspired = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < height; j++) {
                if (Constants.INSPIRATION_ENABLED) {
                    cells[i][j].isInspired = (cells[i][j].enemyShipsInInspirationRange >= Constants.INSPIRATION_SHIP_COUNT);
                    if (cells[i][j].isInspired) {
                        numInspired++;
                    }
                }
                cells[i][j].haliteNearby = numHaliteWithin(new Position(i, j), Magic.BUILD_DROPOFF_RADIUS);
            }
        }
        Log.log(numInspired == 0 ? "No inspired" : "" + numInspired + " number of inspired locations");
    }
    public int numHaliteWithin(Position pos, int radius) {
        int total = 0;

        setAllSecondaryUnvisited();

        LinkedList<MapCell> queue = new LinkedList<>();
        queue.add(at(pos));
        at(pos).secondaryVisited = true;

        while (!queue.isEmpty()) {
            MapCell curr = queue.poll();
            if (curr == null) continue;

            if (curr.secondaryDist > radius) {
                break;
            }

            for (Direction d: Direction.ALL_CARDINALS) {
                MapCell m = offset(curr, d);
                if (!m.secondaryVisited){
                    queue.add(m);
                    m.secondaryVisited = true;
                    m.secondaryDist = curr.secondaryDist + 1;
                    total += m.halite;
                }
            }
        }
        return total;
    }

    public Position normalize(final Position position) {
        final int x = ((position.x % width) + width) % width;
        final int y = ((position.y % height) + height) % height;
        return new Position(x, y);
    }

    public void setAllUnvisited() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                cells[i][j].visited = false;
                cells[i][j].processed = false;
                cells[i][j].path = Direction.STILL;
                cells[i][j].dist = 0;
                cells[i][j].actualDist = 0;
                cells[i][j].depth = 0;
                cells[i][j].lost = 0;
                cells[i][j].gained = 0;
                cells[i][j].score = Integer.MIN_VALUE;
            }
        }
    }

    private void setAllSecondaryUnvisited() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                cells[i][j].secondaryVisited = false;
                cells[i][j].secondaryDist = 0;
            }
        }
    }

    void _update() {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                cells[y][x].ship = null;
            }
        }

        final int updateCount = Input.readInput().getInt();

        for (int i = 0; i < updateCount; ++i) {
            final Input input = Input.readInput();
            final int x = input.getInt();
            final int y = input.getInt();

            cells[y][x].halite = input.getInt();

        }
    }

    static GameMap _generate() {
        final Input mapInput = Input.readInput();
        final int width = mapInput.getInt();
        final int height = mapInput.getInt();

        final GameMap map = new GameMap(width, height);

        for (int y = 0; y < height; ++y) {
            final Input rowInput = Input.readInput();

            for (int x = 0; x < width; ++x) {
                final int halite = rowInput.getInt();
                map.cells[y][x] = new MapCell(x, y, halite);
            }
        }

        return map;
    }
}
