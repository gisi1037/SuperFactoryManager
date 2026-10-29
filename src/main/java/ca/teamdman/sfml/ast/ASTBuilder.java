package ca.teamdman.sfml.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import ca.teamdman.langs.SFMLBaseVisitor;
import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataType;

public class ASTBuilder extends SFMLBaseVisitor<ASTNode> {

    private boolean outputLimits;
    private int loopDepth;
    private final DataCompiler data = new DataCompiler();

    private final Set<String> usedLabels = new HashSet<>();

    public Set<String> getUsedLabels() {
        return usedLabels;
    }

    @Override
    public ASTNode visitProgram(SFMLParser.ProgramContext ctx) {
        String name = "";
        if (ctx.name() != null && ctx.name()
            .string() != null) {
            String raw = ctx.name()
                .string()
                .getText();
            if (raw.length() >= 2 && raw.startsWith("\"") && raw.endsWith("\"")) {
                name = raw.substring(1, raw.length() - 1);
            } else {
                name = raw;
            }
        }

        for (SFMLParser.StateDeclarationContext declaration : ctx.stateDeclaration()) {
            data.declareState(
                declaration.VARIABLE()
                    .getText()
                    .substring(1),
                source(declaration.dataExpression()),
                declaration.getStart()
                    .getLine(),
                declaration.getStart()
                    .getCharPositionInLine());
        }
        new ca.teamdman.sfml.data.FunctionCompiler(data).compile(ctx.functionDefinition());
        for (SFMLParser.DataDeclarationContext declaration : ctx.dataDeclaration()) {
            data.declareGlobal(
                declaration.VARIABLE()
                    .getText()
                    .substring(1),
                source(declaration.dataExpression()),
                declaration.getStart()
                    .getLine(),
                declaration.getStart()
                    .getCharPositionInLine());
        }
        List<Trigger> triggers = new ArrayList<>();
        if (ctx.trigger() != null) {
            for (SFMLParser.TriggerContext tCtx : ctx.trigger()) {
                ASTNode node = visit(tCtx);
                if (node instanceof Trigger) {
                    triggers.add((Trigger) node);
                }
            }
        }

        usedLabels.addAll(data.getReferencedLabels());
        return new Program(
            name,
            triggers,
            usedLabels,
            data.getStateSchema(),
            data.isUsed(),
            data.getMaxLocals(),
            data.getFunctions(),
            data.getGlobalDeclarations());
    }

    @Override
    public ASTNode visitTimerTrigger(SFMLParser.TimerTriggerContext ctx) {
        data.beginTrigger();
        Interval interval = (Interval) visit(ctx.interval());
        Block block = visitBlock(ctx.block());
        return new TimerTrigger(interval, block);
    }

    @Override
    public ASTNode visitPulseTrigger(SFMLParser.PulseTriggerContext ctx) {
        data.beginTrigger();
        Block block = visitBlock(ctx.block());
        return new PulseTrigger(block);
    }

    @Override
    public ASTNode visitNamedRedstoneTrigger(SFMLParser.NamedRedstoneTriggerContext ctx) {
        data.beginTrigger();
        if (ctx.labelAccess()
            .roundrobin() != null
            || ctx.labelAccess()
                .slotqualifier() != null)
            throw new IllegalArgumentException("Named redstone triggers do not accept round robin or slots");
        LabelAccess source = (LabelAccess) visit(ctx.labelAccess());
        return new NamedRedstoneTrigger(source, ctx.FALLING() != null, visitBlock(ctx.block()));
    }

    @Override
    public ASTNode visitIntervalSpace(SFMLParser.IntervalSpaceContext ctx) {
        TerminalNode firstNumNode = ctx.NUMBER(0);
        int ticks = 1;
        if (firstNumNode != null) {
            try {
                ticks = Integer.parseInt(firstNumNode.getText());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Interval out of range", e);
            }
        }
        if (ctx.SECONDS() != null || ctx.SECOND() != null) {
            ticks = Math.multiplyExact(ticks, 20);
        }

        boolean isGlobal = ctx.GLOBAL() != null;
        int offset = 0;
        TerminalNode secondNumNode = ctx.NUMBER(1);
        if (secondNumNode != null) {
            try {
                offset = Integer.parseInt(secondNumNode.getText());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Interval out of range", e);
            }
            if (ctx.SECONDS() != null || ctx.SECOND() != null) {
                offset = Math.multiplyExact(offset, 20);
            }
        }

        if (ticks <= 0) throw new IllegalArgumentException("Interval must be positive");
        return new Interval(ticks, isGlobal, offset);
    }

    @Override
    public ASTNode visitIntervalNoSpace(SFMLParser.IntervalNoSpaceContext ctx) {
        String first = ctx.NUMBER_WITH_G_SUFFIX()
            .getText();
        String digits = first.replaceAll("[^0-9]", "");
        int ticks = 1;
        try {
            ticks = Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Interval out of range", e);
        }

        if (ctx.SECONDS() != null || ctx.SECOND() != null) {
            ticks = Math.multiplyExact(ticks, 20);
        }

        int offset = 0;
        TerminalNode secondNumNode = ctx.NUMBER();
        if (secondNumNode != null) {
            try {
                offset = Integer.parseInt(secondNumNode.getText());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Interval out of range", e);
            }
            if (ctx.SECONDS() != null || ctx.SECOND() != null) {
                offset = Math.multiplyExact(offset, 20);
            }
        }

        if (ticks <= 0) throw new IllegalArgumentException("Interval must be positive");
        return new Interval(ticks, true, offset);
    }

    public Block visitBlock(SFMLParser.BlockContext ctx) {
        if (ctx == null || ctx.statement() == null) {
            return new Block(Collections.emptyList());
        }
        List<Statement> statements = new ArrayList<>();
        data.pushScope();
        try {
            for (SFMLParser.StatementContext sCtx : ctx.statement()) {
                ASTNode node = visit(sCtx);
                if (node instanceof Statement) {
                    statements.add((Statement) node);
                }
            }
        } finally {
            data.popScope();
        }
        return new Block(statements);
    }

    @Override
    public ASTNode visitInputStatement(SFMLParser.InputStatementContext ctx) {
        LabelAccess labelAccess = (LabelAccess) visit(ctx.labelAccess());
        ResourceLimits limits = ctx.inputResourceLimits() != null ? (ResourceLimits) visit(ctx.inputResourceLimits())
            : ResourceLimits.DEFAULT;
        if (ctx.resourceExclusion() != null) {
            ResourceIdSet exclusions = (ResourceIdSet) visit(ctx.resourceExclusion());
            limits = new ResourceLimits(limits.getLimits(), exclusions);
        }
        boolean each = ctx.EACH() != null;
        return new InputStatement(labelAccess, limits, each);
    }

    @Override
    public ASTNode visitRedstoneOutputStatement(SFMLParser.RedstoneOutputStatementContext ctx) {
        // Historic syntax allows two quantities but only the first determines the level.
        // Reject an expression in the ignored position rather than silently skipping its evaluation.
        for (int index = 1; index < ctx.quantity()
            .size(); index++) {
            if (ctx.quantity(index)
                .dataExpression() != null)
                throw new IllegalArgumentException("Dynamic redstone OUTPUT cannot use an ignored second quantity");
        }
        LabelAccess labelAccess = (LabelAccess) visit(ctx.labelAccess());
        ResourceQuantity level = new ResourceQuantity(15, false);
        boolean each = ctx.EACH() != null;
        if (ctx.quantity() != null && !ctx.quantity()
            .isEmpty()) {
            ResourceQuantity q = (ResourceQuantity) visit(ctx.quantity(0));
            if (q != null) {
                level = q;
                if (q.isEach()) {
                    each = true;
                }
            }
        }
        return new RedstoneOutputStatement(labelAccess, level, each);
    }

    @Override
    public ASTNode visitOutputStatement(SFMLParser.OutputStatementContext ctx) {
        LabelAccess labelAccess = (LabelAccess) visit(ctx.labelAccess());
        outputLimits = true;
        ResourceLimits limits;
        try {
            limits = ctx.outputResourceLimits() != null ? (ResourceLimits) visit(ctx.outputResourceLimits())
                : new ResourceLimits(Collections.singletonList(ResourceLimit.DEFAULT_OUTPUT), null);
        } finally {
            outputLimits = false;
        }
        if (ctx.resourceExclusion() != null) {
            ResourceIdSet exclusions = (ResourceIdSet) visit(ctx.resourceExclusion());
            limits = new ResourceLimits(limits.getLimits(), exclusions);
        }
        boolean each = ctx.EACH() != null;
        boolean emptySlotsOnly = ctx.emptyslots() != null;
        return new OutputStatement(labelAccess, limits, each, emptySlotsOnly);
    }

    @Override
    public ASTNode visitIfStatement(SFMLParser.IfStatementContext ctx) {
        List<SFMLParser.BoolexprContext> exprs = ctx.boolexpr();
        List<SFMLParser.BlockContext> blocks = ctx.block();

        // Build chained if-else blocks from back to front
        Block elseBlock = null;
        if (blocks.size() > exprs.size()) {
            elseBlock = visitBlock(blocks.get(blocks.size() - 1));
        }

        Statement currentIf = null;
        for (int i = exprs.size() - 1; i >= 0; i--) {
            BoolExpr condition = (BoolExpr) visit(exprs.get(i));
            Block trueBlock = visitBlock(blocks.get(i));
            Block falseBlock = currentIf != null ? new Block(Collections.singletonList(currentIf)) : elseBlock;
            currentIf = new IfStatement(condition, trueBlock, falseBlock);
        }

        return currentIf != null ? currentIf : new IfStatement(BoolFalse.INSTANCE, new Block(null), null);
    }

    private void beginLoop() {
        if (++loopDepth > 16) throw new IllegalArgumentException("Loop nesting exceeds 16");
        data.markUsed();
        data.pushScope();
    }

    private void endLoop() {
        data.popScope();
        loopDepth--;
    }

    @Override
    public ASTNode visitCountedFor(SFMLParser.CountedForContext ctx) {
        // Resolve the bounds in the surrounding scope before introducing the binder.
        DataExpression first = expression(ctx.dataExpression(0));
        DataExpression last = expression(ctx.dataExpression(1));
        DataExpression step = ctx.STEP() == null ? null : expression(ctx.dataExpression(2));
        if (first.getType() != DataType.INTEGER || last.getType() != DataType.INTEGER
            || step != null && step.getType() != DataType.INTEGER)
            throw new IllegalArgumentException("FOR bounds and STEP require INTEGER values");
        beginLoop();
        try {
            DataCompiler.Binding binding = data.declareReadOnlyLocal(
                ctx.VARIABLE()
                    .getText()
                    .substring(1),
                DataType.INTEGER);
            return new CountedForStatement(binding, first, last, step, visitBlock(ctx.block()));
        } finally {
            endLoop();
        }
    }

    @Override
    public ASTNode visitArrayFor(SFMLParser.ArrayForContext ctx) {
        DataExpression array = expression(ctx.dataExpression());
        if (!array.getType()
            .isSequence()) throw new IllegalArgumentException("FOR IN requires a sequence value");
        beginLoop();
        try {
            DataCompiler.Binding binding = data.declareReadOnlyLocal(
                ctx.VARIABLE()
                    .getText()
                    .substring(1),
                array.getType()
                    .getElementType());
            return new ArrayForStatement(binding, array, visitBlock(ctx.block()));
        } finally {
            endLoop();
        }
    }

    @Override
    public ASTNode visitWhileStatement(SFMLParser.WhileStatementContext ctx) {
        BoolExpr condition = (BoolExpr) visit(ctx.boolexpr());
        beginLoop();
        try {
            return new WhileStatement(condition, visitBlock(ctx.block()));
        } finally {
            endLoop();
        }
    }

    @Override
    public ASTNode visitForgetStatement(SFMLParser.ForgetStatementContext ctx) {
        List<Label> labels = new ArrayList<>();
        if (ctx.label() != null) {
            for (SFMLParser.LabelContext lCtx : ctx.label()) {
                labels.add((Label) visit(lCtx));
            }
        }
        return new ForgetStatement(labels);
    }

    @Override
    public ASTNode visitLabelAccess(SFMLParser.LabelAccessContext ctx) {
        List<Label> labels = new ArrayList<>();
        for (SFMLParser.LabelContext lCtx : ctx.label()) {
            labels.add((Label) visit(lCtx));
        }

        SideQualifier sideQualifier = ctx.sidequalifier() != null ? (SideQualifier) visit(ctx.sidequalifier())
            : SideQualifier.ALL;
        NumberRangeSet slots = ctx.slotqualifier() != null ? (NumberRangeSet) visit(ctx.slotqualifier())
            : NumberRangeSet.ALL;
        RoundRobin roundRobin = ctx.roundrobin() != null ? (RoundRobin) visit(ctx.roundrobin()) : RoundRobin.DISABLED;

        return new LabelAccess(labels, sideQualifier, slots, roundRobin);
    }

    @Override
    public ASTNode visitRawLabel(SFMLParser.RawLabelContext ctx) {
        String name = ctx.getText();
        usedLabels.add(name);
        return new Label(name);
    }

    @Override
    public ASTNode visitStringLabel(SFMLParser.StringLabelContext ctx) {
        String raw = ctx.string()
            .getText();
        String name = raw.length() >= 2 ? raw.substring(1, raw.length() - 1) : raw;
        usedLabels.add(name);
        return new Label(name);
    }

    @Override
    public ASTNode visitEachSide(SFMLParser.EachSideContext ctx) {
        return SideQualifier.ALL;
    }

    @Override
    public ASTNode visitListedSides(SFMLParser.ListedSidesContext ctx) {
        List<Side> sides = new ArrayList<>();
        for (SFMLParser.SideContext sCtx : ctx.side()) {
            sides.add(Side.from(sCtx.getText()));
        }
        return new SideQualifier(sides);
    }

    @Override
    public ASTNode visitSlotqualifier(SFMLParser.SlotqualifierContext ctx) {
        return visit(ctx.rangeset());
    }

    @Override
    public ASTNode visitRangeset(SFMLParser.RangesetContext ctx) {
        List<NumberRange> ranges = new ArrayList<>();
        for (SFMLParser.RangeContext rCtx : ctx.range()) {
            ranges.add((NumberRange) visit(rCtx));
        }
        return new NumberRangeSet(ranges);
    }

    @Override
    public ASTNode visitRange(SFMLParser.RangeContext ctx) {
        List<SFMLParser.NumberContext> nums = ctx.number();
        int min = Integer.parseInt(
            nums.get(0)
                .getText());
        int max = nums.size() > 1 ? Integer.parseInt(
            nums.get(1)
                .getText())
            : min;
        return new NumberRange(min, max);
    }

    @Override
    public ASTNode visitRoundrobin(SFMLParser.RoundrobinContext ctx) {
        if (ctx.BLOCK() != null) {
            return new RoundRobin(RoundRobin.Mode.BY_BLOCK);
        }
        return new RoundRobin(RoundRobin.Mode.BY_LABEL);
    }

    @Override
    public ASTNode visitResourceLimitList(SFMLParser.ResourceLimitListContext ctx) {
        List<ResourceLimit> list = new ArrayList<>();
        for (SFMLParser.ResourceLimitContext rlCtx : ctx.resourceLimit()) {
            list.add((ResourceLimit) visit(rlCtx));
        }
        return new ResourceLimits(list, null);
    }

    @Override
    public ASTNode visitResourceLimit(SFMLParser.ResourceLimitContext ctx) {
        Limit limit = ctx.limit() != null ? (Limit) visit(ctx.limit())
            : new Limit(ResourceQuantity.UNLIMITED, outputLimits ? ResourceQuantity.UNLIMITED : ResourceQuantity.ZERO);
        ResourceIdSet resourceIds = ctx.resourceIdDisjunction() != null
            ? (ResourceIdSet) visit(ctx.resourceIdDisjunction())
            : ResourceIdSet.MATCH_ALL;
        With with = ctx.with() != null ? (With) visit(ctx.with()) : With.ALWAYS_TRUE;

        return new ResourceLimit(resourceIds, limit, with);
    }

    @Override
    public ASTNode visitQuantityRetentionLimit(SFMLParser.QuantityRetentionLimitContext ctx) {
        ResourceQuantity quantity = (ResourceQuantity) visit(ctx.quantity());
        ResourceQuantity retention = (ResourceQuantity) visit(ctx.retention());
        return new Limit(quantity, retention);
    }

    @Override
    public ASTNode visitRetentionLimit(SFMLParser.RetentionLimitContext ctx) {
        ResourceQuantity retention = (ResourceQuantity) visit(ctx.retention());
        return new Limit(ResourceQuantity.UNLIMITED, retention);
    }

    @Override
    public ASTNode visitQuantityLimit(SFMLParser.QuantityLimitContext ctx) {
        ResourceQuantity quantity = (ResourceQuantity) visit(ctx.quantity());
        return new Limit(quantity, outputLimits ? ResourceQuantity.UNLIMITED : ResourceQuantity.ZERO);
    }

    @Override
    public ASTNode visitQuantity(SFMLParser.QuantityContext ctx) {
        boolean each = ctx.EACH() != null;
        return ctx.dataExpression() == null ? new ResourceQuantity(
            Long.parseLong(
                ctx.number()
                    .getText()),
            each) : new ResourceQuantity(expression(ctx.dataExpression()), each);
    }

    @Override
    public ASTNode visitRetention(SFMLParser.RetentionContext ctx) {
        boolean each = ctx.EACH() != null;
        return ctx.dataExpression() == null ? new ResourceQuantity(
            Long.parseLong(
                ctx.number()
                    .getText()),
            each) : new ResourceQuantity(expression(ctx.dataExpression()), each);
    }

    @Override
    public ASTNode visitResourceExclusion(SFMLParser.ResourceExclusionContext ctx) {
        if (ctx == null) return null;
        return visit(ctx.resourceIdList());
    }

    @Override
    public ASTNode visitResourceIdList(SFMLParser.ResourceIdListContext ctx) {
        List<ResourceIdentifier> ids = new ArrayList<>();
        for (SFMLParser.ResourceIdContext rCtx : ctx.resourceId()) {
            ids.add((ResourceIdentifier) visit(rCtx));
        }
        return new ResourceIdSet(ids);
    }

    @Override
    public ASTNode visitResourceIdDisjunction(SFMLParser.ResourceIdDisjunctionContext ctx) {
        List<ResourceIdentifier> ids = new ArrayList<>();
        for (SFMLParser.ResourceIdContext rCtx : ctx.resourceId()) {
            ids.add((ResourceIdentifier) visit(rCtx));
        }
        return new ResourceIdSet(ids);
    }

    @Override
    public ASTNode visitResource(SFMLParser.ResourceContext ctx) {
        return ResourceIdentifier.fromString(ctx.getText());
    }

    @Override
    public ASTNode visitStringResource(SFMLParser.StringResourceContext ctx) {
        return ResourceIdentifier.fromSourceLiteral(
            ctx.string()
                .getText());
    }

    @Override
    public ASTNode visitWith(SFMLParser.WithContext ctx) {
        boolean negated = ctx.WITHOUT() != null;
        TagMatcher matcher = extractTagMatcher(ctx.withClause());
        return new With(matcher, negated);
    }

    private TagMatcher extractTagMatcher(SFMLParser.WithClauseContext ctx) {
        if (ctx instanceof SFMLParser.WithTagContext) {
            SFMLParser.TagMatcherContext tmCtx = ((SFMLParser.WithTagContext) ctx).tagMatcher();
            return new TagMatcher(tmCtx.getText());
        }
        throw new IllegalArgumentException("Compound WITH clauses are not supported by this backport");
    }

    @Override
    public ASTNode visitBooleanTrue(SFMLParser.BooleanTrueContext ctx) {
        return BoolTrue.INSTANCE;
    }

    @Override
    public ASTNode visitBooleanFalse(SFMLParser.BooleanFalseContext ctx) {
        return BoolFalse.INSTANCE;
    }

    @Override
    public ASTNode visitBooleanParen(SFMLParser.BooleanParenContext ctx) {
        return new BoolParen((BoolExpr) visit(ctx.boolexpr()));
    }

    @Override
    public ASTNode visitBooleanNegation(SFMLParser.BooleanNegationContext ctx) {
        return new BoolNegation((BoolExpr) visit(ctx.boolexpr()));
    }

    @Override
    public ASTNode visitBooleanConjunction(SFMLParser.BooleanConjunctionContext ctx) {
        return new BoolConjunction((BoolExpr) visit(ctx.boolexpr(0)), (BoolExpr) visit(ctx.boolexpr(1)));
    }

    @Override
    public ASTNode visitBooleanDisjunction(SFMLParser.BooleanDisjunctionContext ctx) {
        return new BoolDisjunction((BoolExpr) visit(ctx.boolexpr(0)), (BoolExpr) visit(ctx.boolexpr(1)));
    }

    @Override
    public ASTNode visitBooleanRedstone(SFMLParser.BooleanRedstoneContext ctx) {
        ComparisonOperator op = ctx.comparisonOp() != null ? ComparisonOperator.from(
            ctx.comparisonOp()
                .getText())
            : ComparisonOperator.GREATER;
        if (ctx.dataExpression() != null)
            return new BoolRedstone(op, new ResourceQuantity(expression(ctx.dataExpression()), false));
        int val = ctx.number() != null ? Integer.parseInt(
            ctx.number()
                .getText())
            : 0;
        return new BoolRedstone(op, val);
    }

    @Override
    public ASTNode visitBooleanHas(SFMLParser.BooleanHasContext ctx) {
        SetOperator setOp = ctx.setOp() != null ? SetOperator.from(
            ctx.setOp()
                .getText())
            : SetOperator.OVERALL;
        LabelAccess labelAccess = (LabelAccess) visit(ctx.labelAccess());
        ComparisonOperator compOp = ComparisonOperator.from(
            ctx.comparisonOp()
                .getText());
        ResourceQuantity quantity = ctx.dataExpression() == null ? new ResourceQuantity(
            Long.parseLong(
                ctx.number()
                    .getText()),
            false) : new ResourceQuantity(expression(ctx.dataExpression()), false);
        ResourceIdSet resIds = ctx.resourceIdDisjunction() != null ? (ResourceIdSet) visit(ctx.resourceIdDisjunction())
            : ResourceIdSet.MATCH_ALL;
        With with = ctx.with() != null ? (With) visit(ctx.with()) : With.ALWAYS_TRUE;
        ResourceIdSet except = ctx.resourceIdList() != null ? (ResourceIdSet) visit(ctx.resourceIdList()) : null;

        return new BoolHas(setOp, labelAccess, compOp, quantity, resIds, with, except);
    }

    private static String source(ParserRuleContext context) {
        return context.getStart()
            .getInputStream()
            .getText(
                new org.antlr.v4.runtime.misc.Interval(
                    context.getStart()
                        .getStartIndex(),
                    context.getStop()
                        .getStopIndex()));
    }

    private DataExpression expression(SFMLParser.DataExpressionContext context) {
        return data.compile(
            source(context),
            context.getStart()
                .getLine(),
            context.getStart()
                .getCharPositionInLine());
    }

    @Override
    public ASTNode visitTransferStatement(SFMLParser.TransferStatementContext ctx) {
        List<DataExpression> arguments = new ArrayList<>();
        for (SFMLParser.DataExpressionContext argument : ctx.dataExpression()) arguments.add(expression(argument));
        DataType[] expected = { DataType.LABEL_REF, DataType.LABEL_REF, null, DataType.INTEGER };
        for (int i = 0; i < arguments.size(); i++) {
            DataType actual = arguments.get(i)
                .getType();
            boolean valid = expected[i] == null ? actual == DataType.ITEM_KEY || actual == DataType.RESOURCE_SELECTOR
                : actual.equals(expected[i]);
            if (!valid) throw new IllegalArgumentException(
                "Line " + ctx.getStart()
                    .getLine()
                    + ", Col "
                    + ctx.getStart()
                        .getCharPositionInLine()
                    + ": Invalid TRANSFER argument "
                    + (i + 1));
        }
        return new TransferStatement(
            arguments,
            ctx.VARIABLE() == null ? null
                : data.declareLocal(
                    ctx.VARIABLE()
                        .getText()
                        .substring(1),
                    TransferStatement.RESULT_TYPE));
    }

    @Override
    public ASTNode visitDataDeclaration(SFMLParser.DataDeclarationContext ctx) {
        DataExpression value = expression(ctx.dataExpression());
        return new DataDeclarationStatement(
            data.declareLocal(
                ctx.VARIABLE()
                    .getText()
                    .substring(1),
                value.getType()),
            value);
    }

    @Override
    public ASTNode visitDataAssignment(SFMLParser.DataAssignmentContext ctx) {
        DataExpression value = expression(ctx.dataExpression());
        return new DataAssignmentStatement(
            data.assignment(
                source(ctx.dataTarget()),
                value,
                ctx.getStart()
                    .getLine(),
                ctx.getStart()
                    .getCharPositionInLine()));
    }

    @Override
    public ASTNode visitReturnStatement(SFMLParser.ReturnStatementContext ctx) {
        throw new IllegalArgumentException(
            "Line " + ctx.getStart()
                .getLine()
                + ", Col "
                + ctx.getStart()
                    .getCharPositionInLine()
                + ": RETURN is only valid inside a FUNCTION");
    }

    @Override
    public ASTNode visitPrintStatement(SFMLParser.PrintStatementContext ctx) {
        if (ctx.dataExpression()
            .size() > PrintStatement.MAX_ARGUMENTS)
            throw new IllegalArgumentException(
                "Line " + ctx.getStart()
                    .getLine()
                    + ", Col "
                    + ctx.getStart()
                        .getCharPositionInLine()
                    + ": PRINT requires 1 to 8 arguments");
        List<DataExpression> arguments = new ArrayList<>();
        for (SFMLParser.DataExpressionContext argument : ctx.dataExpression()) arguments.add(expression(argument));
        return new PrintStatement(arguments);
    }

    @Override
    public ASTNode visitBooleanData(SFMLParser.BooleanDataContext ctx) {
        return new DataBoolExpr(expression(ctx.dataExpression()));
    }
}
