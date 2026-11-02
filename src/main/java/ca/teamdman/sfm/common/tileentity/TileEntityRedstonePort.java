package ca.teamdman.sfm.common.tileentity;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import ca.teamdman.sfm.common.block.BlockRedstonePort;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.util.LoadedRedstoneAccess;

/** Six physical input/output faces. Output levels and active pulse deadlines belong to saved block data. */
public class TileEntityRedstonePort extends TileEntity {

    /** Conservative upper bound for our loaded checks, block reads and up to 42 power callbacks. */
    public static final int MAX_INPUT_PROVIDER_CALLS = 4096;
    private static final String OUTPUT_TAG = "sfm:outputs";
    private static final String PULSE_TAG = "sfm:pulseEnd";
    private static final AtomicLong NEXT_EPOCH = new AtomicLong(1);

    private final int[] outputs = new int[6];
    // Absolute world ticks, zero when inactive. Six bounded timers; no retained program/controller.
    private final long[] pulseEnds = new long[6];
    private final int[] inputs = new int[6];
    private final int[] sampledInputs = new int[6];
    private final long[] rising = new long[6];
    private final long[] falling = new long[6];
    private final LoadedRedstoneAccess readAccess = new LoadedRedstoneAccess();
    private int availableMask;
    private long signalEpoch = NEXT_EPOCH.getAndIncrement();
    private boolean initialized;
    private boolean refreshing;
    private boolean reassertOutputs;
    private String inputFailure;

    @Override
    public boolean canUpdate() {
        return false;
    }

    public int getInputLevel(int side) {
        return validSide(side) && isInputAvailable(side) ? inputs[side] : 0;
    }

    public int getOutputLevel(int side) {
        return validSide(side) ? outputs[side] : 0;
    }

    public boolean isInputAvailable(int side) {
        return validSide(side) && (availableMask & 1 << side) != 0;
    }

    public long getRisingCount(int side) {
        return validSide(side) ? rising[side] : 0;
    }

    public long getFallingCount(int side) {
        return validSide(side) ? falling[side] : 0;
    }

    public long getSignalEpoch() {
        return signalEpoch;
    }

    public boolean isInputInitialized() {
        return initialized;
    }

    public String getInputFailure() {
        return inputFailure;
    }

    /** Neighbor callbacks record even short pulses; a query also reconciles providers that missed a notification. */
    public void refreshInputs() {
        try {
            refreshInputs(null);
        } catch (RuntimeException error) {
            // An invalid peripheral must not turn an ordinary neighbor update into a world crash.
            resetSignals();
            initialized = true;
            inputFailure = error.getClass()
                .getSimpleName();
            Arrays.fill(inputs, 0);
        }
    }

    /** The optional callback charges a caller's budget before each provider operation, without partial publication. */
    public void refreshInputs(Runnable beforeProviderCall) {
        if (refreshing || !isLiveServerTile(beforeProviderCall)) return;
        refreshing = true;
        readAccess.attach(worldObj, beforeProviderCall);
        try {
            int sampledMask = 0;
            for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
                readAccess.reset();
                int level = sampleInput(direction, beforeProviderCall);
                if (!readAccess.isComplete()) level = -1;
                int side = direction.ordinal();
                sampledInputs[side] = Math.max(0, level);
                if (level >= 0) sampledMask |= 1 << side;
            }
            // Availability gaps are lifecycle changes, never invented falling/rising edges.
            // A new epoch also prevents a manager replaying old counters after an unseen gap.
            if (!initialized || sampledMask != availableMask || countersWouldOverflow(sampledMask)) {
                resetSignals();
                initialized = true;
            } else {
                for (int side = 0; side < 6; side++) {
                    if ((sampledMask & 1 << side) == 0) continue;
                    if (inputs[side] == 0 && sampledInputs[side] > 0) rising[side]++;
                    else if (inputs[side] > 0 && sampledInputs[side] == 0) falling[side]++;
                }
            }
            availableMask = sampledMask;
            System.arraycopy(sampledInputs, 0, inputs, 0, inputs.length);
            inputFailure = null;
        } finally {
            readAccess.detach();
            refreshing = false;
        }
    }

    private boolean countersWouldOverflow(int sampledMask) {
        for (int side = 0; side < 6; side++) {
            if ((sampledMask & 1 << side) == 0) continue;
            if ((inputs[side] == 0 && sampledInputs[side] > 0 && rising[side] == Long.MAX_VALUE)
                || (inputs[side] > 0 && sampledInputs[side] == 0 && falling[side] == Long.MAX_VALUE)) return true;
        }
        return false;
    }

    /** Vanilla direction arguments describe the receiver's face, opposite the source's physical output face. */
    private int sampleInput(ForgeDirection direction, Runnable beforeProviderCall) {
        int x = xCoord + direction.offsetX;
        int y = yCoord + direction.offsetY;
        int z = zCoord + direction.offsetZ;
        before(beforeProviderCall);
        if (!worldObj.blockExists(x, y, z)) return -1;
        before(beforeProviderCall);
        Block neighbor = worldObj.getBlock(x, y, z);
        before(beforeProviderCall);
        if (!neighbor.shouldCheckWeakPower(readAccess, x, y, z, direction.ordinal())) {
            before(beforeProviderCall);
            return clamp(neighbor.isProvidingWeakPower(readAccess, x, y, z, direction.ordinal()));
        }

        // World.getIndirectPowerLevelTo would scan these six positions without loaded checks.
        // Reproduce its solid-block conduction while keeping our own accesses inside loaded chunks.
        int level = 0;
        boolean complete = true;
        for (ForgeDirection around : ForgeDirection.VALID_DIRECTIONS) {
            int sourceX = x + around.offsetX;
            int sourceY = y + around.offsetY;
            int sourceZ = z + around.offsetZ;
            before(beforeProviderCall);
            if (!worldObj.blockExists(sourceX, sourceY, sourceZ)) {
                complete = false;
                continue;
            }
            before(beforeProviderCall);
            Block source = worldObj.getBlock(sourceX, sourceY, sourceZ);
            before(beforeProviderCall);
            level = Math.max(
                level,
                clamp(source.isProvidingStrongPower(readAccess, sourceX, sourceY, sourceZ, around.ordinal())));
        }
        return complete ? level : -1;
    }

    /** Installs the entire batch before notifying observers. Null/empty sides selects all six faces. */
    public boolean setOutput(List<ForgeDirection> sides, int strength) {
        if (strength < 0 || strength > 15 || !isLiveServerTile()) return false;
        int selectedMask = sideMask(sides);
        if (selectedMask == 0) return false;
        boolean cancelled = false;
        for (int side = 0; side < 6; side++) {
            if ((selectedMask & 1 << side) != 0 && pulseEnds[side] != 0) {
                pulseEnds[side] = 0;
                cancelled = true;
            }
        }
        // Even an unchanged explicit level takes ownership from a pending pulse.
        if (cancelled) markDirty();
        return writeOutputs(selectedMask, strength);
    }

    /** Latest command wins per face. The off edge runs independently of the program's next activation. */
    public boolean pulse(List<ForgeDirection> sides, long ticks, int strength) {
        if (ticks < 1 || ticks > Integer.MAX_VALUE || strength < 1 || strength > 15)
            throw new IllegalArgumentException("PULSE requires 1..2147483647 game ticks and strength 1..15");
        if (!isLiveServerTile()) return false;
        int mask = sideMask(sides);
        if (mask == 0) throw new IllegalArgumentException("PULSE requires absolute physical sides");
        long now = worldObj.getTotalWorldTime();
        if (now < 0 || now > Long.MAX_VALUE - ticks)
            throw new IllegalArgumentException("PULSE deadline exceeds world time range");
        for (int side = 0; side < 6; side++) if ((mask & 1 << side) != 0) pulseEnds[side] = now + ticks;
        markDirty();
        // Schedule before notifications, which can synchronously replace/remove the port or write an output.
        schedulePulseTick();
        writeOutputs(mask, strength);
        return true;
    }

    private static int sideMask(List<ForgeDirection> sides) {
        if (sides == null || sides.isEmpty()) return 63;
        int mask = 0;
        for (ForgeDirection side : sides) {
            if (side == null || side == ForgeDirection.UNKNOWN) return 0;
            mask |= 1 << side.ordinal();
        }
        return mask;
    }

    private boolean writeOutputs(int selectedMask, int strength) {
        int changedMask = 0;
        for (int side = 0; side < 6; side++) {
            if ((selectedMask & 1 << side) != 0 && outputs[side] != strength) {
                outputs[side] = strength;
                changedMask |= 1 << side;
            }
        }
        if (changedMask == 0) return false;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        BlockRedstonePort.notifyOutputNeighbors(worldObj, xCoord, yCoord, zCoord, getBlockType(), changedMask);
        // Strongly powered neighbors can legitimately feed this port's own outputs back into its inputs.
        refreshInputs();
        return true;
    }

    private void expirePulses() {
        boolean active = false;
        for (long end : pulseEnds) active |= end != 0;
        if (!active) return;
        if (!isLiveServerTile()) return;
        long now = worldObj.getTotalWorldTime();
        int expired = 0;
        for (int side = 0; side < 6; side++) {
            if (pulseEnds[side] != 0 && pulseEnds[side] <= now) {
                pulseEnds[side] = 0;
                expired |= 1 << side;
            }
        }
        if (expired == 0) return;
        markDirty();
        try {
            writeOutputs(expired, 0);
        } catch (RuntimeException invalidNeighbor) {
            // The off levels were installed before notification. A peripheral must not crash a world tick.
            ca.teamdman.sfm.SuperFactoryManager.LOG
                .warn("Redstone pulse ended; neighbor notification failed", invalidNeighbor);
        }
    }

    private void schedulePulseTick() {
        for (long end : pulseEnds) if (end != 0) {
            // Vanilla coalesces pending updates. One active-only chain also handles earlier retriggers correctly.
            worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord), 1);
            return;
        }
    }

    /** Used before removal so synchronous neighbor reads cannot observe a stale powered tile. */
    public int clearOutputsForRemoval() {
        Arrays.fill(pulseEnds, 0);
        int mask = 0;
        for (int side = 0; side < 6; side++) {
            if (outputs[side] != 0) mask |= 1 << side;
            outputs[side] = 0;
        }
        resetSignals();
        return mask;
    }

    /** Chunk-load event hook. Restored signals are reasserted once, separately from pure query sampling. */
    public void onLoaded() {
        resetSignals();
        reassertOutputs = true;
        expirePulses();
        if (worldObj != null && !worldObj.isRemote && worldObj.blockExists(xCoord, yCoord, zCoord))
            worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord), 1);
    }

    public void onScheduledInitialization() {
        expirePulses();
        if (isLiveServerTile()) schedulePulseTick();
        refreshInputs();
        if (!reassertOutputs || !isLiveServerTile()) return;
        reassertOutputs = false;
        int mask = 0;
        for (int side = 0; side < 6; side++) if (outputs[side] != 0) mask |= 1 << side;
        BlockRedstonePort.notifyOutputNeighbors(worldObj, xCoord, yCoord, zCoord, getBlockType(), mask);
    }

    private boolean isLiveServerTile() {
        return isLiveServerTile(null);
    }

    private boolean isLiveServerTile(Runnable beforeProviderCall) {
        if (worldObj == null || worldObj.isRemote || !ServerUiTasks.isServerThread() || isInvalid()) return false;
        before(beforeProviderCall);
        if (!worldObj.blockExists(xCoord, yCoord, zCoord)) return false;
        before(beforeProviderCall);
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this;
    }

    private static void before(Runnable callback) {
        if (callback != null) callback.run();
    }

    @Override
    public void markDirty() {
        // TileEntity.markDirty also performs unguarded comparator-neighbor reads. This block has no comparator output.
        if (worldObj != null && !worldObj.isRemote && worldObj.blockExists(xCoord, yCoord, zCoord))
            worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
    }

    @Override
    public void validate() {
        super.validate();
        resetSignals();
    }

    @Override
    public void onChunkUnload() {
        resetSignals();
        super.onChunkUnload();
    }

    private void resetSignals() {
        signalEpoch = NEXT_EPOCH.getAndIncrement();
        availableMask = 0;
        initialized = false;
        Arrays.fill(rising, 0);
        Arrays.fill(falling, 0);
    }

    private static boolean validSide(int side) {
        return side >= 0 && side < 6;
    }

    private static int clamp(int level) {
        return Math.max(0, Math.min(15, level));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setIntArray(OUTPUT_TAG, outputs.clone());
        for (int side = 0; side < 6; side++) {
            if (pulseEnds[side] != 0) tag.setLong(PULSE_TAG + side, pulseEnds[side]);
            else tag.removeTag(PULSE_TAG + side);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        readOutputs(tag);
        Arrays.fill(pulseEnds, 0);
        for (int side = 0; side < 6; side++) {
            long end = tag.getLong(PULSE_TAG + side);
            if (end > 0 && outputs[side] > 0) pulseEnds[side] = end;
        }
        resetSignals();
    }

    private void readOutputs(NBTTagCompound tag) {
        Arrays.fill(outputs, 0);
        int[] saved = tag.getIntArray(OUTPUT_TAG);
        if (saved.length == outputs.length)
            for (int side = 0; side < outputs.length; side++) outputs[side] = clamp(saved[side]);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setIntArray(OUTPUT_TAG, outputs.clone());
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager network, S35PacketUpdateTileEntity packet) {
        if (worldObj != null && worldObj.isRemote) readOutputs(packet.func_148857_g());
    }
}
