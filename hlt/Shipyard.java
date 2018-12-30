package hlt;

public class Shipyard extends Dropoff {
    public Shipyard(final PlayerId owner, final int x, final int y) {
        super(owner, EntityId.NONE, x, y);
    }

    public Command spawn() {
        return Command.spawnShip();
    }
}
