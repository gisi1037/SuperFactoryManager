package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class OutputStatement implements Statement {

    private final LabelAccess labelAccess;
    private final ResourceLimits resourceLimits;
    private final boolean each;
    private final boolean emptySlotsOnly;

    public OutputStatement(LabelAccess labelAccess, ResourceLimits resourceLimits, boolean each,
        boolean emptySlotsOnly) {
        this.labelAccess = labelAccess;
        this.resourceLimits = resourceLimits != null ? resourceLimits : ResourceLimits.DEFAULT;
        this.each = each;
        this.emptySlotsOnly = emptySlotsOnly;
    }

    @Override
    public void tick(ProgramContext context) {
        if (context.getManager() != null && context.getManager().getLogLevel().includes(ca.teamdman.sfm.common.tileentity.LogLevel.DEBUG)) {
            context.log(ca.teamdman.sfm.common.tileentity.LogLevel.DEBUG, toString());
        }
        context.executeOutput(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OUTPUT ");
        if (resourceLimits != null) {
            String limStr = resourceLimits.toString();
            if (!limStr.isEmpty()) {
                sb.append(limStr).append(" ");
            }
        }
        sb.append("TO ");
        if (each) {
            sb.append("EACH ");
        }
        sb.append(labelAccess);
        return sb.toString();
    }

    public String toStringPretty() {
        return toString();
    }

    public LabelAccess getLabelAccess() {
        return labelAccess;
    }

    public ResourceLimits getResourceLimits() {
        return resourceLimits;
    }

    public boolean isEach() {
        return each;
    }

    public boolean isEmptySlotsOnly() {
        return emptySlotsOnly;
    }
}
