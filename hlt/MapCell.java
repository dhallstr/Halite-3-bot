package hlt;

public class MapCell {
    public final Position position;
    public int halite;
    public Ship ship;
    public Entity structure;

    public boolean isInspired = false;

    // These are used in navigation and their value should never be used elsewhere
    public boolean visited = false;
    public Direction path = Direction.STILL;
    public int dist = 0, actualDist = 0;
    public int cost = 0;

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

    public int collectAmount(double halite) {
        return (int)Math.ceil(isInspired ? halite / (double)Constants.INSPIRED_EXTRACT_RATIO : halite / (double)Constants.EXTRACT_RATIO);
    }
    public int minedAmount(double halite) {
        return (int)Math.ceil(halite / (double)Constants.EXTRACT_RATIO);
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
