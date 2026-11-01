package ca.teamdman.sfml.ast;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class Program implements Statement {

    private final ca.teamdman.sfml.data.ProgramStateSchema stateSchema;
    private final boolean dataFeatures;
    private final boolean waitFeatures;
    private final boolean dynamicLabels;
    private final boolean runtimeTimerOffsets;
    private final int localCount;
    private final String name;
    private final String linkedSource;
    private final List<Trigger> triggers;
    private final Set<String> referencedLabels;
    private final List<ca.teamdman.sfml.data.CompiledFunction> functions;
    private final List<NamedRedstoneTrigger> namedRedstoneTriggers;
    private final List<ca.teamdman.sfml.data.GlobalDeclaration> globals;

    public Program(String name, List<Trigger> triggers, Set<String> referencedLabels) {
        this(name, triggers, referencedLabels, ca.teamdman.sfml.data.ProgramStateSchema.EMPTY, false, 0);
    }

    public Program(String name, List<Trigger> triggers, Set<String> referencedLabels,
        ca.teamdman.sfml.data.ProgramStateSchema stateSchema, boolean dataFeatures, int localCount) {
        this(name, triggers, referencedLabels, stateSchema, dataFeatures, localCount, Collections.emptyList());
    }

    public Program(String name, List<Trigger> triggers, Set<String> referencedLabels,
        ca.teamdman.sfml.data.ProgramStateSchema stateSchema, boolean dataFeatures, int localCount,
        List<ca.teamdman.sfml.data.CompiledFunction> functions) {
        this(
            name,
            triggers,
            referencedLabels,
            stateSchema,
            dataFeatures,
            localCount,
            functions,
            Collections.emptyList());
    }

    public Program(String name, List<Trigger> triggers, Set<String> referencedLabels,
        ca.teamdman.sfml.data.ProgramStateSchema stateSchema, boolean dataFeatures, int localCount,
        List<ca.teamdman.sfml.data.CompiledFunction> functions, List<ca.teamdman.sfml.data.GlobalDeclaration> globals) {
        this(name, triggers, referencedLabels, stateSchema, dataFeatures, localCount, functions, globals, false);
    }

    public Program(String name, List<Trigger> triggers, Set<String> referencedLabels,
        ca.teamdman.sfml.data.ProgramStateSchema stateSchema, boolean dataFeatures, int localCount,
        List<ca.teamdman.sfml.data.CompiledFunction> functions, List<ca.teamdman.sfml.data.GlobalDeclaration> globals,
        boolean waitFeatures) {
        this(
            name,
            triggers,
            referencedLabels,
            stateSchema,
            dataFeatures,
            localCount,
            functions,
            globals,
            waitFeatures,
            false);
    }

    public Program(String name, List<Trigger> triggers, Set<String> referencedLabels,
        ca.teamdman.sfml.data.ProgramStateSchema stateSchema, boolean dataFeatures, int localCount,
        List<ca.teamdman.sfml.data.CompiledFunction> functions, List<ca.teamdman.sfml.data.GlobalDeclaration> globals,
        boolean waitFeatures, boolean dynamicLabels) {
        this(
            name,
            triggers,
            referencedLabels,
            stateSchema,
            dataFeatures,
            localCount,
            functions,
            globals,
            waitFeatures,
            dynamicLabels,
            null);
    }

    public Program(String name, List<Trigger> triggers, Set<String> referencedLabels,
        ca.teamdman.sfml.data.ProgramStateSchema stateSchema, boolean dataFeatures, int localCount,
        List<ca.teamdman.sfml.data.CompiledFunction> functions, List<ca.teamdman.sfml.data.GlobalDeclaration> globals,
        boolean waitFeatures, boolean dynamicLabels, String linkedSource) {
        if (linkedSource != null) ca.teamdman.sfm.common.util.BoundedUtf8.encode(linkedSource, 30000);
        this.linkedSource = linkedSource;
        this.waitFeatures = waitFeatures;
        this.dynamicLabels = dynamicLabels;
        this.globals = Collections.unmodifiableList(new java.util.ArrayList<>(globals));
        this.functions = Collections.unmodifiableList(new java.util.ArrayList<>(functions));
        this.stateSchema = stateSchema;
        this.dataFeatures = dataFeatures;
        this.localCount = localCount;
        this.name = name != null ? name : "";
        this.triggers = triggers != null ? triggers : Collections.emptyList();
        List<NamedRedstoneTrigger> nativeTriggers = new java.util.ArrayList<>();
        boolean timerOffsets = false;
        for (Trigger trigger : this.triggers) {
            if (trigger instanceof NamedRedstoneTrigger) nativeTriggers.add((NamedRedstoneTrigger) trigger);
            if (trigger instanceof TimerTrigger && ((TimerTrigger) trigger).getInterval()
                .hasRuntimeOffset()) timerOffsets = true;
        }
        runtimeTimerOffsets = timerOffsets;
        namedRedstoneTriggers = Collections.unmodifiableList(nativeTriggers);
        this.referencedLabels = referencedLabels != null ? referencedLabels : Collections.emptySet();
    }

    public List<ca.teamdman.sfml.data.CompiledFunction> getFunctions() {
        return functions;
    }

    public List<ca.teamdman.sfml.data.GlobalDeclaration> getGlobalDeclarations() {
        return globals;
    }

    public ca.teamdman.sfml.data.ProgramStateSchema getStateSchema() {
        return stateSchema;
    }

    public boolean hasDataFeatures() {
        return dataFeatures;
    }

    public String getName() {
        return name;
    }

    public List<Trigger> getTriggers() {
        return triggers;
    }

    public Set<String> getReferencedLabels() {
        return referencedLabels;
    }

    public boolean hasDynamicLabels() {
        return dynamicLabels;
    }

    @Override
    public void tick(ProgramContext context) {
        if (dynamicLabels) context.requireDynamicLabels();
        boolean dataStarted = false;
        if (!globals.isEmpty()) {
            ca.teamdman.sfml.data.ProgramGlobals current = context.getProgramGlobals(this);
            if (!current.isInitialized()) {
                context.beginDataInvocation(localCount, stateSchema.size() > 0);
                ca.teamdman.sfml.data.ProgramGlobals candidate = new ca.teamdman.sfml.data.ProgramGlobals(globals);
                context.getDataRuntime()
                    .setGlobals(candidate);
                try {
                    for (ca.teamdman.sfml.data.GlobalDeclaration declaration : globals)
                        declaration.initialize(context.getDataRuntime());
                    candidate.finishInitialization();
                    context.setProgramGlobals(this, candidate);
                } catch (RuntimeException failure) {
                    context.getDataRuntime()
                        .setGlobals(current);
                    throw failure;
                }
                dataStarted = true;
            }
        }
        if (runtimeTimerOffsets) {
            ca.teamdman.sfml.data.ProgramExecution execution = context.getProgramExecution(this);
            if (!execution.hasTimerOffsets()) {
                if (!dataStarted) {
                    context.beginDataInvocation(localCount, stateSchema.size() > 0);
                    dataStarted = true;
                }
                execution.initializeTimerOffsets(this, context.getDataRuntime());
            }
        }
        if (!namedRedstoneTriggers.isEmpty()) {
            if (!dataStarted) for (NamedRedstoneTrigger trigger : namedRedstoneTriggers) {
                if (trigger.getSource()
                    .isDynamic()) {
                    context.beginDataInvocation(localCount, stateSchema.size() > 0);
                    dataStarted = true;
                    break;
                }
            }
            context.prepareNamedRedstoneTriggers(namedRedstoneTriggers);
        }
        if (waitFeatures) {
            context.getProgramExecution(this)
                .tick(this, context, localCount, stateSchema.size() > 0, dataStarted);
            return;
        }
        for (Trigger trigger : triggers) {
            if (!trigger.shouldRun(context)) continue;
            if (dataFeatures) {
                if (!dataStarted) {
                    context.beginDataInvocation(localCount, stateSchema.size() > 0);
                    dataStarted = true;
                }
                context.getDataRuntime()
                    .beginTrigger();
            }
            if (!context.didSomething()) {
                context.setDidSomething(true);
                if (context.getManager() != null && context.getManager()
                    .getLogLevel()
                    .includes(ca.teamdman.sfm.common.tileentity.LogLevel.TRACE)) {
                    context.getManager()
                        .logNetworkDiagnostics(context);
                }
                if (context.getManager() != null && context.getManager()
                    .getLogLevel()
                    .includes(ca.teamdman.sfm.common.tileentity.LogLevel.DEBUG)) {
                    context.log(ca.teamdman.sfm.common.tileentity.LogLevel.DEBUG, "PROGRAM TICK BEGIN");
                }
            }
            if (context.getManager() != null && context.getManager()
                .getLogLevel()
                .includes(ca.teamdman.sfm.common.tileentity.LogLevel.DEBUG)) {
                context.log(
                    ca.teamdman.sfm.common.tileentity.LogLevel.DEBUG,
                    "TRIGGERED FROM " + trigger.toStringCondensed());
            }
            long startNs = System.nanoTime();
            context.clearAllInputs();
            try {
                trigger.runBlock(context);
            } finally {
                context.clearAllInputs();
                if (dataFeatures) context.getDataRuntime()
                    .endTrigger();
            }
            long elapsedNs = System.nanoTime() - startNs;
            double ms = elapsedNs / 1_000_000.0;
            if (context.getManager() != null && context.getManager()
                .getLogLevel()
                .includes(ca.teamdman.sfm.common.tileentity.LogLevel.INFO)) {
                context.log(
                    ca.teamdman.sfm.common.tileentity.LogLevel.INFO,
                    String.format("Program trigger tick took %.2f ms:\n```\n%s\n```", ms, trigger.toString()));
            }
        }
    }

    @Override
    public String toString() {
        if (linkedSource != null) return linkedSource;
        StringBuilder sb = new StringBuilder();
        if (name != null && !name.isEmpty()) {
            sb.append("NAME \"")
                .append(name)
                .append("\"\n");
        }
        for (ca.teamdman.sfml.data.ProgramStateSchema.Declaration declaration : stateSchema.getDeclarations()) {
            sb.append("STATE $")
                .append(declaration.getName())
                .append(" = ")
                .append(declaration.getDefaultValue())
                .append("\n");
        }
        for (ca.teamdman.sfml.data.GlobalDeclaration declaration : globals) sb.append(declaration)
            .append("\n");
        for (ca.teamdman.sfml.data.CompiledFunction function : functions) sb.append(function)
            .append("\n");
        for (Trigger trigger : triggers) {
            sb.append(trigger)
                .append("\n");
        }
        return sb.toString()
            .trim();
    }
}
