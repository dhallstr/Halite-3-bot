package hlt;

public class Dropoff extends Entity {
    public Dropoff(final PlayerId owner, final EntityId id, int x, int y) {
        super(owner, id, x, y);
    }

    static Dropoff _generate(final PlayerId playerId) {
        final Input input = Input.readInput();

        final EntityId dropoffId = new EntityId(input.getInt());
        final int x = input.getInt();
        final int y = input.getInt();

        return new Dropoff(playerId, dropoffId, x, y);
    }
}
