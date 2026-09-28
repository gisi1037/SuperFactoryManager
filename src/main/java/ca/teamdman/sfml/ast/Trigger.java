package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public interface Trigger extends ASTNode {

    Block getBlock();

    boolean shouldRun(ProgramContext context);

    String toStringCondensed();

    default void tick(ProgramContext context) {
        if (shouldRun(context)) {
            getBlock().tick(context);
        }
    }
}
