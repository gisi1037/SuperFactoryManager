package ca.teamdman.sfml.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Flat immutable tank observations, in mB. A tank index is advisory, never transfer authority. */
public final class FluidContents {

    public static final int MAX_ROWS = 1024;

    public static final class Row {

        public final FluidIdentity fluid;
        public final int dimension, x, y, z, tank, sideMask;
        public final long amount;

        public Row(FluidIdentity fluid, long amount, int dimension, int x, int y, int z, int tank, int sideMask) {
            this.fluid = fluid;
            this.amount = amount;
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.tank = tank;
            this.sideMask = sideMask;
        }
    }

    private final QueryBudget owner;
    private final List<Row> rows;

    public FluidContents(QueryBudget owner, List<Row> rows) {
        if (owner == null || rows == null || rows.size() > MAX_ROWS)
            throw new IllegalArgumentException("Invalid fluid observation");
        for (Row row : rows) {
            owner.chargeSlot();
            if (row == null || row.fluid == null
                || row.amount <= 0
                || row.amount > Integer.MAX_VALUE
                || row.tank < 0
                || row.tank >= 256
                || row.sideMask <= 0
                || (row.sideMask & ~63) != 0) throw new IllegalArgumentException("Invalid fluid observation row");
        }
        owner.chargeAllocation(2 + rows.size());
        this.owner = owner;
        this.rows = Collections.unmodifiableList(new ArrayList<>(rows));
    }

    public DataValue materialize(QueryBudget sameOwner, DataRuntime runtime) {
        if (owner != sameOwner) throw new IllegalArgumentException("Fluid contents require their owning query budget");
        runtime.requireQueryBudget(owner);
        int characters = 0;
        for (Row row : rows) characters = Math.addExact(
            characters,
            row.fluid.getId()
                .length());
        DataType type = DataType.FLUID_CONTENTS.getElementType();
        int width = type.getLength();
        int cells = 1 + rows.size() * (width + 1);
        runtime.allocate(cells);
        runtime.allocateString(characters);
        runtime.work(cells * 3 + characters);
        owner.chargeSlots(cells * 3 + characters);
        owner.chargeAllocation(cells + rows.size() * width);
        int fluidField = type.fieldIndex("fluid");
        int idField = type.fieldIndex("id");
        int amountField = type.fieldIndex("amount");
        int dimensionField = type.fieldIndex("dimension");
        int xField = type.fieldIndex("x");
        int yField = type.fieldIndex("y");
        int zField = type.fieldIndex("z");
        int tankField = type.fieldIndex("tank");
        int sideMaskField = type.fieldIndex("sideMask");
        DataValue[] result = new DataValue[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            DataValue[] values = new DataValue[width];
            values[fluidField] = DataValue.fluidIdentity(row.fluid);
            values[idField] = DataValue.string(row.fluid.getId());
            values[amountField] = DataValue.integer(row.amount);
            values[dimensionField] = DataValue.integer(row.dimension);
            values[xField] = DataValue.integer(row.x);
            values[yField] = DataValue.integer(row.y);
            values[zField] = DataValue.integer(row.z);
            values[tankField] = DataValue.integer(row.tank);
            values[sideMaskField] = DataValue.integer(row.sideMask);
            result[i] = DataValue.ownedCollection(type, values);
        }
        return DataValue.ownedSequence(DataType.FLUID_CONTENTS, result);
    }
}
