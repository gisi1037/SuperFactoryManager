package ca.teamdman.sfml.data;

/** Dynamic STATE requires structural epochs and a store-independent lifetime token. */
public interface VersionedStateAccess extends StateAccess {

    /** Stable token for this heap's lifetime; must not retain the store, world or disk. */
    Object identity();

    long generation(int slot);

    void write(int slot, DataValue value, boolean structural);
}
