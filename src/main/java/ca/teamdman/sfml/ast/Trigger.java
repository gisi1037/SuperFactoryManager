package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public interface Trigger extends ASTNode {

    Block getBlock();

    boolean shouldRun(ProgramContext context);

    String toStringCondensed();

    default void tick(ProgramContext context) {
        if (shouldRun(context)) {
            runBlock(context);
        }
    }

    /** Finish this activation only; the scheduler still decides when this trigger runs again. */
    default void runBlock(ProgramContext context) {
        try {
            getBlock().tick(context);
        } catch (ca.teamdman.sfml.data.LoopBreak done) {
            // Loops consume their own BREAK first. No completed effects are rolled back.
        }
    }
}
