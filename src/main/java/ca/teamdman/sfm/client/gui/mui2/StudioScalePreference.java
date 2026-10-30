package ca.teamdman.sfm.client.gui.mui2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** Client preference and pure sizing rules; zero follows the game without changing its settings. */
public final class StudioScalePreference {

    public static final int MAX_SCALE = 16;
    private final Path path;
    private int preferred;

    public StudioScalePreference(Path path) {
        this.path = path;
        try {
            if (!Files.isRegularFile(path) || Files.size(path) > 4096) return;
            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(path)) {
                properties.load(input);
            }
            int value = Integer.parseInt(properties.getProperty("guiScale", "0"));
            if (value >= 0 && value <= MAX_SCALE) preferred = value;
        } catch (IOException | IllegalArgumentException ignored) {
            // Invalid or unavailable preferences must never prevent opening an existing draft.
        }
    }

    public int preferred() {
        return preferred;
    }

    /** Preserve at least the Studio's supported 320 x 240 logical workspace. */
    public static int maximum(int displayWidth, int displayHeight) {
        return Math.max(1, Math.min(MAX_SCALE, Math.min(displayWidth / 320, displayHeight / 240)));
    }

    public int effective(int displayWidth, int displayHeight, int gameScale) {
        return preferred == 0 ? Math.max(1, gameScale) : Math.min(preferred, maximum(displayWidth, displayHeight));
    }

    public static int logicalSize(int gameSize, float ratio) {
        return Math.max(1, (int) Math.ceil(gameSize / (double) ratio));
    }

    public void remember(int value) {
        if (value < 0 || value > MAX_SCALE) throw new IllegalArgumentException("Invalid Studio scale");
        if (preferred == value) return;
        preferred = value;
        Properties properties = new Properties();
        properties.setProperty("guiScale", Integer.toString(value));
        try {
            Path parent = path.toAbsolutePath()
                .getParent();
            Files.createDirectories(parent);
            Path temporary = Files.createTempFile(parent, "sfm-studio-", ".tmp");
            try {
                try (OutputStream output = Files.newOutputStream(temporary)) {
                    properties.store(output, "SFM Studio only; 0 follows the game GUI scale");
                }
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException ignored) {
            // Retain the in-memory choice even when the client config directory is read-only.
        }
    }
}
