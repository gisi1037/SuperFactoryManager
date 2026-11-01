package ca.teamdman.sfm.common.program.project;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class LibraryPublicationCodecTest {

    private static final String ROOT = "EVERY 20 TICKS DO END";
    private static final String SOURCE = "integer twice(integer value) { RETURN value * 2; }";
    private static final UUID PROJECT = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    @Test
    void originalRootOnlyConstructorAndEmptyClosureRemainByteIdenticalV1() throws Exception {
        PublishedProgram original = new PublishedProgram(ROOT);
        assertEquals(1, original.format);
        assertEquals("f5328160bc084011b924f02157418280f2b52efacaaada88f0212c24d91a670e", original.digest());
        assertArrayEquals(original.encode(), new PublishedProgram(ROOT, Collections.emptyList()).encode());
        assertTrue(original.modules.isEmpty());
        assertTrue(original.dependencies.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> original.modules.add(module("math")));
        assertEquals(1, PublishedProgram.decode(original.encode()).format);
    }

    @Test
    void exactLibraryCodecHasIndependentDomainAndStrictUtf8HeadersLengthsAndEof() throws Exception {
        PublishedLibrary library = new PublishedLibrary("// \u00e9 \uD83D\uDE80\r\n" + SOURCE);
        byte[] valid = library.encode();
        assertEquals(library.source, PublishedLibrary.decode(valid).source);
        assertArrayEquals(
            valid,
            PublishedLibrary.decode(valid)
                .encode());
        assertNotEquals(new PublishedProgram(library.source).digest(), library.digest());
        assertNotEquals(library.digest(), new PublishedLibrary(library.source + "\n").digest());
        byte[] magic = valid.clone(), version = valid.clone(), language = valid.clone(), utf8 = valid.clone(),
            hugeLength = valid.clone();
        magic[0] ^= 1;
        version[7] = 2;
        language[12] = 'x';
        utf8[12] = (byte) 0xff;
        ByteBuffer.wrap(hugeLength)
            .putInt(8, Integer.MAX_VALUE);
        for (byte[] invalid : new byte[][] { magic, version, language, utf8, hugeLength,
            Arrays.copyOf(valid, valid.length + 1), new byte[PublishedLibrary.MAX_BYTES + 1] })
            assertThrows(IOException.class, () -> PublishedLibrary.decode(invalid));
        for (int i = 0; i < valid.length; i++) {
            byte[] shortBytes = Arrays.copyOf(valid, i);
            assertThrows(IOException.class, () -> PublishedLibrary.decode(shortBytes));
        }
        assertThrows(IOException.class, () -> PublishedLibrary.decode(null));
        assertThrows(IllegalArgumentException.class, () -> new PublishedLibrary("\uD800"));
        assertThrows(IllegalArgumentException.class, () -> new PublishedLibrary(repeat("x", 30001)));
    }

    @Test
    void selectionsAreExactBoundedIdentifiersWithoutAuthorityAndModulesBindTheirDigest() {
        LibraryModule module = module("math_2");
        LibraryImport same = new LibraryImport("math_2", PROJECT, 3, module.library.digest());
        assertEquals(module.reference, same);
        assertEquals(module.reference.hashCode(), same.hashCode());
        assertNotEquals(module.reference, new LibraryImport("Math_2", PROJECT, 3, module.library.digest()));
        for (String alias : new String[] { "", "2math", "math.name", "../math", "m\u00e9", repeat("a", 33) })
            assertThrows(
                IllegalArgumentException.class,
                () -> new LibraryImport(alias, PROJECT, 3, module.library.digest()));
        assertThrows(IllegalArgumentException.class, () -> new LibraryImport("math", null, 3, module.library.digest()));
        assertThrows(
            IllegalArgumentException.class,
            () -> new LibraryImport("math", PROJECT, -1, module.library.digest()));
        assertThrows(IllegalArgumentException.class, () -> new LibraryImport("math", PROJECT, 3, repeat("A", 64)));
        assertThrows(
            IllegalArgumentException.class,
            () -> new LibraryModule(module.reference, new PublishedLibrary(SOURCE + "\n")));
    }

    @Test
    void linkedBundlesSortAliasesFreezeClosureAndHashEverySelectionAndExactSource() throws Exception {
        List<LibraryModule> mutable = new ArrayList<>(Arrays.asList(module("z"), module("a")));
        PublishedProgram bundle = new PublishedProgram(ROOT, mutable);
        mutable.clear();
        assertEquals(2, bundle.format);
        assertEquals("a", bundle.modules.get(0).reference.alias);
        assertEquals(2, bundle.dependencies.size());
        assertArrayEquals(
            bundle.encode(),
            new PublishedProgram(ROOT, Arrays.asList(module("a"), module("z"))).encode());
        assertArrayEquals(
            bundle.encode(),
            PublishedProgram.decode(bundle.encode())
                .encode());
        assertEquals(SOURCE, PublishedProgram.decode(bundle.encode()).modules.get(0).library.source);
        assertThrows(UnsupportedOperationException.class, () -> bundle.modules.clear());
        assertThrows(UnsupportedOperationException.class, () -> bundle.dependencies.clear());
        assertNotEquals(bundle.digest(), new PublishedProgram(ROOT + "\n", bundle.modules).digest());
        LibraryModule changedRevision = new LibraryModule(
            new LibraryImport("a", PROJECT, 4, module("a").library.digest()),
            module("a").library);
        assertNotEquals(
            bundle.digest(),
            new PublishedProgram(ROOT, Arrays.asList(changedRevision, module("z"))).digest());
        assertThrows(
            IllegalArgumentException.class,
            () -> new PublishedProgram(ROOT, Arrays.asList(module("a"), module("a"))));
        assertEquals(2, new PublishedProgram(ROOT, Arrays.asList(module("a"), module("A"))).modules.size());
    }

    @Test
    void strictV2RejectsUnknownEmptyUnsortedDuplicateMismatchedAndUnboundedClosures() throws Exception {
        List<LibraryModule> ordered = Arrays.asList(module("a"), module("z"));
        byte[] valid = new PublishedProgram(ROOT, ordered).encode();
        byte[] unknown = valid.clone();
        unknown[7] = 3;
        for (byte[] invalid : new byte[][] { unknown, raw(2, 0, Collections.emptyList()), raw(1, 1, ordered),
            raw(2, 9, Collections.emptyList()), raw(2, -1, Collections.emptyList()),
            raw(2, 2, Arrays.asList(module("z"), module("a"))), raw(2, 2, Arrays.asList(module("a"), module("a"))),
            Arrays.copyOf(valid, valid.length + 1), new byte[PublishedProgram.MAX_BYTES + 1] })
            assertThrows(IOException.class, () -> PublishedProgram.decode(invalid));
        for (int i = 0; i < valid.length; i++) {
            byte[] shortBytes = Arrays.copyOf(valid, i);
            assertThrows(IOException.class, () -> PublishedProgram.decode(shortBytes));
        }
        byte[] wrongDigest = raw(2, 1, Collections.singletonList(module("a")));
        int digestOffset = headerLength() + 4 + 4 + 1 + 16 + 8 + 4;
        wrongDigest[digestOffset] = wrongDigest[digestOffset] == 'a' ? (byte) 'b' : (byte) 'a';
        assertThrows(IOException.class, () -> PublishedProgram.decode(wrongDigest));
        byte[] hugeLibrary = raw(2, 1, Collections.singletonList(module("a")));
        ByteBuffer.wrap(hugeLibrary)
            .putInt(digestOffset + 64, Integer.MAX_VALUE);
        assertThrows(IOException.class, () -> PublishedProgram.decode(hugeLibrary));
    }

    @Test
    void aggregateSourceAndModuleCountAreBoundedInConstructorsAndDecoder() throws Exception {
        PublishedLibrary large = new PublishedLibrary(repeat("x", 30000));
        List<LibraryModule> modules = new ArrayList<>();
        for (int i = 0; i < 3; i++)
            modules.add(new LibraryModule(new LibraryImport("m" + i, PROJECT, 0, large.digest()), large));
        PublishedProgram full = new PublishedProgram(repeat("x", 30000), modules);
        assertEquals(2, PublishedProgram.decode(full.encode()).format);
        List<LibraryModule> tooLarge = new ArrayList<>(modules);
        tooLarge.add(new LibraryModule(new LibraryImport("m3", PROJECT, 0, large.digest()), large));
        byte[] rawOversizedSources = raw(2, 4, tooLarge);
        assertTrue(rawOversizedSources.length < PublishedProgram.MAX_BYTES);
        assertThrows(IOException.class, () -> PublishedProgram.decode(rawOversizedSources));
        modules.add(
            new LibraryModule(
                new LibraryImport("m3", PROJECT, 0, new PublishedLibrary("x").digest()),
                new PublishedLibrary("x")));
        assertThrows(IllegalArgumentException.class, () -> new PublishedProgram(repeat("x", 30000), modules));
        List<LibraryModule> small = new ArrayList<>();
        for (int i = 0; i < 8; i++) small.add(module("m" + i));
        assertEquals(8, new PublishedProgram(ROOT, small).modules.size());
        small.add(module("m8"));
        assertThrows(IllegalArgumentException.class, () -> new PublishedProgram(ROOT, small));
    }

    @Test
    void closedSourceRestoreReturnsOnlyRootWithExplicitV1RootChecksum() {
        PublishedProgram bundle = new PublishedProgram(ROOT, Collections.singletonList(module("math")));
        PublicationReceipt receipt = new PublicationReceipt(UUID.randomUUID(), PROJECT, 0, bundle.digest(), "Root");
        PublicationSource source = new PublicationSource(receipt, bundle);
        assertEquals(ROOT, source.source);
        assertEquals(2, source.format);
        assertEquals(new PublishedProgram(ROOT).digest(), source.sourceDigest);
        assertEquals(ROOT, PublicationSource.fromWire(receipt, ROOT, 2, source.sourceDigest).source);
        assertThrows(IllegalArgumentException.class, () -> new PublicationSource(receipt, ROOT));
        assertThrows(
            IllegalArgumentException.class,
            () -> PublicationSource.fromWire(receipt, ROOT + "\n", 2, source.sourceDigest));
        assertThrows(
            IllegalArgumentException.class,
            () -> PublicationSource.fromWire(receipt, ROOT, 1, source.sourceDigest));
        assertThrows(
            IllegalArgumentException.class,
            () -> PublicationSource.fromWire(receipt, ROOT, 3, source.sourceDigest));
    }

    private static LibraryModule module(String alias) {
        PublishedLibrary library = new PublishedLibrary(SOURCE);
        return new LibraryModule(new LibraryImport(alias, PROJECT, 3, library.digest()), library);
    }

    private static int headerLength() throws IOException {
        return new PublishedProgram(ROOT).encode().length - 4;
    }

    /** Deliberately bypasses constructor canonicalization to exercise the stored format boundary. */
    private static byte[] raw(int format, int count, List<LibraryModule> modules) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeInt(0x53464D42);
            output.writeInt(format);
            PublicationIO.writeString(output, PublishedProgram.LANGUAGE, 32);
            PublicationIO.writeString(output, PublishedProgram.API, 32);
            PublicationIO.writeString(output, PublishedProgram.PROFILE, 32);
            PublicationIO.writeString(output, ROOT, ProjectWorkspace.MAX_SOURCE_BYTES);
            output.writeInt(count);
            for (LibraryModule module : modules) {
                PublicationIO.writeString(output, module.reference.alias, 32);
                PublicationIO.writeId(output, module.reference.project);
                output.writeLong(module.reference.revision);
                PublicationIO.writeString(output, module.reference.digest, 64);
                byte[] library = module.library.encode();
                output.writeInt(library.length);
                output.write(library);
            }
        }
        return bytes.toByteArray();
    }

    private static String repeat(String value, int count) {
        return String.join("", Collections.nCopies(count, value));
    }
}
