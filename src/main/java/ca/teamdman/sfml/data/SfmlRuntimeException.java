package ca.teamdman.sfml.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A private runtime diagnostic with source coordinates, never a source excerpt or a replay instruction. */
public final class SfmlRuntimeException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;
    private static final int MAX_DETAIL = 1024, MAX_FUNCTIONS = 16;
    private final SourceLocation location;
    private final String detail;
    private final List<String> functions;

    private SfmlRuntimeException(SourceLocation location, String detail, List<String> functions,
        RuntimeException cause) {
        super(format(location, detail, functions), cause);
        this.location = location;
        this.detail = detail;
        this.functions = Collections.unmodifiableList(new ArrayList<>(functions));
    }

    public int getLine() {
        return location == null ? 0 : location.getLine();
    }

    public int getColumn() {
        return location == null ? 0 : location.getColumn();
    }

    public int getCol() {
        return getColumn();
    }

    public String getStatementKind() {
        return location == null ? "Function" : location.getStatementKind();
    }

    public String getKind() {
        return getStatementKind();
    }

    public SourceLocation getLocation() {
        return location;
    }

    public String getModuleAlias() {
        return location == null ? null : location.getModuleAlias();
    }

    public long getModuleRevision() {
        return location == null ? -1 : location.getModuleRevision();
    }

    public boolean isModuleLocation() {
        return location != null && location.isModuleLocation();
    }

    /** Bounded reason without compiled coordinates or function prefixes. */
    public String getDetail() {
        return detail;
    }

    /** Innermost function first. No arguments, source text, handles, or invocation objects are retained. */
    public List<String> getFunctionContexts() {
        return functions;
    }

    public static SfmlRuntimeException at(SourceLocation location, RuntimeException cause) {
        return at(location, null, cause);
    }

    public static SfmlRuntimeException at(SourceLocation location, String message, RuntimeException cause) {
        if (cause instanceof Continuation.Pause) throw cause;
        if (cause instanceof SfmlRuntimeException) {
            SfmlRuntimeException existing = (SfmlRuntimeException) cause;
            if (existing.location != null) return existing;
            return new SfmlRuntimeException(location, existing.detail, existing.functions, existing);
        }
        String detail = bounded(
            message == null ? cause.getMessage() : message,
            MAX_DETAIL,
            cause.getClass()
                .getSimpleName());
        return new SfmlRuntimeException(location, detail, Collections.emptyList(), cause);
    }

    public static SfmlRuntimeException inFunction(String name, IllegalArgumentException cause) {
        SfmlRuntimeException existing = cause instanceof SfmlRuntimeException ? (SfmlRuntimeException) cause : null;
        List<String> functions = new ArrayList<>(existing == null ? Collections.emptyList() : existing.functions);
        if (functions.size() < MAX_FUNCTIONS) functions.add(bounded(name, 128, "function"));
        String detail = existing == null ? bounded(
            cause.getMessage(),
            MAX_DETAIL,
            cause.getClass()
                .getSimpleName())
            : existing.detail;
        return new SfmlRuntimeException(existing == null ? null : existing.location, detail, functions, cause);
    }

    private static String format(SourceLocation location, String detail, List<String> functions) {
        StringBuilder text = new StringBuilder();
        if (location != null && location.isModuleLocation()) text.append("Library ")
            .append(location.getModuleAlias())
            .append(" revision ")
            .append(location.getModuleRevision())
            .append(" | ");
        if (location != null) text.append("Line ")
            .append(location.getLine())
            .append(", Col ")
            .append(location.getColumn())
            .append(": [")
            .append(location.getStatementKind())
            .append("] ");
        for (int i = functions.size() - 1; i >= 0; i--) text.append("In function ")
            .append(functions.get(i))
            .append(": ");
        return text.append(detail)
            .toString();
    }

    static String bounded(String text, int limit, String fallback) {
        if (text == null || text.isEmpty()) return fallback;
        int end = Math.min(text.length(), limit);
        StringBuilder cleaned = new StringBuilder(end);
        for (int i = 0; i < end; i++) {
            char character = text.charAt(i);
            cleaned.append(Character.isISOControl(character) ? ' ' : character);
        }
        return cleaned.toString();
    }
}
