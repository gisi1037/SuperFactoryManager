package ca.teamdman.sfm.client.gui.mui2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import ca.teamdman.sfm.client.gui.syntax.InsertCatalog;
import ca.teamdman.sfm.client.gui.syntax.InsertCatalog.Card;
import ca.teamdman.sfm.client.gui.syntax.InsertCatalog.Category;
import ca.teamdman.sfm.client.gui.syntax.InsertCatalog.Form;
import ca.teamdman.sfm.client.gui.syntax.InsertCatalog.Match;
import ca.teamdman.sfm.client.gui.syntax.InsertNavigation;
import ca.teamdman.sfm.client.gui.syntax.InsertNavigation.Section;
import ca.teamdman.sfm.client.gui.syntax.InsertPlacement;

/** Native shared category browser. Lists use MUI2 scrolling; browsing never edits or executes the program. */
public final class InsertBrowser {

    public enum Entry {
        ALL,
        ITEMS,
        FLUIDS,
        REDSTONE,
        TEMPLATES
    }

    public interface Host {

        SfmlCodeEditorWidget editor();

        boolean editable();

        Set<String> labels();

        int availableWidth();

        int availableHeight();

        ModularPanel panel(int width, int height, java.util.function.BiPredicate<Character, Integer> keys);

        void focus();

        void status(String text);

        boolean canCreateProject();

        void newExample(String name, String code);

        boolean canPickLibrary();

        void library();

        boolean prepareInsert();
    }

    private final Host host;
    private final IPanelHandler handler;
    private final InsertCatalog catalog = InsertCatalog.INSTANCE;
    private List<Card> cards = Collections.emptyList();
    private List<Match> matches = Collections.emptyList();
    private final List<InsertRow> resultRows = new ArrayList<>();
    private final int[] sectionCounts = new int[Section.values().length];
    private String source = "", query = "", comparison = ">=", message = "";
    private Section section = Section.ITEMS;
    // Narrow screens keep the same category -> insert -> description route, with named Back controls.
    private int pane = 1, start, end;
    private boolean result, notes, usage, syntax, rebuilding;
    private Form selected;
    private TextFieldWidget searchField;
    private ButtonWidget<?> closeButton, backButton, insertButton, clearButton;
    private TextWidget<?> scopeLabel;
    private int availableWidth, availableHeight;
    private InsertPlacement placement;
    private ModularPanel popup;
    private RememberedList filters, results, details;
    private Geometry geometry;

    public InsertBrowser(ModularPanel parent, Host host) {
        this.host = host;
        handler = IPanelHandler.simple(parent, (owner, player) -> build(), true);
    }

    public void open(Entry entry) {
        if (!host.editable() || handler.isPanelOpen()) return;
        section = entry == Entry.FLUIDS ? Section.FLUIDS
            : entry == Entry.REDSTONE ? Section.REDSTONE : entry == Entry.TEMPLATES ? Section.TEMPLATES : Section.ITEMS;
        query = "";
        pane = 1;
        selected = null;
        result = notes = usage = syntax = false;
        SfmlEditorBuffer buffer = host.editor().buffer;
        source = buffer.source();
        start = buffer.start();
        end = buffer.end();
        if (!buffer.selected()) {
            int[] placeholder = ca.teamdman.sfm.client.gui.syntax.SyntaxSnippet.placeholderAt(source, start);
            if (placeholder != null) {
                start = placeholder[0];
                end = placeholder[1];
            }
        }
        placement = InsertPlacement.at(source, start);
        cards = catalog.withContext(source, host.labels());
        // Category counts depend on the captured catalog, never on the global search text.
        for (Section choice : Section.values())
            sectionCounts[choice.ordinal()] = InsertNavigation.search(cards, choice, "")
                .size();
        if (host.editor()
            .isValid())
            host.editor()
                .getContext()
                .removeFocus();
        message = "";
        handler.deleteCachedPanel();
        handler.openPanel();
    }

    private boolean hasQuery() {
        return !query.trim()
            .isEmpty();
    }

    private ModularPanel build() {
        availableWidth = host.availableWidth();
        availableHeight = host.availableHeight();
        geometry = new Geometry(availableWidth, availableHeight);
        popup = host.panel(geometry.width, geometry.height, this::key)
            .onCloseAction(host::focus);
        popup.child(
            new TextWidget<>("Insert code").pos(8, 7)
                .size(130, 11)
                .color(0xffefa09a));
        closeButton = button("Close", geometry.width - 58, 4, 50, handler::closePanel);
        popup.child(closeButton);
        popup.child(
            new TextWidget<>("Find an insert (search all categories)").pos(8, 23)
                .size(geometry.width - 16, 11)
                .color(0xffc6ccd6));
        searchField = new TextFieldWidget().setMaxLength(96)
            .autoUpdateOnChange(true)
            .value(new StringValue.Dynamic(() -> query, value -> {
                query = value;
                selected = null;
                pane = 1;
                changed();
            }))
            .pos(8, 36)
            .size(geometry.width - 82, 18)
            .addTooltipLine("Search names, explanations and named forms, such as empty, heat or divide.");
        popup.child(searchField);
        clearButton = button("Clear", geometry.width - 70, 36, 62, () -> choose(section));
        popup.child(clearButton);
        scopeLabel = new TextWidget<>(IKey.dynamic(this::scope)).pos(8, 58)
            .size(geometry.width - 16, 22)
            .color(0xffc6ccd6);
        popup.child(scopeLabel);
        backButton = button("", 8, 82, geometry.width - 16, this::back).overlay(IKey.dynamic(this::backLabel))
            .setEnabledIf(w -> !geometry.wide || hasQuery());
        popup.child(backButton);
        filters = list(8, geometry.filterWidth, 0);
        results = list(geometry.resultsX, geometry.resultsWidth, 1);
        details = list(geometry.detailsX, geometry.detailsWidth, 2);
        popup.child(filters);
        popup.child(results);
        popup.child(details);
        insertButton = button("", 8, geometry.height - 26, geometry.width - 16, this::insert)
            .background(StudioTheme.PRIMARY)
            .overlay(
                IKey.dynamic(this::insertLabel)
                    .color(() -> canInsert() ? 0xffeeeeee : 0xffa5a0a1));
        popup.child(insertButton);
        changed();
        popup.onUpdateListener(widget -> resizeToScreen(), true);
        return popup;
    }

    private String insertLabel() {
        if (!geometry.wide && pane == 0) return "Choose a category";
        if (selected == null) return "Choose an insert to preview";
        if (selected.placement.equals("New program"))
            return host.canCreateProject() ? "Create new program from template" : "Copy complete program for Studio";
        if (selected.placement.equals("Library picker")) return "Choose a saved library";
        return selected.card.example ? "Insert code sequence at caret" : "Insert code at caret";
    }

    private String backLabel() {
        if (pane == 2) return "Back to " + (hasQuery() ? "search results" : section.name);
        if (hasQuery() || pane == 0) return "Back to " + section.name;
        return "Choose category";
    }

    private void back() {
        if (pane == 2) {
            pane = 1;
            revealSelected();
        } else if (hasQuery()) choose(section);
        else pane = pane == 0 ? 1 : 0;
        popup.scheduleResize();
    }

    private boolean key(char character, int key) {
        if (key == Keyboard.KEY_RETURN && GuiScreen.isCtrlKeyDown()) {
            insert();
            return true;
        }
        if (popup.getContext()
            .isFocused(searchField)) {
            if (key != Keyboard.KEY_RETURN) return false;
            popup.getContext()
                .removeFocus();
            pane = 1;
            return true;
        }
        if ((key == Keyboard.KEY_UP || key == Keyboard.KEY_DOWN) && pane != 0) {
            int index = -1;
            for (int i = 0; i < matches.size(); i++)
                if (selected != null && matches.get(i).card == selected.card) index = i;
            if (!matches.isEmpty()) {
                index = Math.max(0, Math.min(matches.size() - 1, index + (key == Keyboard.KEY_DOWN ? 1 : -1)));
                select(matches.get(index).form, false);
                revealSelected();
            }
            return true;
        }
        if (key == Keyboard.KEY_RETURN && selected != null) {
            pane = 2;
            popup.scheduleResize();
            return true;
        }
        if (key == Keyboard.KEY_BACK) {
            back();
            return true;
        }
        return false;
    }

    private RememberedList list(int x, int width, int page) {
        RememberedList list = new RememberedList();
        list.pos(x, Geometry.CONTENT_TOP)
            .size(width, Math.max(24, geometry.height - Geometry.CONTENT_TOP - 32))
            .background(StudioTheme.INSET)
            .setEnabledIf(w -> geometry.wide || pane == page);
        return list;
    }

    private void resizeToScreen() {
        int width = host.availableWidth(), height = host.availableHeight();
        if (width == availableWidth && height == availableHeight) return;
        availableWidth = width;
        availableHeight = height;
        int fs = filters.getScrollY(), rs = results.getScrollY(), ds = details.getScrollY();
        Form previous = selected;
        geometry = new Geometry(width, height);
        popup.size(geometry.width, geometry.height);
        closeButton.left(geometry.width - 58);
        searchField.width(geometry.width - 82);
        clearButton.left(geometry.width - 70);
        scopeLabel.width(geometry.width - 16);
        backButton.width(geometry.width - 16);
        insertButton.top(geometry.height - 26)
            .width(geometry.width - 16);
        filters.pos(8, Geometry.CONTENT_TOP)
            .size(geometry.filterWidth, geometry.height - Geometry.CONTENT_TOP - 32);
        results.pos(geometry.resultsX, Geometry.CONTENT_TOP)
            .size(geometry.resultsWidth, geometry.height - Geometry.CONTENT_TOP - 32);
        details.pos(geometry.detailsX, Geometry.CONTENT_TOP)
            .size(geometry.detailsWidth, geometry.height - Geometry.CONTENT_TOP - 32);
        rebuildFilters();
        rebuildResults();
        selected = previous;
        rebuildDetails();
        filters.restore = fs;
        results.restore = rs;
        details.restore = ds;
        popup.scheduleResize();
    }

    private void choose(Section choice) {
        section = choice;
        query = "";
        pane = 1;
        selected = null;
        changed();
        popup.scheduleResize();
    }

    private void changed() {
        if (rebuilding || results == null) return;
        rebuilding = true;
        result = notes = usage = syntax = false;
        message = "";
        results.restore = 0;
        details.restore = 0;
        try {
            if (!searchField.getText()
                .equals(query)) searchField.setText(query);
            if (filters.getChildren()
                .isEmpty()) rebuildFilters();
            rebuildResults();
            rebuildDetails();
        } finally {
            rebuilding = false;
        }
    }

    private String scope() {
        return (hasQuery() ? "Search all categories" : section.name) + " | "
            + matches.size()
            + " inserts\nChoose a name to read help. Only Insert changes code.";
    }

    private void rebuildFilters() {
        int scroll = filters.getScrollY();
        filters.removeAll();
        String group = "";
        for (Section choice : Section.values()) {
            if (!group.equals(choice.group)) {
                group = choice.group;
                text(filters, group, geometry.filterWidth, 0xffb9c1cc);
            }
            int count = sectionCounts[choice.ordinal()];
            filters.child(new CategoryRow(choice, count));
        }
        filters.restore = scroll;
        resize(filters);
    }

    private void rebuildResults() {
        matches = InsertNavigation.search(cards, section, query);
        results.removeAll();
        resultRows.clear();
        boolean visible = false;
        for (Match match : matches) if (selected != null && selected.card == match.card) {
            if (hasQuery() || !InsertNavigation.forms(match.card, section, query)
                .contains(selected)) selected = match.form;
            visible = true;
        }
        if (!visible) selected = null;
        text(
            results,
            hasQuery() ? "Matches across every category." : section.description,
            geometry.resultsWidth,
            0xffc6ccd6);
        if (section == Section.NBT && !hasQuery()) text(
            results,
            "Read contents -> take a group's .item key -> check the tag type -> read a value. The guarded heat pattern shows these steps.",
            geometry.resultsWidth,
            0xffbddce0);
        if (matches.isEmpty())
            text(results, "No matches. Try another word or choose a category.", geometry.resultsWidth, 0xffefa09a);
        else {
            text(results, "Colors mark code kind:", geometry.resultsWidth, 0xffb9c1cc);
            Set<String> roles = new LinkedHashSet<>();
            for (Match match : matches) roles.add(match.card.role);
            results.child(new KindLegend(roles, geometry.resultsWidth - 12));
        }
        for (Match match : matches) {
            InsertRow row = new InsertRow(match);
            resultRows.add(row);
            results.child(row);
        }
        resize(results);
    }

    private void select(Form form, boolean showDetails) {
        selected = form;
        result = notes = usage = syntax = false;
        message = "";
        details.restore = 0;
        if (showDetails) pane = 2;
        rebuildDetails();
        popup.scheduleResize();
    }

    private void revealSelected() {
        for (InsertRow row : resultRows) if (selected != null && row.match.card == selected.card) {
            results.reveal = row;
            resize(results);
            break;
        }
    }

    private void rebuildDetails() {
        int scroll = details.restore >= 0 ? details.restore : details.getScrollY();
        details.removeAll();
        if (selected == null) {
            text(details, "Choose an insert", geometry.detailsWidth, 0xffe7e8eb);
            text(
                details,
                "Select a name to read what it does and preview its code. Selecting never changes your draft.",
                geometry.detailsWidth,
                0xffc6ccd6);
            resize(details);
            return;
        }
        Form form = selected;
        Category role = catalog.role(form.card.role);
        text(details, form.card.name, geometry.detailsWidth, 0xffe7e8eb);
        text(
            details,
            InsertNavigation.kind(form.card.role) + " | " + form.placement,
            geometry.detailsWidth,
            role.color);
        if (!message.isEmpty()) text(details, message, geometry.detailsWidth, 0xffefa09a);
        text(details, form.field("summary"), geometry.detailsWidth, 0xffe7e8eb);
        text(details, form.field("details"), geometry.detailsWidth, 0xffc6ccd6);
        text(details, form.field("placementHelp"), geometry.detailsWidth, 0xffe7cb89);
        List<Form> availableForms = InsertNavigation.forms(form.card, section, query);
        List<String> groups = new ArrayList<>();
        for (Form variant : availableForms)
            if (!variant.group.isEmpty() && !groups.contains(variant.group)) groups.add(variant.group);
        for (String group : groups) details.child(row("Form: " + group, geometry.detailsWidth, 0xffe7e8eb, () -> {
            for (Form variant : availableForms) if (variant.group.equals(group)) {
                selected = variant;
                rebuildDetails();
                break;
            }
        }));
        if (availableForms.size() > 1) for (Form variant : availableForms) {
            if (!groups.isEmpty() && !variant.group.equals(form.group)) continue;
            details.child(row((variant == form ? "> " : "") + variant.name, geometry.detailsWidth, 0xffe7e8eb, () -> {
                selected = variant;
                message = "";
                rebuildDetails();
            }));
        }
        if (form.hasComparison()) {
            com.cleanroommc.modularui.widget.ParentWidget<?> comparisons = new com.cleanroommc.modularui.widget.ParentWidget<>()
                .widthRel(1f)
                .height(20);
            String[] choices = { "==", "!=", "<", "<=", ">", ">=" };
            int width = (geometry.detailsWidth - 10) / 6;
            for (int i = 0; i < choices.length; i++) {
                String compare = choices[i];
                comparisons.child(button(compare, 2 + i * width, 0, width - 2, () -> {
                    comparison = compare;
                    rebuildDetails();
                }).overlay(
                    IKey.str(compare)
                        .color(compare.equals(comparison) ? role.color : 0xffeeeeee)));
            }
            details.child(comparisons);
        }
        if (form.hasResult()) details
            .child(row("Save operation result: " + (result ? "ON" : "OFF"), geometry.detailsWidth, role.color, () -> {
                result = !result;
                rebuildDetails();
            }));
        if (!form.field("inputs")
            .isEmpty()) text(details, "Inputs: " + form.field("inputs"), geometry.detailsWidth, 0xffc6ccd6);
        if (!form.field("returns")
            .isEmpty()) text(details, "Result: " + form.field("returns"), geometry.detailsWidth, 0xffc6ccd6);
        text(details, "Code to add (read only)", geometry.detailsWidth, 0xffe7e8eb);
        text(details, form.code(result, comparison), geometry.detailsWidth, 0xff9cdcfe);
        details.child(
            row(
                "Copy preview",
                geometry.detailsWidth,
                0xffe7e8eb,
                () -> GuiScreen.setClipboardString(form.code(result, comparison))));
        String refusal = placement.refusal(form);
        text(
            details,
            refusal.isEmpty() ? placement.note() : refusal,
            geometry.detailsWidth,
            refusal.isEmpty() ? 0xffb9c1cc : 0xffefa09a);
        text(
            details,
            "Up/Down: results. Enter: details. Ctrl+Enter: insert. Backspace: Back.",
            geometry.detailsWidth,
            0xffaeb7c4);
        if (form.placement.equals("New program") && !host.canCreateProject()) text(
            details,
            "Complete examples open as new projects in Studio. Copy preserves the current Manager draft.",
            geometry.detailsWidth,
            0xffe7cb89);
        if (form.placement.equals("Library picker") && !host.canPickLibrary()) text(
            details,
            "Use the Programming Station to select an authorized saved library.",
            geometry.detailsWidth,
            0xffe7cb89);
        details.child(
            row(syntax ? "Hide syntax & arguments" : "Syntax & arguments", geometry.detailsWidth, 0xffe7e8eb, () -> {
                syntax = !syntax;
                rebuildDetails();
            }));
        if (syntax) {
            for (String signature : form.signatures()) text(details, signature, geometry.detailsWidth, 0xff9cdcfe);
            text(details, form.field("argumentsHelp"), geometry.detailsWidth, 0xffc6ccd6);
        }
        if (!form.field("technicalNotes")
            .isEmpty()) {
            details.child(
                row(notes ? "Hide technical notes" : "Show technical notes", geometry.detailsWidth, 0xffe7e8eb, () -> {
                    notes = !notes;
                    rebuildDetails();
                }));
            if (notes) text(details, form.field("technicalNotes"), geometry.detailsWidth, 0xffc6ccd6);
        }
        if (!form.usage()
            .isEmpty() || form.card.id.equals("example_first_transfer")) {
            details.child(
                row(
                    usage ? "Hide usage guide" : "Show usage guide (not inserted)",
                    geometry.detailsWidth,
                    0xffe7e8eb,
                    () -> {
                        usage = !usage;
                        rebuildDetails();
                    }));
            if (usage) for (String line : form.usage()) text(details, line, geometry.detailsWidth, 0xffc6ccd6);
            if (usage && form.card.id.equals("example_first_transfer")) {
                int step = 0;
                for (String instruction : catalog.firstSteps)
                    text(details, (++step) + ". " + instruction, geometry.detailsWidth, 0xffc6ccd6);
            }
        }
        for (String id : form.card.related) {
            Card related = catalog.card(id);
            if (related == null) continue;
            details.child(
                row(
                    "Open related: " + related.name,
                    geometry.detailsWidth,
                    0xffe7e8eb,
                    () -> { openRelated(related); }));
        }
        details.restore = scroll;
        resize(details);
    }

    private void openRelated(Card related) {
        section = InsertNavigation.sectionFor(related, section);
        query = "";
        selected = null;
        changed();
        for (Match match : matches) if (match.card == related) {
            select(match.form, true);
            break;
        }
        revealSelected();
    }

    private boolean canInsert() {
        if ((!geometry.wide && pane == 0) || selected == null
            || !host.editable()
            || !source.equals(
                host.editor()
                    .source()))
            return false;
        if (selected.placement.equals("New program")) return true;
        if (selected.placement.equals("Library picker")) return host.canPickLibrary();
        return placement.refusal(selected)
            .isEmpty();
    }

    private void insert() {
        if ((!geometry.wide && pane == 0) || selected == null || !host.editable()) return;
        if (!source.equals(
            host.editor()
                .source())) {
            message = "The draft changed while browsing. Close and reopen Insert.";
            rebuildDetails();
            return;
        }
        Form form = selected;
        String code = form.code(result, comparison);
        if (form.placement.equals("New program")) {
            handler.closePanel();
            if (host.canCreateProject()) host.newExample(form.card.name, code);
            else {
                GuiScreen.setClipboardString(code);
                host.status("Example copied. Open it as a new project in Studio; current draft kept.");
            }
            return;
        }
        if (form.placement.equals("Library picker")) {
            if (host.canPickLibrary()) {
                handler.closePanel();
                host.library();
            }
            return;
        }
        message = placement.refusal(form);
        if (message.isEmpty() && !host.prepareInsert())
            message = "Finish or discard pending visual edits before inserting code.";
        if (!message.isEmpty()) {
            details.restore = 0;
            rebuildDetails();
            return;
        }
        if (host.editor()
            .insertTemplate(code, start, end)) {
            handler.closePanel();
            host.status("Inserted " + form.card.name + ". Replace placeholders, then Check code.");
        }
    }

    private static void resize(ListWidget<IWidget, ?> list) {
        if (list.isValid()) list.scheduleResize();
    }

    private static ButtonWidget<?> button(String name, int x, int y, int width, Runnable run) {
        return new ButtonWidget<>().pos(x, y)
            .size(width, 18)
            .background(StudioTheme.BUTTON)
            .hoverBackground(StudioTheme.HOVER)
            .overlay(IKey.str(name))
            .onMousePressed(mouse -> {
                if (mouse != 0) return false;
                run.run();
                return true;
            });
    }

    private static ButtonWidget<?> row(String value, int width, int color, Runnable run) {
        Paragraph paragraph = new Paragraph(value, width - 10, color);
        return new ButtonWidget<>().widthRel(1f)
            .height(paragraph.lines.size() * 10 + 8)
            .background(StudioTheme.PANEL)
            .hoverBackground(StudioTheme.HOVER)
            .overlay(
                IKey.str(String.join("\n", paragraph.lines))
                    .color(color))
            .onMousePressed(mouse -> {
                if (mouse != 0) return false;
                run.run();
                return true;
            });
    }

    private static void text(ListWidget<IWidget, ?> list, String value, int width, int color) {
        if (!value.isEmpty()) list.child(new Paragraph(value, width - 10, color).widthRel(1f));
    }

    private static final class Paragraph extends Widget<Paragraph> {

        final List<String> lines = new ArrayList<>();
        final int color;

        Paragraph(String text, int width, int color) {
            this.color = color;
            for (String line : text.split("\n", -1)) lines.addAll(
                Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(line, Math.max(20, width - 8)));
            height(lines.size() * 10 + 8);
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> theme) {
            for (int i = 0; i < lines.size(); i++)
                Minecraft.getMinecraft().fontRenderer.drawString(lines.get(i), 4, 4 + i * 10, color);
        }
    }

    /** Cached text; only the small kind marker is colored, never the whole description. */
    private final class InsertRow extends ButtonWidget<InsertRow> {

        final Match match;
        final List<String> title, summary;
        final String kind;
        final int color;

        InsertRow(Match match) {
            this.match = match;
            color = catalog.role(match.card.role).color;
            kind = InsertNavigation.kind(match.card.role);
            int width = geometry.resultsWidth - 24;
            title = new Paragraph(match.card.name, width, 0xffe7e8eb).lines;
            String explanation = (hasQuery() && !match.form.name.equals(match.card.name) ? match.form.name + ": " : "")
                + match.form.field("summary");
            if (match.card.example)
                explanation += InsertNavigation.wholeProgram(match.card) ? " Complete program; opens a new draft."
                    : " Worked pattern; inserts a code sequence.";
            summary = new Paragraph(explanation, width, 0xffb9c1cc).lines;
            widthRel(1f).height(26 + (title.size() + summary.size()) * 10)
                .background(StudioTheme.PANEL)
                .hoverBackground(StudioTheme.HOVER);
            onMousePressed(mouse -> {
                if (mouse != 0) return false;
                select(match.form, true);
                return true;
            });
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> theme) {
            super.draw(context, theme);
            Gui.drawRect(2, 2, 4, getArea().height - 2, color);
            if (selected != null && selected.card == match.card) Gui.drawRect(4, 0, getArea().width - 1, 2, 0xffb66e74);
            Gui.drawRect(8, 7, 12, 11, color);
            Minecraft.getMinecraft().fontRenderer.drawString(kind, 16, 5, 0xffdce0e5);
            int y = 18;
            for (String line : title) {
                Minecraft.getMinecraft().fontRenderer.drawString(line, 8, y, 0xffe7e8eb);
                y += 10;
            }
            y += 2;
            for (String line : summary) {
                Minecraft.getMinecraft().fontRenderer.drawString(line, 8, y, 0xffb9c1cc);
                y += 10;
            }
        }
    }

    private final class CategoryRow extends ButtonWidget<CategoryRow> {

        final Section choice;
        final List<String> lines;

        CategoryRow(Section choice, int count) {
            this.choice = choice;
            lines = new Paragraph(choice.name + " (" + count + ")", geometry.filterWidth - 18, 0xffe7e8eb).lines;
            widthRel(1f).height(lines.size() * 10 + 8)
                .background(StudioTheme.PANEL)
                .hoverBackground(StudioTheme.HOVER);
            addTooltipLine(choice.description);
            onMousePressed(mouse -> {
                if (mouse != 0) return false;
                choose(choice);
                return true;
            });
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> theme) {
            super.draw(context, theme);
            if (!hasQuery() && section == choice) Gui.drawRect(1, 1, 4, getArea().height - 1, 0xffc4787d);
            if (!choice.role.isEmpty()) Gui.drawRect(6, 7, 10, 11, catalog.role(choice.role).color);
            int y = 4;
            for (String line : lines) {
                Minecraft.getMinecraft().fontRenderer.drawString(line, 13, y, 0xffe7e8eb);
                y += 10;
            }
        }
    }

    private final class KindLegend extends Widget<KindLegend> {

        final List<String> roles = new ArrayList<>();
        final List<Integer> xs = new ArrayList<>(), ys = new ArrayList<>();

        KindLegend(Set<String> kinds, int width) {
            int x = 4, y = 3;
            for (String role : kinds) {
                int length = Minecraft.getMinecraft().fontRenderer.getStringWidth(InsertNavigation.kind(role)) + 18;
                if (x > 4 && x + length > width) {
                    x = 4;
                    y += 12;
                }
                roles.add(role);
                xs.add(x);
                ys.add(y);
                x += length;
            }
            widthRel(1f).height(y + 15);
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> theme) {
            for (int i = 0; i < roles.size(); i++) {
                String role = roles.get(i);
                int x = xs.get(i), y = ys.get(i);
                Gui.drawRect(x, y + 2, x + 4, y + 6, catalog.role(role).color);
                Minecraft.getMinecraft().fontRenderer.drawString(InsertNavigation.kind(role), x + 7, y, 0xffc6ccd6);
            }
        }
    }

    private static final class RememberedList extends ListWidget<IWidget, RememberedList> {

        int restore = -1;
        IWidget reveal;

        @Override
        public void postResize() {
            super.postResize();
            if (restore >= 0) {
                getScrollData().scrollBy(getScrollArea(), restore - getScrollY());
                restore = -1;
            }
            if (reveal != null) {
                int y = 0;
                for (IWidget child : getChildren()) {
                    if (child == reveal) {
                        if (y < getScrollY()) getScrollData().scrollTo(getScrollArea(), y);
                        else if (y + child.getArea().height > getScrollY() + getArea().height)
                            getScrollData().scrollTo(getScrollArea(), y + child.getArea().height - getArea().height);
                        break;
                    }
                    y += child.getArea().height + getSeparatorSize();
                }
                reveal = null;
            }
        }
    }

    /** Fixed gutters; narrow windows use named category/list/detail routes, never result pagination. */
    public static final class Geometry {

        static final int CONTENT_TOP = 106;

        public final int width, height, filterWidth, resultsX, resultsWidth, detailsX, detailsWidth;
        public final boolean wide;

        public Geometry(int availableWidth, int availableHeight) {
            width = Math.max(240, Math.min(1000, availableWidth - 16));
            height = Math.max(180, Math.min(680, availableHeight - 16));
            wide = width >= 760;
            filterWidth = wide ? 174 : width - 16;
            resultsX = wide ? 188 : 8;
            resultsWidth = wide ? 232 : width - 16;
            detailsX = wide ? 426 : 8;
            detailsWidth = wide ? width - 434 : width - 16;
        }
    }
}
