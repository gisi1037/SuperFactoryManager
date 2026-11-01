package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.LoopBreak;

public enum BreakStatement implements Statement {

    INSTANCE;

    @Override
    public void tick(ProgramContext context) {
        throw LoopBreak.INSTANCE;
    }

    @Override
    public String toString() {
        return "BREAK";
    }
}
