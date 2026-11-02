package ca.teamdman.sfml.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.SourceLocation;

public final class Block implements ASTNode {

    private final List<Statement> statements;
    private final List<SourceLocation> locations;
    private final List<ca.teamdman.sfml.data.DataCompiler.Binding[]> debugLocals;

    public Block(List<Statement> statements) {
        this(statements, null);
    }

    public Block(List<Statement> statements, List<SourceLocation> locations) {
        this(statements, locations, Collections.emptyList());
    }

    public Block(List<Statement> statements, List<SourceLocation> locations,
        List<ca.teamdman.sfml.data.DataCompiler.Binding[]> debugLocals) {
        this.statements = statements != null ? statements : Collections.emptyList();
        if (locations != null && locations.size() != this.statements.size())
            throw new IllegalArgumentException("Statement source annotation count mismatch");
        this.locations = locations == null ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<>(locations));
        this.debugLocals = Collections.unmodifiableList(new ArrayList<>(debugLocals));
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public void tick(ProgramContext context) {
        if (context.isResumable()) {
            ca.teamdman.sfml.data.DataRuntime runtime = context.getDataRuntime();
            ca.teamdman.sfml.data.Continuation flow = runtime.continuation();
            ca.teamdman.sfml.data.Continuation.Progress cursor = flow.progress(this);
            while (cursor.index < statements.size()) {
                Statement statement = statements.get(cursor.index);
                try {
                    if (context.isInLoop()) runtime.work(1);
                    flow.command(statement, () -> {
                        execute(context, statement, cursor.index);
                        return null;
                    });
                } catch (RuntimeException failure) {
                    throw located(cursor.index, failure);
                }
                cursor.index++;
            }
            flow.finish(this);
            return;
        }
        ca.teamdman.sfml.data.DataRuntime loopRuntime = context.isInLoop() ? context.getDataRuntime() : null;
        for (int index = 0; index < statements.size(); index++) {
            // Bound repeated legacy statements too, without imposing new work cutoffs outside loops.
            try {
                if (loopRuntime != null) loopRuntime.work(1);
                execute(context, statements.get(index), index);
            } catch (RuntimeException failure) {
                throw located(index, failure);
            }
        }
    }

    private void execute(ProgramContext context, Statement statement, int index) {
        ca.teamdman.sfm.common.program.ExecutionTrace.Event event = locations.isEmpty() ? null
            : context.beginTrace(locations.get(index));
        String outcome = "completed";
        try {
            statement.tick(context);
        } catch (ca.teamdman.sfml.data.Continuation.Pause pause) {
            outcome = "waiting";
            throw pause;
        } catch (ca.teamdman.sfml.data.LoopBreak stop) {
            outcome = "break";
            throw stop;
        } catch (RuntimeException failure) {
            outcome = "failed";
            throw failure;
        } finally {
            if (event != null) context.finishTrace(
                event,
                outcome,
                context.debugRuntime(),
                debugLocals.isEmpty() ? null : debugLocals.get(index));
        }
    }

    private RuntimeException located(int index, RuntimeException failure) {
        return locations.isEmpty() ? failure
            : locations.get(index)
                .annotate(failure);
    }

    public static Block of(Statement... stmts) {
        List<Statement> list = new ArrayList<>();
        Collections.addAll(list, stmts);
        return new Block(list);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Statement stmt : statements) {
            sb.append(stmt.toString())
                .append("\n");
        }
        return sb.toString()
            .trim();
    }
}
