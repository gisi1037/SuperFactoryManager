package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public interface BoolExpr extends ASTNode {

    boolean evaluate(ProgramContext context);
}
