package ca.teamdman.sfm.client.blueprint;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioIoAllowances.Part;

/** Heap-only pending fields, owned by one exact project/node/source. Applying returns text, never mutates a graph. */
public final class StudioFieldDraft {

    public final UUID project, node;
    public final String original, source;
    public final Type type;
    private final Map<String, String> values = new LinkedHashMap<>(), initial = new LinkedHashMap<>();
    private StudioLabelRows labels;
    private final StudioLabelRows initialLabels;
    private StudioOreFilter oreFilter;
    private final StudioOreFilter initialOreFilter;
    private StudioExclusionRows exclusions;
    private final StudioExclusionRows initialExclusions;
    private StudioResourceAlternatives alternatives;
    private final StudioResourceAlternatives initialAlternatives;
    private StudioLimitRows limits;
    private final StudioLimitRows initialLimits;
    private final boolean singleLimitEligible;
    private boolean promotedLimit;
    private StudioIoAllowances ioAllowances;
    private final StudioIoAllowances initialIoAllowances;
    private StudioPrintArguments printArguments;
    private final StudioPrintArguments initialPrintArguments;
    private StudioTransferArguments transferArguments;
    private final StudioTransferArguments initialTransferArguments;

    public StudioFieldDraft(UUID project, Node node, String source, List<StudioNodeProperties.Field> fields) {
        if (project == null || node == null || source == null)
            throw new IllegalArgumentException("Missing field owner");
        this.project = project;
        this.node = node.id;
        this.type = node.type;
        original = node.clause();
        this.source = source;
        for (StudioNodeProperties.Field field : fields) initial.put(field.getKey(), field.getValue());
        values.putAll(initial);
        if ((type == Type.INPUT || type == Type.OUTPUT
            || type == Type.IF
            || type == Type.REDSTONE
            || type == Type.NAMED_REDSTONE) && initial.containsKey("label"))
            labels = new StudioLabelRows(initial.get("label"));
        initialLabels = labels == null ? null : new StudioLabelRows(labels);
        oreFilter = initial.containsKey("oreFilter") ? new StudioOreFilter(initial.get("oreFilter")) : null;
        initialOreFilter = oreFilter == null ? null : new StudioOreFilter(oreFilter);
        exclusions = initial.containsKey("exclusion") ? new StudioExclusionRows(initial.get("exclusion")) : null;
        initialExclusions = exclusions == null ? null : new StudioExclusionRows(exclusions);
        alternatives = (type == Type.INPUT || type == Type.OUTPUT || type == Type.IF) && initial.containsKey("resource")
            ? new StudioResourceAlternatives(initial.get("resource"))
            : null;
        initialAlternatives = alternatives == null ? null : new StudioResourceAlternatives(alternatives);
        limits = initial.containsKey("limits") ? new StudioLimitRows(initial.get("limits")) : null;
        initialLimits = limits == null ? null : new StudioLimitRows(limits);
        boolean eligible = false;
        StudioIoAllowances numbers = null;
        if ((type == Type.INPUT || type == Type.OUTPUT) && alternatives != null && oreFilter != null) {
            try {
                if (StudioNodeProperties.hasAbsentIoLimit(type, original)) {
                    numbers = StudioIoAllowances.absent();
                } else {
                    numbers = new StudioIoAllowances(StudioNodeProperties.singleLimitText(type, original));
                    eligible = true;
                }
            } catch (IllegalArgumentException manual) {
                // Unsafe and unsupported forms keep their existing controls/manual fallback.
            }
        }
        singleLimitEligible = eligible;
        ioAllowances = numbers;
        initialIoAllowances = ioAllowances == null ? null : new StudioIoAllowances(ioAllowances);
        if (type == Type.PRINT && !fields.isEmpty()) {
            try {
                printArguments = new StudioPrintArguments(original);
            } catch (IllegalArgumentException manual) {
                // Unsafe/unsupported Print clauses remain intact for manual editing.
            }
        }
        initialPrintArguments = printArguments == null ? null : new StudioPrintArguments(printArguments);
        if (type == Type.TRANSFER && !fields.isEmpty()) {
            try {
                transferArguments = new StudioTransferArguments(original);
            } catch (IllegalArgumentException manual) {
                // Preserve unsupported/over-budget clauses for Text editing.
            }
        }
        initialTransferArguments = transferArguments == null ? null : new StudioTransferArguments(transferArguments);
    }

    public StudioFieldDraft(StudioFieldDraft other) {
        project = other.project;
        node = other.node;
        type = other.type;
        original = other.original;
        source = other.source;
        values.putAll(other.values);
        initial.putAll(other.initial);
        labels = other.labels == null ? null : new StudioLabelRows(other.labels);
        initialLabels = other.initialLabels == null ? null : new StudioLabelRows(other.initialLabels);
        oreFilter = other.oreFilter == null ? null : new StudioOreFilter(other.oreFilter);
        initialOreFilter = other.initialOreFilter == null ? null : new StudioOreFilter(other.initialOreFilter);
        exclusions = other.exclusions == null ? null : new StudioExclusionRows(other.exclusions);
        initialExclusions = other.initialExclusions == null ? null : new StudioExclusionRows(other.initialExclusions);
        alternatives = other.alternatives == null ? null : new StudioResourceAlternatives(other.alternatives);
        initialAlternatives = other.initialAlternatives == null ? null
            : new StudioResourceAlternatives(other.initialAlternatives);
        limits = other.limits == null ? null : new StudioLimitRows(other.limits);
        initialLimits = other.initialLimits == null ? null : new StudioLimitRows(other.initialLimits);
        singleLimitEligible = other.singleLimitEligible;
        promotedLimit = other.promotedLimit;
        ioAllowances = other.ioAllowances == null ? null : new StudioIoAllowances(other.ioAllowances);
        initialIoAllowances = other.initialIoAllowances == null ? null
            : new StudioIoAllowances(other.initialIoAllowances);
        printArguments = other.printArguments == null ? null : new StudioPrintArguments(other.printArguments);
        initialPrintArguments = other.initialPrintArguments == null ? null
            : new StudioPrintArguments(other.initialPrintArguments);
        transferArguments = other.transferArguments == null ? null
            : new StudioTransferArguments(other.transferArguments);
        initialTransferArguments = other.initialTransferArguments == null ? null
            : new StudioTransferArguments(other.initialTransferArguments);
    }

    public String value(String key) {
        requireActiveField(key);
        if (!values.containsKey(key)) throw new IllegalArgumentException("Unknown field");
        if (transferArguments != null && StudioTransferArguments.Role.forKey(key) != null)
            return transferArguments.value(transferArguments.alias(StudioTransferArguments.Role.forKey(key)));
        if (printArguments != null && printExpressionKey(key))
            return printArguments.value(printArguments.alias(Integer.parseInt(key.substring(10))));
        return values.get(key);
    }

    public boolean set(String key, String value) {
        requireActiveField(key);
        if (!values.containsKey(key) || value == null || value.length() > BlueprintGraph.MAX_CLAUSE)
            throw new IllegalArgumentException("Unknown or oversized field");
        if (transferArguments != null && StudioTransferArguments.Role.forKey(key) != null)
            return setTransferArgument(transferArguments.alias(StudioTransferArguments.Role.forKey(key)), value);
        if (printArguments != null && printExpressionKey(key))
            return setPrintArgument(printArguments.alias(Integer.parseInt(key.substring(10))), value);
        if (ioAllowances != null && numericLimitField(key)) {
            Part part = key.startsWith("amount") ? Part.AMOUNT : Part.RETAIN;
            boolean changed = key.endsWith("Each") ? ioAllowances.setEachText(part, value)
                : ioAllowances.setLiteral(part, value);
            values.put(key, value);
            return changed;
        }
        if (labels != null && key.equals("label")) {
            StudioLabelRows replacement = new StudioLabelRows(value);
            if (labels.sameContent(replacement)) return false;
            labels = replacement;
            values.put(key, labels.source());
            return true;
        }
        if (limits != null && key.equals("limits")) {
            StudioLimitRows replacement = new StudioLimitRows(value);
            if (limits.sameContent(replacement)) return false;
            limits = replacement;
            values.put(key, limits.source());
            return true;
        }
        if (oreFilter != null && key.equals("oreFilter")) {
            StudioOreFilter replacement = new StudioOreFilter(value);
            if (oreFilter.sameContent(replacement)) return false;
            oreFilter = replacement;
            syncOreFilter();
            return true;
        }
        if (exclusions != null && key.equals("exclusion")) {
            // Keep the old single-selector setter, including invalid pending text and blank removal.
            StudioExclusionRows replacement = new StudioExclusionRows(value.equals(initial.get(key)) ? value : "");
            if (!value.equals(initial.get(key)) && !value.isEmpty()) replacement.append(value);
            if (exclusions.sameContent(replacement)) return false;
            exclusions = replacement;
            values.put(key, exclusions.source());
            return true;
        }
        if (alternatives != null && key.equals("resource")) {
            // Keep the old single-selector setter, including invalid pending text and blank removal.
            StudioResourceAlternatives replacement = new StudioResourceAlternatives(
                value.equals(initial.get(key)) ? value : "");
            if (!value.equals(initial.get(key)) && !value.isEmpty()) replacement.append(value);
            if (alternatives.sameContent(replacement)) return false;
            alternatives = replacement;
            values.put(key, alternatives.source());
            return true;
        }
        if (value.equals(values.get(key))) return false;
        values.put(key, value);
        return true;
    }

    public boolean hasLabelRows() {
        return labels != null;
    }

    public boolean hasLimitRows() {
        return limits != null;
    }

    public boolean hasIoAllowances() {
        return ioAllowances != null;
    }

    public boolean allowanceEnabled(Part part) {
        return ioModel().enabled(part);
    }

    public String allowanceText(Part part) {
        return ioModel().literal(part);
    }

    public boolean allowanceEach(Part part) {
        return ioModel().each(part);
    }

    public boolean setAllowanceEnabled(Part part, boolean enabled) {
        return ioModel().setEnabled(part, enabled);
    }

    public boolean setAllowanceText(Part part, String value) {
        boolean changed = ioModel().setLiteral(part, value);
        String key = part == Part.AMOUNT ? "amount" : "retain";
        if (values.containsKey(key)) values.put(key, value);
        return changed;
    }

    public boolean setAllowanceEach(Part part, boolean each) {
        boolean changed = ioModel().setEach(part, each);
        String key = part == Part.AMOUNT ? "amountEach" : "retainEach";
        if (values.containsKey(key)) values.put(key, Boolean.toString(each));
        return changed;
    }

    public static boolean numericLimitField(String key) {
        return "amount".equals(key) || "amountEach".equals(key) || "retain".equals(key) || "retainEach".equals(key);
    }

    private StudioIoAllowances ioModel() {
        if (ioAllowances == null)
            throw new IllegalArgumentException("Number controls are unavailable or retired. Reopen Fields.");
        return ioAllowances;
    }

    public boolean canAddIndependentLimit() {
        return singleLimitEligible && limits == null;
    }

    /** Copy-and-swap: even successful conversion leaves this draft, its IDs and the graph untouched. */
    public StudioFieldDraft addIndependentLimit(UUID currentProject, Node current, String currentSource) {
        requireCurrent(currentProject, current, currentSource);
        if (!canAddIndependentLimit()) throw new IllegalArgumentException("This limit cannot be converted here");
        // Guard every pending cell before the existing typed validators/full-clause compiler run.
        for (Map.Entry<String, String> field : values.entrySet())
            if (ioAllowances == null || !numericLimitField(field.getKey()))
                StudioLimitRows.requireSafeSyntax(field.getValue());
        String single = StudioNodeProperties
            .singleLimitText(type, appliedClause(currentProject, current, currentSource));
        StudioLimitRows promoted = new StudioLimitRows(single);
        promoted.append(""); // A deliberate blank row must block Apply, never act as a grammar trailing comma.
        StudioFieldDraft replacement = new StudioFieldDraft(this);
        replacement.limits = promoted;
        replacement.values.put("limits", promoted.source());
        replacement.promotedLimit = true;
        replacement.alternatives = null;
        replacement.oreFilter = null;
        replacement.ioAllowances = null;
        return replacement;
    }

    /** In row mode, retired typed controls must not race with the authoritative full-clause text. */
    public boolean retiredLimitField(String key) {
        return promotedLimit && (numericLimitField(key) || "amountEnabled".equals(key)
            || "retainEnabled".equals(key)
            || "resource".equals(key)
            || "oreFilter".equals(key));
    }

    private void requireActiveField(String key) {
        if (retiredLimitField(key)) throw new IllegalArgumentException("Limit controls changed. Reopen Fields.");
        if (retiredPrintField(key))
            throw new IllegalArgumentException("Print argument positions changed. Use rows or reopen Fields.");
        if (retiredTransferField(key))
            throw new IllegalArgumentException("Transfer arguments changed. Use roles or reopen Fields.");
    }

    public boolean hasTransferArguments() {
        return transferArguments != null;
    }

    public List<StudioTransferArguments.Row> transferArgumentRows() {
        return transferModel().rows();
    }

    public int transferArgumentCount() {
        return transferModel().size();
    }

    public UUID transferArgumentRevision() {
        return transferModel().revision();
    }

    public String transferArgumentValue(UUID row) {
        return transferModel().value(row);
    }

    public StudioTransferArguments.Role transferArgumentRole(UUID row) {
        return transferModel().role(row);
    }

    public boolean setTransferArgument(UUID row, String value) {
        if (!transferModel().set(row, value)) return false;
        if (!transferArguments.aliasesRetired()) values.put(transferArguments.role(row).key, value);
        return true;
    }

    public UUID addTransferArgument(String value) {
        return transferModel().append(value);
    }

    public void removeTransferArgument(UUID row) {
        transferModel().remove(row);
    }

    public boolean retiredTransferField(String key) {
        return transferArguments != null && transferArguments.aliasesRetired()
            && StudioTransferArguments.Role.forKey(key) != null;
    }

    private StudioTransferArguments transferModel() {
        if (transferArguments == null) throw new IllegalArgumentException("These Transfer arguments require Text");
        return transferArguments;
    }

    public boolean hasPrintArguments() {
        return printArguments != null;
    }

    public List<StudioPrintArguments.Row> printArgumentRows() {
        return printModel().rows();
    }

    public int printArgumentCount() {
        return printModel().size();
    }

    public UUID printArgumentRevision() {
        return printModel().revision();
    }

    public String printArgumentValue(UUID row) {
        return printModel().value(row);
    }

    public boolean setPrintArgument(UUID row, String value) {
        if (!printModel().set(row, value)) return false;
        if (!printArguments.aliasesRetired())
            for (int i = 0; i < printArguments.size(); i++) if (printArguments.alias(i)
                .equals(row)) values.put("expression" + i, value);
        return true;
    }

    public UUID addPrintArgument(String value) {
        return printModel().append(value);
    }

    public void removePrintArgument(UUID row) {
        printModel().remove(row);
    }

    public boolean retiredPrintField(String key) {
        return printArguments != null && printArguments.aliasesRetired() && printExpressionKey(key);
    }

    private static boolean printExpressionKey(String key) {
        if (key == null || !key.startsWith("expression") || key.length() == 10) return false;
        for (int i = 10; i < key.length(); i++) if (key.charAt(i) < '0' || key.charAt(i) > '9') return false;
        return true;
    }

    private StudioPrintArguments printModel() {
        if (printArguments == null) throw new IllegalArgumentException("These Print arguments require Text");
        return printArguments;
    }

    public List<StudioLimitRows.Row> limitRows() {
        return limitModel().rows();
    }

    public int limitCount() {
        return limitModel().size();
    }

    public UUID limitRevision() {
        return limitModel().revision();
    }

    public String limitValue(UUID row) {
        return limitModel().value(row);
    }

    public boolean setLimit(UUID row, String value) {
        if (!limitModel().set(row, value)) return false;
        values.put("limits", limits.source());
        return true;
    }

    public UUID addLimit(String value) {
        UUID row = limitModel().append(value);
        values.put("limits", limits.source());
        return row;
    }

    public void removeLimit(UUID row) {
        limitModel().remove(row);
        values.put("limits", limits.source());
    }

    private StudioLimitRows limitModel() {
        if (limits == null) throw new IllegalArgumentException("These limits require the full clause editor");
        return limits;
    }

    public List<StudioLabelRows.Row> labelRows() {
        return labelModel().rows();
    }

    public int labelCount() {
        return labelModel().size();
    }

    public UUID labelRevision() {
        return labelModel().revision();
    }

    public String labelValue(UUID row) {
        return labelModel().value(row);
    }

    public boolean setLabel(UUID row, String value) {
        if (!labelModel().set(row, value)) return false;
        values.put("label", labels.source());
        return true;
    }

    public UUID addLabel(String value) {
        UUID row = labelModel().append(value);
        values.put("label", labels.source());
        return row;
    }

    public void removeLabel(UUID row) {
        labelModel().remove(row);
        values.put("label", labels.source());
    }

    private StudioLabelRows labelModel() {
        if (labels == null) throw new IllegalArgumentException("This node has no structured label rows");
        return labels;
    }

    public boolean hasExclusionRows() {
        return exclusions != null;
    }

    public List<StudioExclusionRows.Row> exclusionRows() {
        return exclusionModel().rows();
    }

    public int exclusionCount() {
        return exclusionModel().size();
    }

    public UUID exclusionRevision() {
        return exclusionModel().revision();
    }

    public String exclusionValue(UUID row) {
        return exclusionModel().value(row);
    }

    public boolean setExclusion(UUID row, String value) {
        if (!exclusionModel().set(row, value)) return false;
        values.put("exclusion", exclusions.source());
        return true;
    }

    public UUID addExclusion(String value) {
        UUID row = exclusionModel().append(value);
        values.put("exclusion", exclusions.source());
        return row;
    }

    public void removeExclusion(UUID row) {
        exclusionModel().remove(row);
        values.put("exclusion", exclusions.source());
    }

    private StudioExclusionRows exclusionModel() {
        if (exclusions == null) throw new IllegalArgumentException("These exclusions require the full clause editor");
        return exclusions;
    }

    public boolean hasResourceRows() {
        return alternatives != null;
    }

    public List<StudioResourceAlternatives.Row> resourceRows() {
        return alternativeModel().rows();
    }

    public int resourceCount() {
        return alternativeModel().size();
    }

    public UUID resourceRevision() {
        return alternativeModel().revision();
    }

    public String resourceValue(UUID row) {
        return alternativeModel().value(row);
    }

    public boolean setResource(UUID row, String value) {
        if (!alternativeModel().set(row, value)) return false;
        values.put("resource", alternatives.source());
        return true;
    }

    public UUID addResource(String value) {
        UUID row = alternativeModel().append(value);
        values.put("resource", alternatives.source());
        return row;
    }

    public void removeResource(UUID row) {
        alternativeModel().remove(row);
        values.put("resource", alternatives.source());
    }

    private StudioResourceAlternatives alternativeModel() {
        if (alternatives == null)
            throw new IllegalArgumentException("These alternatives require the full clause editor");
        return alternatives;
    }

    public StudioOreFilter.Mode oreMode() {
        return oreModel().mode();
    }

    public String orePattern() {
        return oreModel().pattern();
    }

    public boolean setOreMode(StudioOreFilter.Mode mode) {
        if (!oreModel().setMode(mode)) return false;
        syncOreFilter();
        return true;
    }

    public boolean setOrePattern(String pattern) {
        if (!oreModel().setPattern(pattern)) return false;
        syncOreFilter();
        return true;
    }

    private StudioOreFilter oreModel() {
        if (oreFilter == null) throw new IllegalArgumentException("This filter requires the full clause editor");
        return oreFilter;
    }

    private void syncOreFilter() {
        // Returning to the original controls must also restore its exact WITH/TAG/# spelling.
        values
            .put("oreFilter", oreFilter.sameContent(initialOreFilter) ? initial.get("oreFilter") : oreFilter.source());
    }

    public boolean dirty() {
        return !values.equals(initial) || labels != null && !labels.sameContent(initialLabels)
            || oreFilter != null && !oreFilter.sameContent(initialOreFilter)
            || exclusions != null && !exclusions.sameContent(initialExclusions)
            || alternatives != null && !alternatives.sameContent(initialAlternatives)
            || limits != null && !limits.sameContent(initialLimits)
            || ioAllowances != null && !ioAllowances.sameContent(initialIoAllowances)
            || printArguments != null && !printArguments.sameContent(initialPrintArguments)
            || transferArguments != null && !transferArguments.sameContent(initialTransferArguments);
    }

    public boolean matches(UUID currentProject, Node current, String currentSource) {
        return project.equals(currentProject) && current != null
            && node.equals(current.id)
            && type == current.type
            && original.equals(current.clause())
            && source.equals(currentSource);
    }

    public void requireCurrent(UUID currentProject, Node current, String currentSource) {
        if (!matches(currentProject, current, currentSource))
            throw new IllegalArgumentException("Project, node or source changed. Cancel and reopen Fields.");
    }

    public String appliedClause(UUID currentProject, Node current, String currentSource) {
        requireCurrent(currentProject, current, currentSource);
        if (transferArguments != null)
            return StudioNodeProperties.updateTransferArguments(type, original, transferArguments);
        if (printArguments != null) return StudioNodeProperties.updatePrintArguments(type, original, printArguments);
        if (ioAllowances != null) ioAllowances.validate();
        if (labels != null) for (StudioLabelRows.Row row : labels.rows()) StudioLabelRows.validateLiteral(row.text);
        if (oreFilter != null) oreFilter.validate();
        if (exclusions != null)
            for (StudioExclusionRows.Row row : exclusions.rows()) StudioExclusionRows.validateLiteral(row.text);
        if (alternatives != null) for (StudioResourceAlternatives.Row row : alternatives.rows())
            StudioResourceAlternatives.validateLiteral(row.text);
        if (limits != null) for (StudioLimitRows.Row row : limits.rows()) StudioLimitRows.validateRow(type, row.text);
        String result = original;
        if (ioAllowances != null && !ioAllowances.sameContent(initialIoAllowances))
            result = singleLimitEligible ? StudioNodeProperties.updateSingleAllowances(type, result, ioAllowances)
                : StudioNodeProperties.updateAbsentAllowances(type, result, ioAllowances);
        for (String key : initial.keySet()) if (!key.equals("oreFilter") && !key.equals("limits")
            && !retiredLimitField(key)
            && !(ioAllowances != null && numericLimitField(key))
            && !initial.get(key)
                .equals(values.get(key)))
            result = key.equals("exclusion") ? StudioNodeProperties.updateExclusions(type, result, values.get(key))
                : key.equals("resource") && alternatives != null
                    ? StudioNodeProperties.updateResourceAlternatives(type, result, values.get(key))
                    : StudioNodeProperties.update(type, result, key, values.get(key));
        // Apply the complete filter last, after resource/quantity edits; no transient mode-only clause is emitted.
        if (oreFilter != null && !initial.get("oreFilter")
            .equals(values.get("oreFilter")))
            result = StudioNodeProperties.update(type, result, "oreFilter", values.get("oreFilter"));
        // Keep the initial row mode through 2→1. Global fields are applied while the old list still exists.
        if (limits != null && (promotedLimit || !initial.get("limits")
            .equals(values.get("limits"))))
            result = promotedLimit ? StudioNodeProperties.promoteSingleLimit(type, result, values.get("limits"))
                : StudioNodeProperties.updateIndependentLimits(type, result, values.get("limits"));
        return result;
    }
}
