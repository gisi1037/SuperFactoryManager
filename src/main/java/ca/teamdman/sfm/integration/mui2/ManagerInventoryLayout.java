package ca.teamdman.sfm.integration.mui2;

/** Presentation-only partition of canonical player indices. No stack or slot is moved. */
public final class ManagerInventoryLayout {

    public final int storage;
    private final int[] sections, offsets;
    private int leftColumns = 3, leftRows, rightColumns = 3, rightRows;

    public ManagerInventoryLayout(int storage) {
        if (storage < 0) throw new IllegalArgumentException("Negative storage capacity");
        this.storage = storage;
        sections = new int[storage];
        offsets = new int[storage];
        preset(true);
    }

    public ManagerInventoryLayout copy() {
        ManagerInventoryLayout copy = new ManagerInventoryLayout(storage);
        copy.leftColumns = leftColumns;
        copy.leftRows = leftRows;
        copy.rightColumns = rightColumns;
        copy.rightRows = rightRows;
        System.arraycopy(sections, 0, copy.sections, 0, storage);
        System.arraycopy(offsets, 0, copy.offsets, 0, storage);
        return copy;
    }

    public int count(int section) {
        return section == 1 ? leftColumns * leftRows
            : section == 2 ? rightColumns * rightRows : storage - count(1) - count(2);
    }

    public int columns(int section) {
        return section == 1 ? leftColumns : section == 2 ? rightColumns : 9;
    }

    public int rows(int section) {
        return section == 1 ? leftRows : section == 2 ? rightRows : (count(0) + 8) / 9;
    }

    /** Each view retains its own stable indices, rendered in ascending canonical order. */
    public int section(int canonical) {
        if (canonical < 9 || canonical >= storage + 9) throw new IllegalArgumentException("Not a storage index");
        return sections[canonical - 9];
    }

    public int offset(int canonical) {
        section(canonical);
        return offsets[canonical - 9];
    }

    public boolean canResize(int section, boolean columns, int delta) {
        if (section < 1 || section > 2 || Math.abs(delta) != 1) return false;
        int cols = columns(section) + (columns ? delta : 0);
        int rows = rows(section) + (columns ? 0 : delta);
        if (cols < 1 || cols > 6 || rows < 0 || rows > 12) return false;
        return cols * rows <= count(0) + count(section);
    }

    public boolean resize(int section, boolean columns, int delta) {
        if (!canResize(section, columns, delta)) return false;
        int previous = count(section);
        if (section == 1) {
            leftColumns += columns ? delta : 0;
            leftRows += columns ? 0 : delta;
        } else {
            rightColumns += columns ? delta : 0;
            rightRows += columns ? 0 : delta;
        }
        int change = count(section) - previous;
        int from = change > 0 ? 0 : section, to = change > 0 ? section : 0;
        int remaining = Math.abs(change);
        for (int i = storage - 1; i >= 0 && remaining > 0; i--) if (sections[i] == from) {
            sections[i] = to;
            remaining--;
        }
        rebuildOffsets();
        return true;
    }

    public void preset(boolean split) {
        leftColumns = rightColumns = 3;
        leftRows = rightRows = split ? Math.min(4, storage / 18) : 0;
        for (int i = 0; i < storage; i++) sections[i] = i < count(0) ? 0 : i < count(0) + count(1) ? 1 : 2;
        rebuildOffsets();
    }

    private void rebuildOffsets() {
        int[] counts = new int[3];
        for (int i = 0; i < storage; i++) offsets[i] = counts[sections[i]]++;
    }

    public String summary() {
        return "Main " + count(0) + " + Left " + count(1) + " + Right " + count(2) + " = " + storage;
    }
}
