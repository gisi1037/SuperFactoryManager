package ca.teamdman.sfml.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class Block implements ASTNode {

    private final List<Statement> statements;

    public Block(List<Statement> statements) {
        this.statements = statements != null ? statements : Collections.emptyList();
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
                if (context.isInLoop()) runtime.work(1);
                flow.command(statement, () -> {
                    statement.tick(context);
                    return null;
                });
                cursor.index++;
            }
            flow.finish(this);
            return;
        }
        ca.teamdman.sfml.data.DataRuntime loopRuntime = context.isInLoop() ? context.getDataRuntime() : null;
        for (Statement stmt : statements) {
            // Bound repeated legacy statements too, without imposing new work cutoffs outside loops.
            if (loopRuntime != null) loopRuntime.work(1);
            stmt.tick(context);
        }
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
