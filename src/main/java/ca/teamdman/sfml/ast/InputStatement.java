package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class InputStatement implements Statement {

    private final LabelAccess labelAccess;
    private final ResourceLimits resourceLimits;
    private final boolean each;

    public InputStatement(LabelAccess labelAccess, ResourceLimits resourceLimits, boolean each) {
        this.labelAccess = labelAccess;
        this.resourceLimits = resourceLimits != null ? resourceLimits : ResourceLimits.DEFAULT;
        this.each = each;
    }

    @Override
    public void tick(ProgramContext context) {
        context.addInputStatement(this);
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
}
