package ca.teamdman.sfml.data;

public interface DataExpression {

    DataType getType();

    DataValue evaluate(DataRuntime runtime);
}
