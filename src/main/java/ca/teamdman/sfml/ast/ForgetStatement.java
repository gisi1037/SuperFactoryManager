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
        if (context.getManager() != null && context.getManager().getLogLevel().includes(ca.teamdman.sfm.common.tileentity.LogLevel.DEBUG)) {
            context.log(ca.teamdman.sfm.common.tileentity.LogLevel.DEBUG, toString());
        }
        if (labels.isEmpty()) {
            context.clearAllInputs();
        } else {
            for (Label label : labels) {
                context.clearInputsForLabel(label.getName());
            }
        }
    }

    @Override
    public String toString() {
        if (labels.isEmpty()) return "FORGET ALL";
        StringBuilder sb = new StringBuilder("FORGET ");
        for (int i = 0; i < labels.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(labels.get(i).getName());
        }
        return sb.toString();
    }

    public List<Label> getLabels() {
        return labels;
    }
}
