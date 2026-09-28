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
        for (Statement stmt : statements) {
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
