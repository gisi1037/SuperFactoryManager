package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.LogLevel;

public final class RedstoneOutputStatement implements Statement {

    private final LabelAccess labelAccess;
    private final ResourceQuantity level;
    private final boolean each;

    public RedstoneOutputStatement(LabelAccess labelAccess, int level, boolean each) {
        this(labelAccess, new ResourceQuantity(Math.max(0, Math.min(15, level)), false), each);
    }

    public RedstoneOutputStatement(LabelAccess labelAccess, ResourceQuantity level, boolean each) {
        if (level == null) throw new IllegalArgumentException("Redstone OUTPUT requires a level");
        this.labelAccess = labelAccess;
        this.level = level.isDynamic() ? level
            : new ResourceQuantity(Math.max(0, Math.min(15, level.getAmount())), false);
        this.each = each;
    }

    public LabelAccess getLabelAccess() {
        return labelAccess;
    }

    public int getLevel() {
        return (int) level.getAmount();
    }

    public boolean isDynamic() {
        return level.isDynamic();
    }

    public int evaluateLevel(ProgramContext context) {
        long strength = level.evaluate(context);
        if (strength > 15) throw new IllegalArgumentException("Dynamic redstone OUTPUT level must be between 0 and 15");
        return (int) strength;
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
