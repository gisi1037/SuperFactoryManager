package ca.teamdman.sfml.ast;

import java.util.Objects;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.LabelReference;

public final class Label implements ASTNode {

    private final LabelReference reference;
    private final DataExpression expression;

    public Label(String name) {
        this(LabelReference.literal(name));
    }

    public Label(LabelReference reference) {
        this.reference = Objects.requireNonNull(reference, "reference");
        expression = null;
    }

    public Label(DataExpression expression) {
        if (expression == null || expression.getType() != DataType.LABEL_REF)
            throw new IllegalArgumentException("Label access requires LABEL_REF; quote a literal label name");
        this.expression = expression;
        reference = null;
    }

    public boolean isDynamic() {
        return expression != null;
    }

    public Label resolve(ProgramContext context) {
        if (expression == null) return this;
        ca.teamdman.sfml.data.DataRuntime runtime = context.getDataRuntime();
        DataValue value = runtime.evaluate(expression, () -> expression.evaluate(runtime));
        return new Label(value.asLabel());
    }

    public LabelReference getReference() {
        if (reference == null) throw new IllegalStateException("Label access requires an execution snapshot");
        return reference;
    }

    public String name() {
        return getName();
    }

    public String getName() {
        return getReference().getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Label that)) return false;
        return expression == that.expression
            && (reference == that.reference || reference != null && that.reference != null
                && reference.getName()
                    .equals(that.reference.getName())
                && reference.getSideMask() == that.reference.getSideMask()
                && reference.getFirstSlot() == that.reference.getFirstSlot()
                && reference.getLastSlot() == that.reference.getLastSlot()
                && reference.hasSlotSelection() == that.reference.hasSlotSelection());
    }

    @Override
    public int hashCode() {
        return expression != null ? System.identityHashCode(expression)
            : Objects.hash(
                reference.getName(),
                reference.getSideMask(),
                reference.getFirstSlot(),
                reference.getLastSlot(),
                reference.hasSlotSelection());
    }

    @Override
    public String toString() {
        if (expression != null) return expression.toString();
        return reference.getSideMask() == 63 && !reference.hasSlotSelection() ? DataValue.string(reference.getName())
            .toString() : reference.toString();
    }
}
