package ca.teamdman.sfm.compat.ae2;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.mojang.authlib.GameProfile;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.security.PlayerSource;
import appeng.api.parts.IPartHost;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.IInterfaceHost;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import ca.teamdman.sfm.common.storage.StorageBackend;
import ca.teamdman.sfm.common.tileentity.TileEntityModAdapter;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.data.QueryBudget;

/** Optional AE2 boundary. Uses the interface's authorized monitor, never its nine physical buffer slots. */
public final class Ae2StorageBackend implements StorageBackend {

    private static final int MAX_ENTRIES = 1024;
    private final TileEntityModAdapter adapter;
    private final UUID owner;
    private final ForgeDirection side;
    private final Object endpoint;
    private final IGrid grid;
    private final PlayerSource source;
    private final IMEMonitor<IAEItemStack> items;
    private final IMEMonitor<IAEFluidStack> fluids;
    private final IEnergyGrid energy;

    private Ae2StorageBackend(TileEntityModAdapter adapter, Object endpoint, IGrid grid, PlayerSource source,
        IMEMonitor<IAEItemStack> items, IMEMonitor<IAEFluidStack> fluids) {
        this.adapter = adapter;
        owner = adapter.getOwner();
        side = adapter.targetSide();
        this.endpoint = endpoint;
        this.grid = grid;
        this.source = source;
        this.items = items;
        this.fluids = fluids;
        energy = grid.getCache(IEnergyGrid.class);
    }

    public static StorageBackend open(TileEntityModAdapter adapter, QueryBudget budget, Consumer<String> diagnostic) {
        World world = adapter.getWorldObj();
        if (!ca.teamdman.sfm.common.net.ServerUiTasks.isServerThread() || !(world instanceof WorldServer)
            || world.isRemote) return unavailable(diagnostic, "server");
        if (adapter.getOwner() == null) return unavailable(diagnostic, "owner");
        Object endpoint = endpoint(adapter, adapter.targetSide(), budget);
        if (!(endpoint instanceof IActionHost host) || !(endpoint instanceof ITileStorageMonitorable monitorable)
            || !(endpoint instanceof IInterfaceHost iface)) return unavailable(diagnostic, "interface");
        if (iface.getInterfaceDuality()
            .hasConfig()) return unavailable(diagnostic, "stocking");
        budget.chargeProviders(7);
        IGridNode node = host.getActionableNode();
        if (node == null || !node.isActive()) return unavailable(diagnostic, "channel");
        IGrid grid = node.getGrid();
        if (grid == null) return unavailable(diagnostic, "channel");
        EntityPlayer player = world.func_152378_a(adapter.getOwner());
        if (player == null) player = FakePlayerFactory
            .get((WorldServer) world, new GameProfile(adapter.getOwner(), adapter.getOwnerName()));
        PlayerSource source = new PlayerSource(player, null);
        IStorageMonitorable access = monitorable.getMonitorable(
            adapter.targetSide()
                .getOpposite(),
            source);
        // AE returns a local buffer fallback on denied or inactive access. Never present that as network storage.
        if (access != endpoint) return unavailable(diagnostic, "denied");
        budget.chargeAllocation(2);
        Ae2StorageBackend backend = new Ae2StorageBackend(
            adapter,
            endpoint,
            grid,
            source,
            access.getItemInventory(),
            access.getFluidInventory());
        String problem = backend.accessProblem(budget);
        return problem == null ? backend : unavailable(diagnostic, problem);
    }

    private static StorageBackend unavailable(Consumer<String> diagnostic, String problem) {
        if (diagnostic != null) diagnostic.accept(problem);
        return null;
    }

    private static Object endpoint(TileEntityModAdapter adapter, ForgeDirection side, QueryBudget budget) {
        budget.chargePositions(2);
        budget.chargeProviders(6);
        World world = adapter.getWorldObj();
        BlockPos own = new BlockPos(adapter.xCoord, adapter.yCoord, adapter.zCoord), target = own.offset(side);
        if (side == ForgeDirection.UNKNOWN || !own.isLoaded(world)
            || own.getTileEntity(world) != adapter
            || adapter.isInvalid()
            || !target.isLoaded(world)) return null;
        TileEntity tile = target.getTileEntity(world);
        if (tile == null || tile.isInvalid()) return null;
        return tile instanceof IPartHost host ? host.getPart(side.getOpposite()) : tile;
    }

    @Override
    public Object identity() {
        return grid;
    }

    @Override
    public boolean supportsItems() {
        return items != null;
    }

    @Override
    public boolean supportsFluids() {
        return fluids != null;
    }

    @Override
    public boolean supportsBulkItems() {
        return true;
    }

    @Override
    public boolean current(QueryBudget budget) {
        return accessProblem(budget) == null;
    }

    private String accessProblem(QueryBudget budget) {
        if (!ca.teamdman.sfm.common.net.ServerUiTasks.isServerThread() || adapter.getWorldObj().isRemote
            || !owner.equals(adapter.getOwner())
            || side != adapter.targetSide()
            || endpoint(adapter, side, budget) != endpoint) return "changed";
        budget.chargeProviders(12);
        IGridNode node = ((IActionHost) endpoint).getActionableNode();
        if (node == null || !node.isActive() || node.getGrid() != grid) return "channel";
        if (((IInterfaceHost) endpoint).getInterfaceDuality()
            .hasConfig()) return "stocking";
        ISecurityGrid security = grid.getCache(ISecurityGrid.class);
        if (security == null || !security.hasPermission(source.player, SecurityPermissions.BUILD)
            || !security.hasPermission(source.player, SecurityPermissions.EXTRACT)
            || !security.hasPermission(source.player, SecurityPermissions.INJECT)) return "permissions";
        IStorageMonitorable access = ((ITileStorageMonitorable) endpoint).getMonitorable(side.getOpposite(), source);
        if (access != endpoint) return "denied";
        if (access.getItemInventory() != items || access.getFluidInventory() != fluids) return "changed";
        return energy == null || !energy.isNetworkPowered() ? "power" : null;
    }

    private void requireCurrent(QueryBudget budget) {
        if (!current(budget))
            throw new IllegalArgumentException("ME adapter unavailable or its network access changed");
    }

    @Override
    public List<StoredItem> items(QueryBudget budget) {
        requireCurrent(budget);
        if (items == null) throw new IllegalArgumentException("ME interface does not expose item storage");
        budget.chargeAllocation(1);
        List<StoredItem> result = new ArrayList<>();
        for (IAEItemStack entry : items.getStorageList()) {
            budget.chargeSlot();
            long amount = entry.getStackSize();
            if (amount <= 0) continue; // Craftable-only entries are not stored stock.
            if (result.size() == MAX_ENTRIES) throw new IllegalArgumentException("ME item snapshot exceeds 1024 types");
            budget.chargeAllocation(4);
            ItemStack stack = entry.getItemStack();
            stack.stackSize = 1;
            result.add(new StoredItem(stack, amount));
        }
        requireCurrent(budget);
        return result;
    }

    @Override
    public List<StoredFluid> fluids(QueryBudget budget) {
        requireCurrent(budget);
        if (fluids == null) throw new IllegalArgumentException("ME interface does not expose fluid storage");
        budget.chargeAllocation(1);
        List<StoredFluid> result = new ArrayList<>();
        for (IAEFluidStack entry : fluids.getStorageList()) {
            budget.chargeSlot();
            long amount = entry.getStackSize();
            if (amount <= 0) continue;
            if (result.size() == MAX_ENTRIES)
                throw new IllegalArgumentException("ME fluid snapshot exceeds 1024 types");
            budget.chargeAllocation(4);
            FluidStack stack = entry.getFluidStack();
            stack.amount = 1;
            result.add(new StoredFluid(stack, amount));
        }
        requireCurrent(budget);
        return result;
    }

    @Override
    public ItemStack extract(ItemStack requested, boolean simulate, QueryBudget budget) {
        requireCurrent(budget);
        budget.chargeProviders(2);
        IAEItemStack found = Platform.poweredExtraction(
            energy,
            items,
            AEItemStack.create(requested),
            source,
            simulate ? Actionable.SIMULATE : Actionable.MODULATE);
        return found == null ? null : found.getItemStack();
    }

    @Override
    public int insert(ItemStack offered, boolean simulate, QueryBudget budget) {
        requireCurrent(budget);
        budget.chargeProviders(2);
        IAEItemStack request = AEItemStack.create(offered);
        IAEItemStack remainder = Platform
            .poweredInsert(energy, items, request, source, simulate ? Actionable.SIMULATE : Actionable.MODULATE);
        if (remainder != null && !remainder.isSameType(request))
            throw new IllegalArgumentException("Invalid ME item insertion remainder");
        return accepted(offered.stackSize, remainder == null ? 0 : remainder.getStackSize());
    }

    @Override
    public FluidStack drain(FluidStack requested, boolean simulate, QueryBudget budget) {
        requireCurrent(budget);
        budget.chargeProviders(2);
        IAEFluidStack found = Platform.poweredExtraction(
            energy,
            fluids,
            AEFluidStack.create(requested),
            source,
            simulate ? Actionable.SIMULATE : Actionable.MODULATE);
        return found == null ? null : found.getFluidStack();
    }

    @Override
    public int fill(FluidStack offered, boolean simulate, QueryBudget budget) {
        requireCurrent(budget);
        budget.chargeProviders(2);
        IAEFluidStack request = AEFluidStack.create(offered);
        IAEFluidStack remainder = Platform
            .poweredInsert(energy, fluids, request, source, simulate ? Actionable.SIMULATE : Actionable.MODULATE);
        if (remainder != null && !remainder.isSameType(request))
            throw new IllegalArgumentException("Invalid ME fluid insertion remainder");
        return accepted(offered.amount, remainder == null ? 0 : remainder.getStackSize());
    }

    private static int accepted(int amount, long remainder) {
        if (remainder < 0 || remainder > amount) throw new IllegalArgumentException("Invalid ME insertion result");
        return amount - (int) remainder;
    }
}
