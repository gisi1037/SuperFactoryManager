package ca.teamdman.sfml.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.common.util.ForgeDirection;

import ca.teamdman.sfm.common.inventory.InventoryUtils;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.util.BlockPos;

public final class BoolHas implements BoolExpr {

    private final SetOperator setOperator;
    private final LabelAccess labelAccess;
    private final ComparisonOperator comparisonOperator;
    private final ResourceQuantity quantity;
    private final ResourceIdSet resourceIds;
    private final With with;
    private final ResourceIdSet except;

    public BoolHas(SetOperator setOperator, LabelAccess labelAccess, ComparisonOperator comparisonOperator,
        long quantity, ResourceIdSet resourceIds, With with, ResourceIdSet except) {
        this(
            setOperator,
            labelAccess,
            comparisonOperator,
            new ResourceQuantity(quantity, false),
            resourceIds,
            with,
            except);
    }

    public BoolHas(SetOperator setOperator, LabelAccess labelAccess, ComparisonOperator comparisonOperator,
        ResourceQuantity quantity, ResourceIdSet resourceIds, With with, ResourceIdSet except) {
        this.setOperator = setOperator != null ? setOperator : SetOperator.OVERALL;
        this.labelAccess = labelAccess;
        this.comparisonOperator = comparisonOperator != null ? comparisonOperator : ComparisonOperator.GREATER;
        this.quantity = quantity;
        this.resourceIds = resourceIds != null ? resourceIds : ResourceIdSet.MATCH_ALL;
        this.with = with != null ? with : With.ALWAYS_TRUE;
        this.except = except;
    }

    @Override
    public boolean evaluate(ProgramContext context) {
        long threshold = quantity.evaluate(context);
        if (labelAccess == null) return false;

        LabelAccess access = labelAccess.resolve(context);
        List<BlockPos> positions = access.getPositions(context.getLabelPositionHolder());
        long overallCount = 0;
        List<Boolean> satisfactionResults = new ArrayList<>();
        Set<TileEntityChest> seenChests = Collections.newSetFromMap(new IdentityHashMap<>());

        for (BlockPos pos : positions) {
            if (context.getNetwork() == null || !context.getNetwork()
                .isConnected(pos)) continue;
            if (context.getWorld() != null) {
                TileEntity te = context.getWorld()
                    .getTileEntity(pos.x, pos.y, pos.z);
                if (te instanceof TileEntityChest) {
                    TileEntityChest c = (TileEntityChest) te;
                    if (seenChests.contains(c)) {
                        continue;
                    }
                    seenChests.add(c);
                    TileEntityChest adj = InventoryUtils.getAdjacentChest(c);
                    if (adj != null) seenChests.add(adj);
                }
            }
            long inThisBlock = 0;
            if (resourceIds.hasItemType()) {
                IInventory inventory = InventoryUtils.getInventory(context.getWorld(), pos);
                List<ForgeDirection> sides = access.getSides(pos, context.getLabelPositionHolder())
                    .resolveDirections();
                if (inventory != null && !sides.isEmpty()) {
                    if (!(inventory instanceof ISidedInventory))
                        sides = Collections.singletonList(ForgeDirection.UNKNOWN);
                    Set<Integer> seen = new java.util.HashSet<>();
                    for (ForgeDirection side : sides) {
                        for (int slot : InventoryUtils.getAccessibleSlots(
                            inventory,
                            side,
                            access.getSlots(pos, side, context.getLabelPositionHolder()))) {
                            if (!seen.add(slot)) continue;
                            ItemStack stack = inventory.getStackInSlot(slot);
                            if (stack == null || except != null && except.matchesItem(stack)
                                || !resourceIds.matchesItem(stack)
                                || !with.matches(stack)) continue;
                            inThisBlock += stack.stackSize;
                        }
                    }
                }
            }
            if (resourceIds.hasFluidType()) {
                inThisBlock += InventoryUtils.countMatchingFluids(
                    context.getWorld(),
                    pos,
                    access.getFluidSides(pos, context.getLabelPositionHolder()),
                    resourceIds,
                    except);
            }

            overallCount += inThisBlock;
            satisfactionResults.add(comparisonOperator.test(inThisBlock, threshold));
        }

        boolean overallSatisfied = comparisonOperator.test(overallCount, threshold);
        return setOperator.test(overallSatisfied, satisfactionResults);
    }

    public SetOperator getSetOperator() {
        return setOperator;
    }

    public LabelAccess getLabelAccess() {
        return labelAccess;
    }

    public ComparisonOperator getComparisonOperator() {
        return comparisonOperator;
    }

    public long getQuantity() {
        return quantity.getAmount();
    }

    public ResourceIdSet getResourceIds() {
        return resourceIds;
    }

    public With getWith() {
        return with;
    }

    public ResourceIdSet getExcept() {
        return except;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (setOperator != null) {
            sb.append(setOperator)
                .append(" ");
        }
        if (labelAccess != null) {
            sb.append(labelAccess)
                .append(" ");
        }
        sb.append("HAS");
        if (comparisonOperator != null) {
            sb.append(" ")
                .append(comparisonOperator.getSymbol());
        }
        sb.append(" ")
            .append(quantity);
        if (resourceIds != null && resourceIds != ResourceIdSet.MATCH_ALL) {
            sb.append(" ")
                .append(resourceIds);
        }
        if (with != null && with != With.ALWAYS_TRUE) {
            sb.append(" ")
                .append(with);
        }
        if (except != null) {
            sb.append(" EXCEPT ")
                .append(except);
        }
        return sb.toString();
    }
}
