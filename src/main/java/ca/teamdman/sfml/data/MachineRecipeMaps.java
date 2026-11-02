package ca.teamdman.sfml.data;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/** Immutable inspection only. Map membership grants no machine or recipe execution authority. */
public final class MachineRecipeMaps {

    public enum Status {
        MATCHES,
        UNAVAILABLE,
        UNSUPPORTED,
        AMBIGUOUS,
        NOT_READY,
        STALE
    }

    private static final DataType STRINGS = DataType.dynamicArray(DataType.STRING);
    public static final DataType TYPE;
    static {
        Map<String, DataType> fields = new TreeMap<>();
        fields.put("available", STRINGS);
        fields.put("current", DataType.STRING);
        fields.put("status", DataType.STRING);
        TYPE = DataType.struct(fields);
    }

    public final Status status;
    public final String current;
    public final List<String> available;

    private MachineRecipeMaps(Status status, String current) {
        this.status = status;
        this.current = current;
        available = current.isEmpty() ? Collections.emptyList() : Collections.singletonList(current);
    }

    /** The first qualified stock machine family advertises exactly its current map. */
    public static MachineRecipeMaps matches(String map) {
        if (!qualified(map)) throw new IllegalArgumentException("Unqualified machine recipe map");
        return new MachineRecipeMaps(Status.MATCHES, map);
    }

    public static boolean qualified(String map) {
        return "gt.recipe.macerator".equals(map) || "gt.recipe.metalbender".equals(map)
            || "gt.recipe.cuttingsaw".equals(map);
    }

    public static MachineRecipeMaps unavailable(Status status) {
        if (status == Status.MATCHES) throw new IllegalArgumentException("A matching machine requires a map");
        return new MachineRecipeMaps(status, "");
    }

    DataValue value(QueryBudget owner, DataRuntime runtime) {
        runtime.requireQueryBudget(owner);
        String text = status.name()
            .toLowerCase(Locale.ROOT);
        int cells = TYPE.getCellCount() + available.size();
        int characters = current.length() * 2 + text.length();
        runtime.allocate(cells);
        runtime.allocateString(characters);
        runtime.work(cells * 4 + characters);
        owner.chargeSlots(cells * 4 + characters);
        owner.chargeAllocation(cells * 4);
        DataValue[] maps = new DataValue[available.size()];
        for (int i = 0; i < maps.length; i++) maps[i] = DataValue.string(available.get(i));
        return DataValue.ownedCollection(
            TYPE,
            new DataValue[] { DataValue.ownedDynamicArray(STRINGS, maps), DataValue.string(current),
                DataValue.string(text) });
    }
}
