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
        if (context.getManager() != null && context.getManager()
            .getLogLevel()
            .includes(ca.teamdman.sfm.common.tileentity.LogLevel.DEBUG)) {
            context.log(ca.teamdman.sfm.common.tileentity.LogLevel.DEBUG, toString());
        }
        if (labels.isEmpty()) {
            context.clearAllInputs();
        } else {
            // Evaluate the complete list before changing any registered input.
            java.util.List<Label> resolved = new java.util.ArrayList<>(labels.size());
            for (Label label : labels) resolved.add(label.resolve(context));
            for (Label label : resolved) {
                if (label.getReference()
                    .getSideMask() != 63 || label.getReference()
                        .hasSlotSelection())
                    throw new IllegalArgumentException(
                        "FORGET accepts label names only, without side or slot restrictions");
            }
            for (Label label : resolved) context.clearInputsForLabel(label.getName());
        }
    }

    @Override
    public String toString() {
        if (labels.isEmpty()) return "FORGET";
        StringBuilder sb = new StringBuilder("FORGET ");
        for (int i = 0; i < labels.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(labels.get(i));
        }
        return sb.toString();
    }

    public List<Label> getLabels() {
        return labels;
    }
}
