package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class TimerTrigger implements Trigger {

    private final Interval interval;
    private final Block block;

    public TimerTrigger(Interval interval, Block block) {
        this.interval = interval != null ? interval : new Interval(20, false, 0);
        this.block = block != null ? block : new Block(null);
    }

    public Interval getInterval() {
        return interval;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public boolean shouldRun(ProgramContext context) {
        long currentTick = interval.isGlobal() ? context.getWorldTime() : context.getManagerTick();
        return interval.matches(currentTick);
    }

    @Override
    public String toStringCondensed() {
        return "EVERY " + interval + " DO";
    }

    @Override
    public String toString() {
        return "EVERY " + interval + " DO\n" + block.toString() + "\nEND";
    }
}
