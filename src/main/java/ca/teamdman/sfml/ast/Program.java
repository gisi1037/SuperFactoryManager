package ca.teamdman.sfml.ast;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class Program implements Statement {

    private final ca.teamdman.sfml.data.ProgramStateSchema stateSchema;
    private final boolean dataFeatures;
    private final int localCount;
    private final String name;
    private final List<Trigger> triggers;
    private final Set<String> referencedLabels;

    public Program(String name, List<Trigger> triggers, Set<String> referencedLabels) {
        this(name, triggers, referencedLabels, ca.teamdman.sfml.data.ProgramStateSchema.EMPTY, false, 0);
    }

    public Program(String name, List<Trigger> triggers, Set<String> referencedLabels,
        ca.teamdman.sfml.data.ProgramStateSchema stateSchema, boolean dataFeatures, int localCount) {
        this.stateSchema = stateSchema;
        this.dataFeatures = dataFeatures;
        this.localCount = localCount;
        this.name = name != null ? name : "";
        this.triggers = triggers != null ? triggers : Collections.emptyList();
        this.referencedLabels = referencedLabels != null ? referencedLabels : Collections.emptySet();
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

    @Override
    public void tick(ProgramContext context) {
        boolean dataStarted = false;
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
                trigger.getBlock()
                    .tick(context);
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
        for (Trigger trigger : triggers) {
            sb.append(trigger)
                .append("\n");
        }
        return sb.toString()
            .trim();
    }
}
