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
        context.executeOutput(this);
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
