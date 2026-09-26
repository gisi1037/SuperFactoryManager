package ca.teamdman.sfml.ast;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class Program implements Statement {

    private final String name;
    private final List<Trigger> triggers;
    private final Set<String> referencedLabels;

    public Program(String name, List<Trigger> triggers, Set<String> referencedLabels) {
        this.name = name != null ? name : "";
        this.triggers = triggers != null ? triggers : Collections.emptyList();
        this.referencedLabels = referencedLabels != null ? referencedLabels : Collections.emptySet();
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
        for (Trigger trigger : triggers) {
            trigger.tick(context);
        }
    }
}
