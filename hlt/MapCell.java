package hlt;

public class MapCell extends Position {
    public int halite;
    public Ship ship;
    public Entity structure;

    public boolean isInspired = false;

    // These are used in navigation and their value should never be used elsewhere
    public boolean visited = false, processed = false;
    public Direction path = Direction.STILL;
    public int dist = 0, actualDist = 0, depth = 0;
    public int lost = 0;
    public int gained = 0;
    public int score = Integer.MIN_VALUE;

    // These are used in other BFS applications
    boolean secondaryVisited = false;
    int secondaryDist = 0;


    public int enemyShipsInInspirationRange = 0, enemyShipsNearby = 0, friendlyShipsNearby = 0, haliteNearby = -1;

    MapCell(final int x, final int y, final int halite) {
        super(x, y);
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
    public int haliteAfterXTurns(int initialHalite, int shipHalite, int x) {
        int myHalite = shipHalite;
        int halite = initialHalite;
        for (int i = 0; i < x; i++) {

            halite -= Math.min(minedAmount(halite), Constants.MAX_HALITE - myHalite);
            myHalite += Math.min(collectAmount(halite), Constants.MAX_HALITE - myHalite);
        }
        return halite;
    }
    public int haliteCollectedAfterXTurns(int initialHalite, int shipHalite, int x) {
        int myHalite = shipHalite;
        int halite = initialHalite;
        for (int i = 0; i < x; i++) {

            halite -= Math.min(minedAmount(halite), Constants.MAX_HALITE - myHalite);
            myHalite += Math.min(collectAmount(halite), Constants.MAX_HALITE - myHalite);
        }
        return myHalite - shipHalite;
    }

    public boolean hasStructure() {
        return structure != null;
    }

}
