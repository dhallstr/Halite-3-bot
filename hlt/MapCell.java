package hlt;

public class MapCell {
    public final Position position;
    public int halite;
    public Ship ship;
    public Entity structure;

    public boolean isInspired = false;

    // These are used in navigation and their value should never be used elsewhere. Otherwise behavior is undefined
    public boolean visited = false;
    public Direction path = Direction.STILL;
    public int dist = 0, actualDist = 0;
    public int lost = 0;
    public int gained = 0;
    public int haliteExpected = 0;
    public int bestHaliteD = 0;
    public int bestPathLength = 0;
    public int bestFuturePathCost = 0;


    // These are used in other BFS applications
    public boolean secondaryVisited = false;
    public int secondaryDist = 0;


    public int enemyShipsNearby = 0;

    public MapCell(final Position position, final int halite) {
        this.position = position;
        this.halite = halite;
    }

    // cost of moving from this tile
    public int moveCost() {
        return moveCost(halite);
    }
    public int moveCost(int halite) {
        return isInspired ? halite / Constants.INSPIRED_MOVE_COST_RATIO : halite / Constants.MOVE_COST_RATIO;
    }

    public int collectAmount(int halite) {
        return (int)(minedAmount(halite) * (isInspired ? (1 + Constants.INSPIRED_BONUS_MULTIPLIER) : 1));
    }
    public int minedAmount(int halite) {
        return isInspired ? ((halite + Constants.INSPIRED_EXTRACT_RATIO - 1) / Constants.INSPIRED_EXTRACT_RATIO) : ((halite + Constants.EXTRACT_RATIO - 1) / Constants.EXTRACT_RATIO);
    }

    public boolean isEmpty() {
        return ship == null && structure == null;
    }

    public boolean isOccupied() {
        return ship != null;
    }

    public boolean hasStructure() {
        return structure != null;
    }

    public void markUnsafe(final Ship ship) {
        this.ship = ship;
    }
}
