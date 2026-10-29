package ca.teamdman.sfml.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.teamdman.sfm.common.program.DebugValueFormatter;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataValue;

/** Diagnostic statement. Disabled/capped capture never evaluates its arguments. */
public final class PrintStatement implements Statement {

    public static final int MAX_ARGUMENTS = 8;
    private final List<DataExpression> arguments;

    public PrintStatement(List<DataExpression> arguments) {
        if (arguments == null || arguments.isEmpty() || arguments.size() > MAX_ARGUMENTS)
            throw new IllegalArgumentException("PRINT requires 1 to 8 arguments");
        this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
    }

    @Override
    public void tick(ProgramContext context) {
        if (!context.tryBeginPrint()) return;
        DataRuntime runtime = context.getDataRuntime();
        DataValue[] values = new DataValue[arguments.size()];
        runtime.allocate(values.length);
        for (int i = 0; i < values.length; i++) values[i] = arguments.get(i)
            .evaluate(runtime);
        context.getDebugConsole()
            .append(DebugValueFormatter.formatPrint(runtime, values));
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("PRINT(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) result.append(", ");
            result.append(arguments.get(i));
        }
        return result.append(')')
            .toString();
    }
}
