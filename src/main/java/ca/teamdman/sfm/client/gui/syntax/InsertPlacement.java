package ca.teamdman.sfm.client.gui.syntax;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;

/** Conservative syntax placement only, never a substitute for compiler/type validation. Parse once per browser open. */
public final class InsertPlacement {

    private boolean known, body, function, valueFunction, loop, declarationsAllowed = true;

    public static InsertPlacement at(String source, int caret) {
        InsertPlacement place = new InsertPlacement();
        if (source.trim()
            .isEmpty()) {
            place.known = true;
            return place;
        }
        ca.teamdman.sfml.program_builder.ProgramBuildResult parsed = ca.teamdman.sfml.program_builder.ProgramBuilder
            .parseSyntax(source);
        if (!parsed.isSuccess()) return place;
        SFMLParser.ProgramContext program = parsed.getSyntax();
        place.known = true;
        int offset = source.codePointCount(0, Math.max(0, Math.min(caret, source.length())));
        for (SFMLParser.FunctionDefinitionContext definition : program.functionDefinition())
            if (offset > definition.getStart()
                .getStartIndex()) place.declarationsAllowed = false;
        for (SFMLParser.TriggerContext trigger : program.trigger()) if (offset > trigger.getStart()
            .getStartIndex()) place.declarationsAllowed = false;
        for (SFMLParser.FunctionDefinitionContext definition : program.functionDefinition())
            if (inside(definition, offset)) {
                place.body = true;
                place.function = true;
                place.valueFunction = !definition.dataType()
                    .getText()
                    .equalsIgnoreCase("void");
            }
        for (SFMLParser.TriggerContext trigger : program.trigger()) if (inside(trigger, offset)) place.body = true;
        place.loop = containsLoop(program, offset);
        return place;
    }

    public String refusal(InsertCatalog.Form form) {
        if (!known) return "";
        String p = form.placement, id = form.card.id;
        if (p.equals("Top level") && body) return "Insert this declaration outside triggers and functions.";
        if ((id.equals("data_global_let") || id.equals("data_state")) && !declarationsAllowed)
            return "Program-wide declarations belong before all functions and triggers.";
        if ((p.equals("Inside running code") || p.equals("Example block")) && !body)
            return "Place the caret inside a trigger or allowed function body.";
        if (p.equals("Trigger condition") && (!body || function))
            return "This legacy condition belongs inside a trigger's if/while test.";
        if (p.equals("Input/Output option") && !body)
            return "Choose the range inside an Input/Output instruction for this option.";
        if (valueFunction && form.card.example && p.equals("Example block"))
            return "This worked block contains actions; use a trigger or void procedure.";
        if (valueFunction && (form.card.role.equals("transfers") || form.card.role.equals("debug")
            || id.equals("call_void")
            || id.startsWith("call:") && p.equals("Inside running code")))
            return "This action requires a trigger or void procedure.";
        if (id.equals("return_value") && (!function || !valueFunction))
            return "Return a value inside a value-returning function.";
        if (id.equals("return_void") && (!function || valueFunction)) return "Use this return inside a void procedure.";
        if (id.equals("break_loop") && !loop) return "Place this break inside a for/while loop.";
        if (id.equals("break_trigger") && (!body || function || loop))
            return "Place this form in a trigger outside loops/functions.";
        return "";
    }

    public String note() {
        return known ? "Placement checked; replace placeholders, then Check code for syntax and types."
            : "Placement uncertain in this incomplete draft; replace placeholders and Check code.";
    }

    private static boolean inside(ParserRuleContext context, int offset) {
        int start = -1;
        for (int i = 0; i < context.getChildCount(); i++) if (context.getChild(i) instanceof TerminalNode) {
            org.antlr.v4.runtime.Token t = ((TerminalNode) context.getChild(i)).getSymbol();
            if (t.getType() == SFMLLexer.DO || t.getType() == SFMLLexer.THEN || t.getType() == SFMLLexer.LBRACE) {
                start = t.getStopIndex() + 1;
                break;
            }
        }
        return start >= 0 && offset >= start
            && offset <= context.getStop()
                .getStartIndex();
    }

    private static boolean containsLoop(org.antlr.v4.runtime.tree.ParseTree tree, int offset) {
        java.util.Deque<org.antlr.v4.runtime.tree.ParseTree> pending = new java.util.ArrayDeque<>();
        pending.push(tree);
        while (!pending.isEmpty()) {
            org.antlr.v4.runtime.tree.ParseTree next = pending.pop();
            if ((next instanceof SFMLParser.ForStatementContext || next instanceof SFMLParser.WhileStatementContext)
                && inside((ParserRuleContext) next, offset)) return true;
            for (int i = 0; i < next.getChildCount(); i++) pending.push(next.getChild(i));
        }
        return false;
    }
}
