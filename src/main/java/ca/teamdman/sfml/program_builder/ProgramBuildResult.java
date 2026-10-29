package ca.teamdman.sfml.program_builder;

import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;

import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfml.ast.Program;

public final class ProgramBuildResult {

    private final boolean success;
    private final Program program;
    private final List<String> errors;
    private final SFMLParser.ProgramContext syntax;
    private final CommonTokenStream tokens;

    private ProgramBuildResult(boolean success, Program program, List<String> errors) {
        this(success, program, errors, null, null);
    }

    private ProgramBuildResult(boolean success, Program program, List<String> errors, SFMLParser.ProgramContext syntax,
        CommonTokenStream tokens) {
        this.success = success;
        this.program = program;
        this.errors = errors != null ? Collections.unmodifiableList(new java.util.ArrayList<>(errors))
            : Collections.emptyList();
        this.syntax = syntax;
        this.tokens = tokens;
    }

    public static ProgramBuildResult success(Program program) {
        return new ProgramBuildResult(true, program, null);
    }

    static ProgramBuildResult success(Program program, SFMLParser.ProgramContext syntax, CommonTokenStream tokens) {
        return new ProgramBuildResult(true, program, null, syntax, tokens);
    }

    /** Authoring-only ANTLR snapshot. Treat as read-only; null for ordinary build() results. */
    public SFMLParser.ProgramContext getSyntax() {
        return syntax;
    }

    /** Authoring-only ANTLR snapshot. Treat as read-only; do not seek or change tokens. */
    public CommonTokenStream getTokens() {
        return tokens;
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
