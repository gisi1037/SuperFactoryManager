package ca.teamdman.sfm.integration.mui2;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.lwjgl.input.Keyboard;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import ca.teamdman.sfm.Config;
import ca.teamdman.sfm.common.gui.ManagerControlToken;
import ca.teamdman.sfm.common.net.PacketManagerNetworkReply;
import ca.teamdman.sfm.common.net.PacketManagerNetworkRequest;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.program.ManagerNetworkBrowser;

/** Cached read-only Network page. Controls and clipped output fit the 70px compact machine body. */
final class ManagerNetworkWidget extends ParentWidget<ManagerNetworkWidget> {

    private final int x, y, z;
    private final Supplier<ManagerControlToken> control;
    private final ManagerNetworkViewState state = new ManagerNetworkViewState();
    private final ConsoleOutputWidget output = new ConsoleOutputWidget();
    private String query = "";
    private int filter, ticks;
    private long displayed = -1;
    private ManagerNetworkBrowser.Page displayedPage;

    ManagerNetworkWidget(int x, int y, int z, Supplier<ManagerControlToken> control, Runnable rebuild, Runnable limits,
        Runnable discord) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.control = control;
        name("manager_network_browser");
        child(new TextFieldWidget() {

            @Override
            public Result onKeyPressed(char character, int key) {
                if (isFocused() && (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER)) {
                    request(0);
                    return Result.SUCCESS;
                }
                return super.onKeyPressed(character, key);
            }
        }.setMaxLength(64)
            .autoUpdateOnChange(true)
            .setFocusOnGuiOpen(false)
            .value(new StringValue.Dynamic(() -> query, value -> query = value))
            .pos(0, 0)
            .size(110, 16)
            .addTooltipLine(
                "Search names, registry IDs, coordinates or saved labels. Enter or Refresh starts a new snapshot."));
        String[] filters = { "All", "Items", "Fluids" };
        int[] widths = { 30, 37, 37 };
        int left = 112;
        for (int i = 0; i < filters.length; i++) {
            final int selected = i;
            button(() -> (filter == selected ? "\u00a7f" : "\u00a77") + filters[selected], left, 0, widths[i], () -> {
                filter = selected;
                request(0);
            });
            left += widths[i] + 2;
        }
        button(() -> "Refresh", 222, 0, 74, () -> request(0)).addTooltipLine(
            "Read existing cached discovery and saved labels only. This never scans cables or reads slots/tanks.");
        button(() -> "Prev", 0, 18, 34, () -> request(-1)).setEnabledIf(w -> state.canPrevious(query, filter));
        button(() -> "Next", 36, 18, 34, () -> request(1)).setEnabledIf(w -> state.canNext(query, filter))
            .addTooltipLine("Search may return an empty page with more candidates. Next continues the same snapshot.");
        button(() -> "Rebuild network", 72, 18, 86, () -> {
            rebuild.run();
            state.note("Discovery rebuild requested. Press Refresh afterward; displayed rows are cached metadata.");
        }).addTooltipLine("Explicit server discovery. Refresh alone never forces a cable scan.");
        button(() -> "Limits", 160, 18, 54, limits);
        if (Config.showDiscordButton) button(() -> "Discord", 216, 18, 80, discord);
        output.follow(false);
        child(
            output.left(0)
                .right(0)
                .top(36)
                .bottom(14));
        child(
            new TextWidget<>(IKey.dynamic(() -> shortSummary(summary()))).left(0)
                .right(0)
                .bottom(0)
                .height(10)
                .scale(.65f)
                .color(0xffb5bec8)
                .addTooltipLine(IKey.dynamic(this::summary)));
    }

    private ButtonWidget<?> button(Supplier<String> title, int x, int y, int width, Runnable action) {
        ButtonWidget<?> button = new ButtonWidget<>().pos(x, y)
            .size(width, 16)
            .background(ManagerConsoleTheme.BUTTON)
            .hoverBackground(ManagerConsoleTheme.HOVER)
            .overlay(
                IKey.dynamic(title)
                    .scale(.75f))
            .onMousePressed(mouse -> {
                if (mouse != 0) return false;
                if (control.get() != null && !state.pending()) action.run();
                return true;
            });
        child(button);
        return button;
    }

    private void request(int direction) {
        state.control(control.get());
        try {
            ManagerNetworkViewState.Request request = state.begin(query, filter, direction, ticks);
            if (request != null) SFMNetwork.WRAPPER.sendToServer(
                new PacketManagerNetworkRequest(
                    x,
                    y,
                    z,
                    request.token,
                    request.id,
                    request.snapshotId,
                    request.cursor,
                    request.query,
                    request.filter));
        } catch (IllegalArgumentException | IllegalStateException error) {
            state.note(error.getMessage() + " Cached rows kept.");
        }
    }

    void tick() {
        ticks++;
        state.control(control.get());
        state.tick(ticks);
        if (displayed == state.revision()) return;
        displayed = state.revision();
        List<String> lines = new ArrayList<>();
        ManagerNetworkBrowser.Page page = state.page();
        if (displayedPage != page) {
            displayedPage = page;
            output.getScrollArea()
                .getScrollY()
                .scrollTo(output.getScrollArea(), 0);
        }
        if (page == null)
            lines.add("Read-only cached network metadata. Refresh to inspect; Rebuild network discovers connections.");
        else {
            for (ManagerNetworkBrowser.Row row : page.rows) {
                String type = (row.kind & 1) != 0 ? (row.kind & 2) != 0 ? "items + fluids" : "items"
                    : (row.kind & 2) != 0 ? "fluids" : "other";
                lines.add(row.name);
                lines.add(
                    row.position.x + " / "
                        + row.position.y
                        + " / "
                        + row.position.z
                        + " | "
                        + type
                        + " | "
                        + stateName(row.state));
                if (!row.labels.isEmpty()) lines.add("Labels: " + row.labels);
            }
            if (page.rows.isEmpty()) lines.add(
                page.nextCursor < 0 ? "No matching rows remain."
                    : "No matches in this bounded page. Next scans more candidates.");
            lines.add("Metadata is not side access, contents, capacity or transfer availability.");
        }
        output.lines(lines);
    }

    private String summary() {
        ManagerNetworkBrowser.Page page = state.page();
        String flags = page == null ? ""
            : (page.partial ? "PARTIAL | " : "") + (page.topologyCurrent ? "" : "UNCONFIRMED | ");
        return flags + state.notice()
            + (page == null ? ""
                : " | Next candidate " + (page.nextCursor < 0 ? page.totalCandidates : page.nextCursor)
                    + "/"
                    + page.totalCandidates
                    + ", captured "
                    + page.capturedTick
                    + ", scan "
                    + page.topologyTick
                    + (page.topologyCurrent ? " current" : " unconfirmed")
                    + (page.partial ? " PARTIAL" : ""));
    }

    private static String shortSummary(String text) {
        if (text.length() <= 64) return text;
        int end = Character.isHighSurrogate(text.charAt(60)) ? 60 : 61;
        return text.substring(0, end) + "...";
    }

    private static String stateName(int state) {
        switch (state) {
            case 0:
                return "cached connected";
            case 1:
                return "stale / unconfirmed";
            case 2:
                return "unloaded";
            case 3:
                return "missing";
            case 4:
                return "metadata unreadable";
            case 5:
                return "disconnected";
            default:
                return "unknown";
        }
    }

    void receive(PacketManagerNetworkReply reply) {
        if (reply.x != x || reply.y != y || reply.z != z) return;
        state.control(control.get());
        state.receive(reply.token, reply.requestId, reply.accepted, reply.message, reply.page);
    }

    void close() {
        state.close();
    }
}
