package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.program.project.LibraryImport;
import ca.teamdman.sfm.common.program.project.LibraryModule;
import ca.teamdman.sfm.common.program.project.PublishedLibrary;
import ca.teamdman.sfm.common.program.project.PublishedProgram;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.SfmlRuntimeException;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.BundleCompiler;
import ca.teamdman.sfml.program_builder.LibraryImports;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class LibraryBundleCompilerTest {

    private static LibraryModule module(String alias, String source) {
        PublishedLibrary library = new PublishedLibrary(source);
        return new LibraryModule(
            new LibraryImport(
                alias,
                UUID.nameUUIDFromBytes(alias.getBytes(StandardCharsets.UTF_8)),
                3,
                library.digest()),
            library);
    }

    private static String header(LibraryModule module) {
        LibraryImport ref = module.reference;
        return "import " + ref.alias
            + " from \""
            + ref.project
            + "\" revision "
            + ref.revision
            + " digest \""
            + ref.digest
            + "\";\n";
    }

    private static Program linked(String root, LibraryModule... modules) {
        ProgramBuildResult built = BundleCompiler.build(new PublishedProgram(root, Arrays.asList(modules)));
        assertTrue(built.isSuccess(), built.getErrorMessage());
        return built.getProgram();
    }

    @Test
    void rootOnlyContextualNamesAndHeaderProbePreserveExistingPrograms() {
        String old = "INTEGER import(INTEGER revision) { LET digest = revision + 1; RETURN digest; } "
            + "EVERY 20 TICKS { LET revision = 2; LET digest = import(revision); PRINT(digest); "
            + "INPUT item:import:revision FROM \"source\"; }";
        ProgramBuildResult root = new ProgramBuilder(old).build();
        assertTrue(root.isSuccess(), root.getErrorMessage());
        assertTrue(
            BundleCompiler.build(new PublishedProgram(old))
                .isSuccess());
        assertFalse(LibraryImports.hasImports(old));
        assertTrue(
            LibraryImports.parse(old)
                .isEmpty());
        for (String source : Arrays.asList(
            "-- import math from invalid\nEVERY 20 TICKS {}",
            "NAME \"import math\" EVERY 20 TICKS {}",
            "PRINT(\"import math\")",
            "@ import math",
            "NAME 3 import math")) assertFalse(LibraryImports.hasImports(source), source);
        LibraryModule math = module("math", "INTEGER twice(INTEGER value) { RETURN value * 2; }");
        String imported = "-- header\nNAME \"case\"\n" + header(math) + "EVERY 20 TICKS {}";
        assertTrue(LibraryImports.hasImports(imported));
        assertEquals(
            "math",
            LibraryImports.parse(imported)
                .get(0).alias);
        ProgramBuildResult unresolved = new ProgramBuilder(imported).build();
        assertFalse(unresolved.isSuccess());
        assertTrue(
            unresolved.getErrorMessage()
                .contains("Line 3, Col 0"));
    }

    @Test
    void isolatedNamespacesLinkForwardOwnCallsAndIgnorePrivateRootBuiltinOverrides() throws Exception {
        LibraryModule math = module(
            "math",
            "INTEGER twice(INTEGER value) { RETURN helper(value) + helper(value); } "
                + "INTEGER helper(INTEGER value) { RETURN MIN(value, 5); }");
        String root = header(math) + "STATE answer = 0 "
            + "INTEGER MIN(INTEGER a, INTEGER b) { RETURN 100; } "
            + "INTEGER private.helper(INTEGER a) { RETURN a; } "
            + "EVERY 20 TICKS { answer = math.twice(7) + private.helper(1); }";
        Program program = linked(root, math);
        assertEquals(root, program.toString());
        assertFalse(
            program.toString()
                .contains("RETURN MIN(value, 5)"));
        Run run = new Run(program);
        run.tick(20);
        assertEquals(11, run.values[0].asLong());
        assertTrue(
            program.getFunctions()
                .stream()
                .anyMatch(
                    function -> function.getName()
                        .equals("math.helper")));
    }

    @Test
    void moduleCannotCaptureRootStateGlobalsPrivateFunctionsOrOtherNamespaces() {
        LibraryModule other = module("other", "INTEGER value() { RETURN 9; }");
        for (String expression : Arrays.asList("secret", "shared", "privateValue()", "other.value()")) {
            LibraryModule bad = module("math", "\nINTEGER value() {\n RETURN " + expression + ";\n}");
            String source = "NAME \"root\"\n" + header(bad)
                + header(other)
                + "STATE secret = 2 LET shared = 3 INTEGER privateValue() { RETURN 4; } EVERY 20 TICKS {}";
            ProgramBuildResult built = BundleCompiler.build(new PublishedProgram(source, Arrays.asList(bad, other)));
            assertFalse(built.isSuccess(), expression);
            assertTrue(
                built.getErrorMessage()
                    .startsWith("Line 2, Col 0:"),
                built.getErrorMessage());
            assertTrue(
                built.getErrorMessage()
                    .contains("math revision 3, module Line 3"),
                built.getErrorMessage());
            assertFalse(
                built.getErrorMessage()
                    .contains("RETURN " + expression),
                built.getErrorMessage());
        }
    }

    @Test
    void librariesAreFunctionOnlyLeavesAndImportsRequireExactClosedModules() {
        for (String source : Arrays.asList(
            "",
            "NAME \"library\" INTEGER f() { RETURN 1; }",
            "STATE value = 1 INTEGER f() { RETURN 1; }",
            "LET value = 1 INTEGER f() { RETURN value; }",
            "EVERY 20 TICKS {}",
            "INTEGER nested.f() { RETURN 1; }"))
            assertFalse(
                BundleCompiler.validateLibrary(source)
                    .isSuccess(),
                source);
        LibraryModule math = module("math", "INTEGER value() { RETURN 3; }");
        assertTrue(
            BundleCompiler.validateLibrary(math.library.source)
                .isSuccess());
        assertFalse(
            BundleCompiler.validateLibrary(header(math) + math.library.source)
                .isSuccess());
        assertFalse(
            BundleCompiler.build(new PublishedProgram(header(math) + "EVERY 20 TICKS {}"))
                .isSuccess());
        assertFalse(
            BundleCompiler.build(new PublishedProgram("EVERY 20 TICKS {}", Collections.singletonList(math)))
                .isSuccess());
        LibraryModule wrongAlias = module("other", math.library.source);
        assertFalse(
            BundleCompiler
                .build(new PublishedProgram(header(math) + "EVERY 20 TICKS {}", Collections.singletonList(wrongAlias)))
                .isSuccess());
        assertThrows(IllegalArgumentException.class, () -> LibraryImports.parse(header(math) + header(math)));
        String duplicateRef = header(math) + header(math).replace("import math", "import copy");
        assertThrows(IllegalArgumentException.class, () -> LibraryImports.parse(duplicateRef));
        assertFalse(
            BundleCompiler
                .build(
                    new PublishedProgram(
                        header(math) + "INTEGER math.value() { RETURN 1; } EVERY 20 TICKS {}",
                        Collections.singletonList(math)))
                .isSuccess());
    }

    @Test
    void allModulesShareFunctionAndConstantPoolCapsAndRejectRecursion() {
        StringBuilder first = new StringBuilder(), second = new StringBuilder();
        for (int i = 0; i < 70; i++) first.append("INTEGER f")
            .append(i)
            .append("() { RETURN 1; }\n");
        for (int i = 0; i < 60; i++) second.append("INTEGER f")
            .append(i)
            .append("() { RETURN 1; }\n");
        LibraryModule a = module("a", first.toString()), b = module("b", second.toString());
        assertTrue(
            BundleCompiler.validateLibrary(first.toString())
                .isSuccess());
        assertTrue(
            BundleCompiler.validateLibrary(second.toString())
                .isSuccess());
        assertFalse(
            BundleCompiler.build(new PublishedProgram(header(a) + header(b) + "EVERY 20 TICKS {}", Arrays.asList(a, b)))
                .isSuccess());
        char[] payload = new char[3000];
        Arrays.fill(payload, 'x');
        StringBuilder constants = new StringBuilder();
        for (int i = 0; i < 3; i++) constants.append("STRING s")
            .append(i)
            .append("() { RETURN \"")
            .append(payload)
            .append("\"; }\n");
        assertTrue(
            BundleCompiler.validateLibrary(constants.toString())
                .isSuccess());
        LibraryModule c = module("c", constants.toString()), d = module("d", constants.toString());
        assertFalse(
            BundleCompiler.build(new PublishedProgram(header(c) + header(d) + "EVERY 20 TICKS {}", Arrays.asList(c, d)))
                .isSuccess());
        LibraryModule recursive = module(
            "cycle",
            "INTEGER first() { RETURN second(); } INTEGER second() { RETURN first(); }");
        ProgramBuildResult cycle = BundleCompiler
            .build(new PublishedProgram(header(recursive) + "EVERY 20 TICKS {}", Collections.singletonList(recursive)));
        assertFalse(cycle.isSuccess());
        assertTrue(
            cycle.getErrorMessage()
                .contains("cycle revision 3, module Line"));
    }

    @Test
    void runtimeImportedFailuresKeepModuleCoordinatesAcrossRootCallAnnotations() throws Exception {
        LibraryModule math = module("math", "INTEGER fail(INTEGER value) {\n RETURN 1 / value;\n}");
        Program program = linked(header(math) + "STATE answer = 0 EVERY 20 TICKS { answer = math.fail(0); }", math);
        SfmlRuntimeException failure = assertThrows(SfmlRuntimeException.class, () -> new Run(program).tick(20));
        assertTrue(failure.isModuleLocation());
        assertEquals("math", failure.getModuleAlias());
        assertEquals(3, failure.getModuleRevision());
        assertEquals(2, failure.getLine());
        assertTrue(
            failure.getFunctionContexts()
                .contains("math.fail"));
    }

    @Test
    void linkedPlansRetainNoCompilerParserOrInvocationObjects() throws Exception {
        LibraryModule math = module(
            "math",
            "INTEGER total(INTEGER[] values, STRING suffix) { "
                + "LET target = LABEL(\"source\" + suffix); PUSH(REF values, 1); LET sum = 0; "
                + "FOR (LET i = 0; i < LEN(values); i = i + 1) { sum = sum + values[i]; } "
                + "FOR (index, value) IN values { sum = sum + index + value; } RETURN sum; }");
        Program program = linked(
            header(math) + "STATE answer = 0 EVERY 20 TICKS { " + "answer = math.total(ARRAY([1, 2]), \"suffix\"); }",
            math);
        SfmlFunctionTest.assertPlanOwnership(program, new java.util.IdentityHashMap<>());
    }

    @Test
    void importedWaitAndRefEffectsUseTheExistingSchedulerAndInitializerRestrictions() throws Exception {
        LibraryModule work = module(
            "work",
            "VOID advance(REF INTEGER value) { value = value + 1; WAIT 1; value = value + 2; } "
                + "INTEGER later() { WAIT 1; RETURN 4; }");
        Program program = linked(header(work) + "STATE answer = 0 EVERY 20 TICKS { work.advance(REF answer); }", work);
        Run run = new Run(program);
        run.tick(20);
        assertEquals(1, run.values[0].asLong());
        run.tick(21);
        assertEquals(3, run.values[0].asLong());
        assertFalse(
            BundleCompiler
                .build(
                    new PublishedProgram(
                        header(work) + "LET value = work.later() EVERY 20 TICKS {}",
                        Collections.singletonList(work)))
                .isSuccess());
    }

    private static final class Run {

        final AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        final TileEntityManager manager = RecoveryFixtures.manager(fixture);
        final Program program;
        final DataValue[] values;

        Run(Program program) throws Exception {
            this.program = program;
            values = program.getStateSchema()
                .getDeclarations()
                .stream()
                .map(declaration -> declaration.getDefaultValue())
                .toArray(DataValue[]::new);
        }

        void tick(long now) {
            fixture.world.time = now;
            ProgramContext context = new ProgramContext(
                fixture.world,
                manager,
                new CableNetwork(fixture.world, AuditRegressionTest.MANAGER),
                fixture.labels,
                now,
                false);
            context.setStateAccess(new StateAccess() {

                @Override
                public DataValue read(int slot) {
                    return values[slot];
                }

                @Override
                public void write(int slot, DataValue value) {
                    values[slot] = value;
                }
            });
            program.tick(context);
        }
    }
}
