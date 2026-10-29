package ca.teamdman.sfm.common.program;

import ca.teamdman.sfm.common.gui.ManagerEditContext;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

/** Server-thread edit transactions; packet callers must first validate container access. */
public final class ProgramEditService {

    private ProgramEditService() {}

    public static ProgramEditSnapshot open(ManagerEditContext container) {
        TileEntityManager manager = container.getManager();
        if (manager.getDisk() == null || !(manager.getDisk()
            .getItem() instanceof ItemDisk)) throw new IllegalStateException("Insert a program disk before editing");
        ProgramDiskData.requireLegacy(manager.getDisk());
        String source = ItemDisk.getProgramString(manager.getDisk());
        // Reject an untransportable legacy/foreign document before claiming OPEN succeeded.
        ProgramDocument.validateSource(source);
        manager.flushProgramState();
        ProgramDocument document = ProgramDocument.ensure(manager.getDisk());
        manager.markDirty();
        return new ProgramEditSnapshot(
            container.getEditSessionId(),
            manager.getDiskGeneration(),
            document.id,
            document.revision,
            source);
    }

    public static SaveResult save(ManagerEditContext container, ProgramEditSnapshot expected, String source) {
        TileEntityManager manager = container.getManager();
        try {
            if (expected == null || !container.getEditSessionId()
                .equals(expected.sessionId)
                || manager.getDiskGeneration() != expected.bindingGeneration
                || manager.getDisk() == null)
                return SaveResult.rejected("Disk or editing session changed. Keep your draft and reopen the editor.");
            ProgramDiskData.requireLegacy(manager.getDisk());
            ProgramDocument document = ProgramDocument.read(manager.getDisk());
            if (document == null || !document.id.equals(expected.documentId)
                || document.revision != expected.revision
                || !ItemDisk.getProgramString(manager.getDisk())
                    .equals(expected.source))
                return SaveResult.rejected("Program changed while editing. Keep your draft and reopen the editor.");
            String error = manager.trySaveProgram(source);
            if (error != null) return SaveResult.rejected(error);
            return new SaveResult(true, open(container), "Program saved");
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException error) {
            return SaveResult.rejected(error.getMessage());
        }
    }

    public static final class SaveResult {

        public final boolean accepted;
        public final ProgramEditSnapshot snapshot;
        public final String message;

        private SaveResult(boolean accepted, ProgramEditSnapshot snapshot, String message) {
            this.accepted = accepted;
            this.snapshot = snapshot;
            this.message = message;
        }

        public static SaveResult rejected(String message) {
            return new SaveResult(false, null, message);
        }
    }
}
