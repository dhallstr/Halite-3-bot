package hlt;

public class Entity extends Position {
    public final PlayerId owner;
    public final EntityId id;

    public Entity(final PlayerId owner, final EntityId id, final int x, final int y) {
        super(x, y);
        this.owner = owner;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return super.equals(o);

        Entity entity = (Entity) o;

        if (!owner.equals(entity.owner)) return false;
        if (!id.equals(entity.id)) return false;
        return super.equals(entity);
    }

    @Override
    public int hashCode() {
        int result = owner.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + super.hashCode();
        return result;
    }
}
