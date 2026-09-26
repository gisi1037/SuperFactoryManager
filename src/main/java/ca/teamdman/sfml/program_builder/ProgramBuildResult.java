package ca.teamdman.sfml.program_builder;

import java.util.Collections;
import java.util.List;

import ca.teamdman.sfml.ast.Program;

public final class ProgramBuildResult {

    private final boolean success;
    private final Program program;
    private final List<String> errors;

    private ProgramBuildResult(boolean success, Program program, List<String> errors) {
        this.success = success;
        this.program = program;
        this.errors = errors != null ? errors : Collections.emptyList();
    }

    public static ProgramBuildResult success(Program program) {
        return new ProgramBuildResult(true, program, null);
    }

    public static ProgramBuildResult failure(List<String> errors) {
        return new ProgramBuildResult(false, null, errors);
    }

    public boolean isSuccess() {
        return success;
    }

    public Program getProgram() {
        return program;
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getErrorMessage() {
        if (errors.isEmpty()) return "";
        return String.join("\n", errors);
    }
}
