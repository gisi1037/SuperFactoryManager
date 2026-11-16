package ca.teamdman.sfm.common.program;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.World;
import net.minecraft.world.storage.SaveHandler;

import com.google.gson.stream.JsonWriter;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.program.ManagerExecutionProfiler.Hotspot;
import ca.teamdman.sfm.common.program.ManagerExecutionProfiler.Report;
import ca.teamdman.sfm.common.program.ManagerExecutionProfiler.Snapshot;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.data.SourceLocation;

/** Explicit server-thread export of a bounded capture. Filenames come from the loaded manager. */
public final class ManagerProfileDump {

    private static long lastWriteNs;

    private ManagerProfileDump() {}

    public static List<String> save(TileEntityManager manager, ManagerExecutionProfiler profiler,
        RuntimeSourceMap sourceMap) {
        World world = manager.getWorldObj();
        if (world == null || world.provider == null || !(world.getSaveHandler() instanceof SaveHandler save))
            return Collections.singletonList("World save storage is unavailable.");
        Snapshot summary = profiler.snapshot();
        if (summary.count == 0)
            return Collections.singletonList("No samples yet. Let the manager tick before exporting.");
        long now = System.nanoTime();
        if (lastWriteNs != 0 && now - lastWriteNs < 1_000_000_000L)
            return Collections.singletonList("Profile dumps are limited to one per second. Try again shortly.");
        lastWriteNs = now;
        int dimension = world.provider.dimensionId;
        String name = "manager-" + dimension
            + "_"
            + manager.xCoord
            + "_"
            + manager.yCoord
            + "_"
            + manager.zCoord
            + ".json";
        Path directory = save.getWorldDirectory()
            .toPath()
            .resolve("sfm-profiles");
        try {
            write(
                directory,
                name,
                dimension,
                manager.xCoord,
                manager.yCoord,
                manager.zCoord,
                profiler,
                summary,
                profiler.report(sourceMap));
            return Collections.singletonList("Saved sfm-profiles/" + name);
        } catch (IOException | SecurityException failure) {
            SuperFactoryManager.LOG.warn("Could not save SFM profile " + name, failure);
            return Collections.singletonList("Could not save the profile dump. See the server log.");
        }
    }

    private static void write(Path directory, String name, int dimension, int x, int y, int z,
        ManagerExecutionProfiler profiler, Snapshot summary, Report report) throws IOException {
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "profile-", ".tmp");
        try {
            try (JsonWriter out = new JsonWriter(Files.newBufferedWriter(temporary, StandardCharsets.UTF_8))) {
                out.beginObject();
                out.name("format_version")
                    .value(1);
                out.name("exported_at_utc")
                    .value(
                        Instant.now()
                            .toString());
                out.name("dimension")
                    .value(dimension);
                out.name("x")
                    .value(x);
                out.name("y")
                    .value(y);
                out.name("z")
                    .value(z);
                out.name("collecting")
                    .value(summary.collecting);
                out.name("update_limit")
                    .value(summary.updateLimit);
                out.name("updates")
                    .value(summary.count);
                out.name("due_updates")
                    .value(summary.dueUpdates);
                out.name("program_updates")
                    .value(summary.programUpdates);
                out.name("update_mean_ms")
                    .value(summary.averageMs);
                out.name("update_p50_ms")
                    .value(summary.p50Ms);
                out.name("update_p95_ms")
                    .value(summary.p95Ms);
                out.name("update_p99_ms")
                    .value(summary.p99Ms);
                out.name("update_max_ms")
                    .value(summary.maximumMs);
                out.name("program_mean_ms")
                    .value(summary.programAverageMs);
                out.name("program_max_ms")
                    .value(summary.programMaximumMs);
                out.name("skipped_trigger_attempts")
                    .value(report.skippedTriggers);
                out.name("skipped_statement_attempts")
                    .value(report.skippedStatements);
                out.name("samples");
                profiler.writeSamples(out);
                out.name("triggers");
                writeHotspots(out, report.triggers);
                out.name("statements_inclusive");
                writeHotspots(out, report.statements);
                out.endObject();
            }
            Files.move(
                temporary,
                directory.resolve(name),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void writeHotspots(JsonWriter out, List<Hotspot> rows) throws IOException {
        out.beginArray();
        for (Hotspot row : rows) {
            out.beginObject();
            SourceLocation at = row.location;
            if (at == null) {
                out.name("trigger")
                    .value(row.trigger);
                out.name("resumes")
                    .value(row.resumes());
            } else {
                out.name("line")
                    .value(at.getLine());
                out.name("column")
                    .value(at.getColumn() + 1);
                out.name("kind")
                    .value(at.getStatementKind());
                if (at.isModuleLocation()) {
                    out.name("module")
                        .value(at.getModuleAlias());
                    out.name("revision")
                        .value(at.getModuleRevision());
                }
            }
            out.name("attempts")
                .value(row.attempts());
            out.name("total_ns")
                .value(row.totalNs());
            out.name("max_ns")
                .value(row.maximumNs());
            out.endObject();
        }
        out.endArray();
    }
}
