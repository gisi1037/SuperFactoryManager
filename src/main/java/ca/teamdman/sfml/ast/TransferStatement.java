package ca.teamdman.sfml.ast;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemTransferResult;
import ca.teamdman.sfml.data.ResourceSelector;

/** A visible effect boundary. Arguments are read-only expressions and evaluated once in source order. */
public final class TransferStatement implements Statement {

    public static final DataType RESULT_TYPE;
    private static final Map<ItemTransferResult.Status, DataValue> STATUSES;
    private static final DataValue ALL_ITEMS = DataValue.selector(ResourceSelector.item("*"));
    private static final DataValue ALL_FLUIDS = DataValue.selector(ResourceSelector.fluid("*"));
    static {
        Map<String, DataType> fields = new LinkedHashMap<>();
        fields.put("requested", DataType.INTEGER);
        fields.put("moved", DataType.INTEGER);
        fields.put("remaining", DataType.INTEGER);
        fields.put("status", DataType.STRING);
        RESULT_TYPE = DataType.struct(fields);
        Map<ItemTransferResult.Status, DataValue> statuses = new EnumMap<>(ItemTransferResult.Status.class);
        for (ItemTransferResult.Status status : ItemTransferResult.Status.values()) statuses.put(
            status,
            DataValue.string(
                status.name()
                    .toLowerCase(Locale.ROOT)));
        STATUSES = Collections.unmodifiableMap(statuses);
    }

    private final DataExpression source, destination, item, amount;
    private final DataCompiler.Binding result;
    private final boolean fluids;

    public TransferStatement(List<DataExpression> arguments, DataCompiler.Binding result) {
        this(arguments, result, false);
    }

    public TransferStatement(List<DataExpression> arguments, DataCompiler.Binding result, boolean fluids) {
        this.fluids = fluids;
        source = arguments.get(0);
        destination = arguments.get(1);
        // Resolve the third-argument overload once from its static type, without evaluating it.
        boolean amountOnly = arguments.size() == 3 && arguments.get(2)
            .getType()
            .equals(DataType.INTEGER);
        item = arguments.size() > 2 && !amountOnly ? arguments.get(2) : null;
        amount = amountOnly ? arguments.get(2) : arguments.size() > 3 ? arguments.get(3) : null;
        this.result = result;
    }

    @Override
    public void tick(ProgramContext context) {
        DataRuntime runtime = context.getDataRuntime();
        DataValue from = source.evaluate(runtime);
        DataValue to = destination.evaluate(runtime);
        DataValue selected = item == null ? fluids ? ALL_FLUIDS : ALL_ITEMS : item.evaluate(runtime);
        long requested = amount == null ? 0
            : amount.evaluate(runtime)
                .asLong();
        runtime.work(1);
        if (result == null) {
            transfer(context, from, to, selected, requested);
            return;
        }
        // Reserve record values, child-array/copy and result callback before any provider mutation.
        runtime.allocate(10);
        runtime.allocateString(11); // Longest status is NO_CAPACITY/UNAVAILABLE; reserve before effects.
        result.writeActionResult(runtime, () -> {
            ItemTransferResult moved = transfer(context, from, to, selected, requested);
            DataValue[] fields = new DataValue[4];
            for (int i = 0; i < fields.length; i++) {
                String name = RESULT_TYPE.getFieldNames()
                    .get(i);
                fields[i] = name.equals("requested") ? DataValue.integer(moved.getRequested())
                    : name.equals("moved") ? DataValue.integer(moved.getMoved())
                        : name.equals("remaining") ? DataValue.integer(moved.getRemaining())
                            : STATUSES.get(moved.getStatus());
            }
            return DataValue.collection(RESULT_TYPE, fields);
        });
    }

    private ItemTransferResult transfer(ProgramContext context, DataValue from, DataValue to, DataValue selected,
        long requested) {
        if (fluids) {
            return amount == null ? context.transferFluids(from.asLabel(), to.asLabel(), selected)
                : context.transferFluids(from.asLabel(), to.asLabel(), selected, requested);
        }
        return amount == null ? context.transferItems(from.asLabel(), to.asLabel(), selected)
            : context.transferItems(from.asLabel(), to.asLabel(), selected, requested);
    }

    @Override
    public String toString() {
        return (fluids ? "TRANSFERF (" : "TRANSFER (") + source
            + ", "
            + destination
            + (item == null ? "" : ", " + item)
            + (amount == null ? "" : ", " + amount)
            + ")"
            + (result == null ? "" : " INTO $" + result.name);
    }
}
