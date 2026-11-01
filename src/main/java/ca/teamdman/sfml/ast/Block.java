package ca.teamdman.sfml.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.SourceLocation;

public final class Block implements ASTNode {

    private final List<Statement> statements;
    private final List<SourceLocation> locations;

    public Block(List<Statement> statements) {
        this(statements, null);
    }

    public Block(List<Statement> statements, List<SourceLocation> locations) {
        this.statements = statements != null ? statements : Collections.emptyList();
        if (locations != null && locations.size() != this.statements.size())
            throw new IllegalArgumentException("Statement source annotation count mismatch");
        this.locations = locations == null ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<>(locations));
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
                        statement.tick(context);
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
                statements.get(index)
                    .tick(context);
            } catch (RuntimeException failure) {
                throw located(index, failure);
            }
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
