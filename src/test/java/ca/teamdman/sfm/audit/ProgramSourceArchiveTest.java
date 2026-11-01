package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.program.project.ProgramSourceArchive;
import ca.teamdman.sfm.common.program.project.ProgramSourceArchive.Entry;
import ca.teamdman.sfm.common.program.project.ProjectWorkspace;

class ProgramSourceArchiveTest {

    @Test
    void exactSavedNamesAndSourcesRoundTripIncludingUnicodeAndInvalidDrafts() {
        List<Entry> entries = Arrays
            .asList(new Entry("Infusion 😀", "name \"é\"\r\n-- 😀\ninvalid draft"), new Entry("Empty", ""));
        String encoded = ProgramSourceArchive.encode(entries);
        assertTrue(encoded.startsWith(ProgramSourceArchive.PREFIX));
        assertEquals(encoded, ProgramSourceArchive.encode(entries));
        List<Entry> restored = ProgramSourceArchive.decode("\n" + encoded + "\n");
        for (int i = 0; i < entries.size(); i++) {
            assertEquals(entries.get(i).name, restored.get(i).name);
            assertEquals(entries.get(i).source, restored.get(i).source);
        }
        assertThrows(UnsupportedOperationException.class, () -> restored.clear());
        assertTrue(
            ProgramSourceArchive.decode(ProgramSourceArchive.encode(Collections.emptyList()))
                .isEmpty());
    }

    @Test
    void supportsTheWholeOwnerQuotaAtMaximumSourceSize() {
        String source = String.join("", Collections.nCopies(ProjectWorkspace.MAX_SOURCE_BYTES, "x"));
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < ProjectWorkspace.MAX_PROJECTS_PER_OWNER; i++)
            entries.add(new Entry("Program " + i, source));
        String encoded = ProgramSourceArchive.encode(entries);
        List<Entry> restored = ProgramSourceArchive.decode(encoded);
        assertEquals(ProjectWorkspace.MAX_PROJECTS_PER_OWNER, restored.size());
        assertEquals(source, restored.get(restored.size() - 1).source);
        assertTrue(encoded.length() <= ProgramSourceArchive.MAX_TEXT_CHARACTERS);
        entries.add(new Entry("Too many", ""));
        assertThrows(IllegalArgumentException.class, () -> ProgramSourceArchive.encode(entries));
    }

    @Test
    void refusesMalformedDataBeforeAnyEntryCanBeImported() throws Exception {
        for (String invalid : new String[] { "", "arbitrary clipboard", ProgramSourceArchive.PREFIX + "!!",
            raw(2, 0, -1, false), raw(1, 129, -1, false), raw(1, 1, 97, false), raw(1, 0, -1, true) })
            assertThrows(IllegalArgumentException.class, () -> ProgramSourceArchive.decode(invalid));
        String valid = ProgramSourceArchive.encode(Collections.singletonList(new Entry("Saved", "source")));
        byte[] packed = Base64.getDecoder()
            .decode(valid.substring(ProgramSourceArchive.PREFIX.length()));
        packed[packed.length - 1] ^= 1;
        assertThrows(
            IllegalArgumentException.class,
            () -> ProgramSourceArchive.decode(
                ProgramSourceArchive.PREFIX + Base64.getEncoder()
                    .encodeToString(packed)));
        assertThrows(IllegalArgumentException.class, () -> new Entry("bad\nname", ""));
        assertThrows(IllegalArgumentException.class, () -> new Entry("Saved", "\ud800"));
    }

    private static String raw(int version, int count, int nameLength, boolean trailing) throws Exception {
        ByteArrayOutputStream packed = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(packed))) {
            out.writeInt(0x53464d41);
            out.writeInt(version);
            out.writeInt(count);
            if (nameLength >= 0) out.writeInt(nameLength);
            if (trailing) out.writeByte(1);
        }
        return ProgramSourceArchive.PREFIX + Base64.getEncoder()
            .encodeToString(packed.toByteArray());
    }
}
