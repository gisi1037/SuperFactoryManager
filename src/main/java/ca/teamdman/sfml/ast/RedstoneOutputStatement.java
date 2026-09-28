package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.LogLevel;

public final class RedstoneOutputStatement implements Statement {

    private final LabelAccess labelAccess;
    private final int level;
    private final boolean each;

    public RedstoneOutputStatement(LabelAccess labelAccess, int level, boolean each) {
        this.labelAccess = labelAccess;
        this.level = Math.max(0, Math.min(15, level));
        this.each = each;
    }

    public LabelAccess getLabelAccess() {
        return labelAccess;
    }

    public int getLevel() {
        return level;
    }

    public boolean isEach() {
        return each;
    }

    @Override
    public void tick(ProgramContext context) {
        if (context.getManager() != null && context.getManager()
            .getLogLevel()
            .includes(LogLevel.DEBUG)) {
            context.log(LogLevel.DEBUG, toString());
        }
        context.executeRedstoneOutput(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OUTPUT ");
        sb.append(level);
        sb.append(" REDSTONE LEVEL TO ");
        if (each) {
            sb.append("EACH ");
        }
        sb.append(labelAccess);
        return sb.toString();
    }
}
