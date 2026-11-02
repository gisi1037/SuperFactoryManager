package ca.teamdman.sfm.common.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import ca.teamdman.sfm.common.util.BoundedUtf8;
import io.netty.buffer.ByteBuf;

/** Small, source-free messages carried by MUI2's window-scoped action/value transport. */
public final class CableLabelData {

    public static final int PAGE_SIZE = 8;

    public enum Action {
        REFRESH,
        MANAGERS,
        ROW,
        PREVIOUS,
        NEXT,
        SEARCH,
        CLEAR,
        ADD,
        REMOVE
    }

    public enum Mode {
        BUSY,
        MANAGERS,
        BLOCKS,
        STALE
    }

    private CableLabelData() {}

    public static final class Request {

        public final UUID session;
        public final long revision;
        public final Action action;
        public final int row;
        public final String text;

        public Request(UUID session, long revision, Action action, int row, String text) {
            if (session == null || revision <= 0
                || action == null
                || row < 0
                || row >= PAGE_SIZE
                || action != Action.ROW && row != 0
                || text == null
                || text.length() > 256
                || action != Action.SEARCH && action != Action.ADD && action != Action.REMOVE && !text.isEmpty())
                throw new IllegalArgumentException("Invalid cable label request");
            BoundedUtf8.encode(text, 768);
            this.session = session;
            this.revision = revision;
            this.action = action;
            this.row = row;
            this.text = text;
        }

        public void write(ByteBuf buffer) {
            buffer.writeInt(0x53464301);
            ProgramEditWire.uuid(buffer, session);
            buffer.writeLong(revision);
            buffer.writeByte(action.ordinal());
            buffer.writeByte(row);
            ProgramEditWire.string(buffer, text, 768);
        }

        public static Request read(ByteBuf buffer) {
            if (buffer.readableBytes() > 808 || buffer.readInt() != 0x53464301)
                throw new IllegalArgumentException("Invalid cable label message");
            UUID session = ProgramEditWire.uuid(buffer);
            long revision = buffer.readLong();
            int action = buffer.readUnsignedByte(), row = buffer.readUnsignedByte();
            if (action >= Action.values().length) throw new IllegalArgumentException("Invalid cable label action");
            Request request = new Request(
                session,
                revision,
                Action.values()[action],
                row,
                ProgramEditWire.string(buffer, 768));
            if (buffer.isReadable()) throw new IllegalArgumentException("Trailing cable label data");
            return request;
        }
    }

    public static final class Row {

        public final String text;
        public final boolean selected;

        public Row(String text, boolean selected) {
            BoundedUtf8.encode(text, 1536);
            this.text = text;
            this.selected = selected;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Row && selected == ((Row) other).selected && text.equals(((Row) other).text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, selected);
        }
    }

    public static final class View {

        public final UUID session;
        public final long revision;
        public final Mode mode;
        public final String title, message;
        public final int page, total, selected;
        public final List<Row> rows;

        public View(UUID session, long revision, Mode mode, String title, String message, int page, int total,
            int selected, List<Row> rows) {
            if (session == null || revision <= 0
                || mode == null
                || page < 0
                || total < 0
                || total > 8192
                || page > total
                || selected < 0
                || selected > 32
                || rows == null
                || rows.size() > PAGE_SIZE) throw new IllegalArgumentException("Invalid cable label view");
            BoundedUtf8.encode(title, 768);
            BoundedUtf8.encode(message, 768);
            this.session = session;
            this.revision = revision;
            this.mode = mode;
            this.title = title;
            this.message = message;
            this.page = page;
            this.total = total;
            this.selected = selected;
            this.rows = Collections.unmodifiableList(new ArrayList<>(rows));
        }

        public void write(ByteBuf buffer) {
            ProgramEditWire.uuid(buffer, session);
            buffer.writeLong(revision);
            buffer.writeByte(mode.ordinal());
            ProgramEditWire.string(buffer, title, 768);
            ProgramEditWire.string(buffer, message, 768);
            buffer.writeInt(page);
            buffer.writeInt(total);
            buffer.writeByte(selected);
            buffer.writeByte(rows.size());
            for (Row row : rows) {
                ProgramEditWire.string(buffer, row.text, 1536);
                buffer.writeBoolean(row.selected);
            }
        }

        public static View read(ByteBuf buffer) {
            if (buffer.readableBytes() > 16384) throw new IllegalArgumentException("Oversized cable label view");
            UUID session = ProgramEditWire.uuid(buffer);
            long revision = buffer.readLong();
            int mode = buffer.readUnsignedByte();
            if (mode >= Mode.values().length) throw new IllegalArgumentException("Invalid cable label view mode");
            String title = ProgramEditWire.string(buffer, 768), message = ProgramEditWire.string(buffer, 768);
            int page = buffer.readInt(), total = buffer.readInt(), selected = buffer.readUnsignedByte(),
                count = buffer.readUnsignedByte();
            if (count > PAGE_SIZE) throw new IllegalArgumentException("Oversized cable label page");
            List<Row> rows = new ArrayList<>();
            for (int i = 0; i < count; i++)
                rows.add(new Row(ProgramEditWire.string(buffer, 1536), buffer.readBoolean()));
            View view = new View(session, revision, Mode.values()[mode], title, message, page, total, selected, rows);
            if (buffer.isReadable()) throw new IllegalArgumentException("Trailing cable label view data");
            return view;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof View)) return false;
            View view = (View) other;
            return session.equals(view.session) && revision == view.revision
                && mode == view.mode
                && title.equals(view.title)
                && message.equals(view.message)
                && page == view.page
                && total == view.total
                && selected == view.selected
                && rows.equals(view.rows);
        }

        @Override
        public int hashCode() {
            return Objects.hash(session, revision, mode, title, message, page, total, selected, rows);
        }
    }
}
