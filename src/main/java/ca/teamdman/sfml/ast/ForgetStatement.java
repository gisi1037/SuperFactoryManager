package ca.teamdman.sfml.ast;

import java.util.Collections;
import java.util.List;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class ForgetStatement implements Statement {

    private final List<Label> labels;

    public ForgetStatement(List<Label> labels) {
        this.labels = labels != null ? labels : Collections.emptyList();
    }

    @Override
    public void tick(ProgramContext context) {
        if (labels.isEmpty()) {
            context.clearAllInputs();
        } else {
            for (Label label : labels) {
                context.clearInputsForLabel(label.getName());
            }
        }
    }

    public List<Label> getLabels() {
        return labels;
    }
}
