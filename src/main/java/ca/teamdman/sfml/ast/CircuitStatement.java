package ca.teamdman.sfml.ast;

import java.util.LinkedHashMap;
import java.util.Map;

import ca.teamdman.sfm.common.program.GhostCircuitService;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.LabelReference;

/** Machine configuration remains an explicit action, never hidden in an ordinary value expression. */
public final class CircuitStatement implements Statement {

    public static final DataType RESULT_TYPE;
    static {
        Map<String, DataType> fields = new LinkedHashMap<>();
        fields.put("matched", DataType.INTEGER);
        fields.put("changed", DataType.INTEGER);
        fields.put("status", DataType.STRING);
        RESULT_TYPE = DataType.struct(fields);
    }
    private final DataExpression target, configuration;
    private final DataCompiler.Binding result;

    public CircuitStatement(DataExpression target, DataExpression configuration, DataCompiler.Binding result) {
        this.target = target;
        this.configuration = configuration;
        this.result = result;
    }

    @Override
    public void tick(ProgramContext context) {
        DataRuntime runtime = context.getDataRuntime();
        LabelReference label = target.evaluate(runtime)
            .asLabel();
        long value = configuration.evaluate(runtime)
            .asLong();
        if (result == null) {
            context.setCircuit(label, value);
            return;
        }
        runtime.allocate(8);
        runtime.allocateString(11);
        result.writeActionResult(runtime, () -> {
            GhostCircuitService.Result changed = context.setCircuit(label, value);
            DataValue[] fields = new DataValue[3];
            fields[RESULT_TYPE.fieldIndex("matched")] = DataValue.integer(changed.matched);
            fields[RESULT_TYPE.fieldIndex("changed")] = DataValue.integer(changed.changed);
            fields[RESULT_TYPE.fieldIndex("status")] = DataValue.string(changed.status);
            return DataValue.collection(RESULT_TYPE, fields);
        });
    }

    @Override
    public String toString() {
        return "SETCIRCUIT (" + target + ", " + configuration + ")" + (result == null ? "" : " INTO $" + result.name);
    }
}
