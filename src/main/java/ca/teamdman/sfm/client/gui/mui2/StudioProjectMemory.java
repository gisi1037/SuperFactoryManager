package ca.teamdman.sfm.client.gui.mui2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/** Bounded client preference only. A remembered ID must still be loaded through the authenticated server. */
public final class StudioProjectMemory {

    private static final int MAX_SCOPES = 128;
    private final Path path;
    private final LinkedHashMap<String, UUID> selected = new LinkedHashMap<>();

    public StudioProjectMemory(Path path) {
        this.path = path;
        if (!Files.isRegularFile(path)) return;
        try {
            if (Files.size(path) > 65536) return;
            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(path)) {
                properties.load(input);
            }
            for (String scope : properties.stringPropertyNames()) {
                if (selected.size() >= MAX_SCOPES) break;
                try {
                    selected.put(scope, UUID.fromString(properties.getProperty(scope)));
                } catch (IllegalArgumentException ignored) {
                    // A corrupt preference never supplies project data or changes a draft.
                }
            }
        } catch (IOException | IllegalArgumentException ignored) {
            // Failure to remember a selection must never prevent authoring or recovery.
        }
    }

    public static String scope(String worldOrServer, UUID player) {
        return worldOrServer + "|" + player;
    }

    public UUID selected(String scope) {
        return selected.get(scope);
    }

    public void remember(String scope, UUID project) {
        if (project == null ? !selected.containsKey(scope) : project.equals(selected.get(scope))) return;
        selected.remove(scope);
        if (project != null) selected.put(scope, project);
        while (selected.size() > MAX_SCOPES) selected.remove(
            selected.keySet()
                .iterator()
                .next());
        Properties properties = new Properties();
        for (Map.Entry<String, UUID> entry : selected.entrySet()) properties.setProperty(
            entry.getKey(),
            entry.getValue()
                .toString());
        try {
            Path parent = path.toAbsolutePath()
                .getParent();
            Files.createDirectories(parent);
            Path temporary = Files.createTempFile(parent, "studio-projects-", ".tmp");
            try {
                try (OutputStream output = Files.newOutputStream(temporary)) {
                    properties.store(output, "SFM Studio project selection; no source or credentials");
                }
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException ignored) {
            // The in-memory preference remains usable when the config directory is read-only.
        }
    }
}
