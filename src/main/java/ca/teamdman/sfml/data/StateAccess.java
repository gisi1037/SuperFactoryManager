package ca.teamdman.sfml.data;

/** Disk adapter seam. Slots are scoped to the validated active program schema. */
public interface StateAccess {

    DataValue read(int slot);

    void write(int slot, DataValue value);
}
