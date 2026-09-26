package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class PulseTrigger implements Trigger {

    private final Block block;

    public PulseTrigger(Block block) {
        this.block = block != null ? block : new Block(null);
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public boolean shouldRun(ProgramContext context) {
        return context.hasRedstonePulse();
    }
}
