package ca.teamdman.sfml.program_builder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfm.common.program.project.LibraryImport;
import ca.teamdman.sfm.common.program.project.LibraryModule;
import ca.teamdman.sfm.common.program.project.PublishedProgram;
import ca.teamdman.sfml.ast.ASTBuilder;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.SfmlRuntimeException;

/** Links a closed immutable bundle in one compiler; no repository IO or source concatenation occurs here. */
public final class BundleCompiler {

    private static final Pattern COORDINATES = Pattern.compile("Line (\\d+), Col (\\d+):");

    private BundleCompiler() {}

    public static ProgramBuildResult validateLibrary(String source) {
        ProgramBuildResult syntax = ProgramBuilder.parseSyntax(source);
        if (!syntax.isSuccess()) return syntax;
        try {
            requireLeaf(syntax.getSyntax());
            return ProgramBuildResult.success((Program) new ASTBuilder().visit(syntax.getSyntax()));
        } catch (IllegalArgumentException invalid) {
            return failure(invalid.getMessage());
        }
    }

    public static ProgramBuildResult build(PublishedProgram bundle) {
        if (bundle == null) return failure("Published bundle is required");
        if (bundle.modules.isEmpty()) return new ProgramBuilder(bundle.source).build();
        ProgramBuildResult root = ProgramBuilder.parseSyntax(bundle.source);
        if (!root.isSuccess()) return root;
        List<LibraryImport> imports;
        try {
            imports = LibraryImports.fromSyntax(root.getSyntax());
        } catch (IllegalArgumentException invalid) {
            return failure(invalid.getMessage());
        }
        Map<String, SFMLParser.ImportDeclarationContext> locations = new HashMap<>();
        for (SFMLParser.ImportDeclarationContext declaration : root.getSyntax()
            .importDeclaration())
            locations.put(
                declaration.identifier()
                    .getText(),
                declaration);
        try {
            if (imports.size() != bundle.modules.size() || imports.size() > 8)
                throw new IllegalArgumentException("Published bundle does not match its exact import closure");
            Map<String, LibraryModule> modules = new HashMap<>();
            int bytes = bundle.source.getBytes(StandardCharsets.UTF_8).length;
            for (LibraryModule module : bundle.modules) {
                if (module == null || modules.put(module.reference.alias, module) != null)
                    throw new IllegalArgumentException("Duplicate or missing bundle module");
                bytes = Math.addExact(bytes, module.library.source.getBytes(StandardCharsets.UTF_8).length);
            }
            if (bytes > 120000) throw new IllegalArgumentException("Bundle source exceeds 120000 UTF-8 bytes");
            List<ASTBuilder.LibraryUnit> units = new ArrayList<>();
            for (LibraryImport reference : imports) {
                LibraryModule module = modules.remove(reference.alias);
                if (module == null || !same(reference, module.reference)
                    || !reference.digest.equals(module.library.digest()))
                    return importFailure(
                        locations.get(reference.alias),
                        reference,
                        1,
                        0,
                        "Exact library module is missing or mismatched");
                ProgramBuildResult parsed = ProgramBuilder.parseSyntax(module.library.source);
                if (!parsed.isSuccess()) {
                    int[] coordinates = coordinates(parsed.getErrorMessage());
                    return importFailure(
                        locations.get(reference.alias),
                        reference,
                        coordinates[0],
                        coordinates[1],
                        "Invalid library syntax");
                }
                try {
                    requireLeaf(parsed.getSyntax());
                } catch (IllegalArgumentException invalid) {
                    return importFailure(
                        locations.get(reference.alias),
                        reference,
                        1,
                        0,
                        "Library must contain simple function definitions only");
                }
                units.add(
                    new ASTBuilder.LibraryUnit(
                        reference.alias,
                        reference.revision,
                        parsed.getSyntax()
                            .functionDefinition()));
            }
            if (!modules.isEmpty()) throw new IllegalArgumentException("Bundle contains unreferenced modules");
            return ProgramBuildResult.success((Program) new ASTBuilder(units, bundle.source).visit(root.getSyntax()));
        } catch (SfmlRuntimeException moduleFailure) {
            if (moduleFailure.isModuleLocation()) {
                for (LibraryImport reference : imports)
                    if (reference.alias.equals(moduleFailure.getModuleAlias())) return importFailure(
                        locations.get(reference.alias),
                        reference,
                        moduleFailure.getLine(),
                        moduleFailure.getColumn(),
                        "Library compilation failed");
            }
            return failure(moduleFailure.getMessage());
        } catch (IllegalArgumentException | ArithmeticException invalid) {
            return failure(invalid.getMessage());
        } catch (StackOverflowError invalid) {
            return failure("Bundle expression is too complex");
        }
    }

    private static void requireLeaf(SFMLParser.ProgramContext tree) {
        if (tree.name() != null || !tree.importDeclaration()
            .isEmpty()
            || !tree.stateDeclaration()
                .isEmpty()
            || !tree.dataDeclaration()
                .isEmpty()
            || !tree.trigger()
                .isEmpty()
            || tree.functionDefinition()
                .isEmpty())
            throw new IllegalArgumentException(
                "A library must contain at least one function and no other top-level statements");
        for (SFMLParser.FunctionDefinitionContext function : tree.functionDefinition()) if (function.functionName()
            .getText()
            .indexOf('.') >= 0) throw new IllegalArgumentException("Library export names must be simple identifiers");
    }

    private static boolean same(LibraryImport left, LibraryImport right) {
        return left.alias.equals(right.alias) && left.project.equals(right.project)
            && left.revision == right.revision
            && left.digest.equals(right.digest);
    }

    private static int[] coordinates(String error) {
        Matcher matcher = COORDINATES.matcher(error == null ? "" : error);
        return matcher.find() ? new int[] { Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)) }
            : new int[] { 1, 0 };
    }

    private static ProgramBuildResult importFailure(SFMLParser.ImportDeclarationContext declaration,
        LibraryImport reference, int line, int column, String detail) {
        return failure(
            "Line " + declaration.getStart()
                .getLine()
                + ", Col "
                + declaration.getStart()
                    .getCharPositionInLine()
                + ": Library "
                + reference.alias
                + " revision "
                + reference.revision
                + ", module Line "
                + line
                + ", Col "
                + column
                + ": "
                + detail);
    }

    private static ProgramBuildResult failure(String error) {
        String message = error == null ? "Bundle compilation failed" : error;
        return ProgramBuildResult
            .failure(Collections.singletonList(message.length() > 2048 ? message.substring(0, 2048) : message));
    }
}
