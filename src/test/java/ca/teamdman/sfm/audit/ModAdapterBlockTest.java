package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import com.mojang.authlib.GameProfile;

import ca.teamdman.sfm.common.block.BlockModAdapter;
import ca.teamdman.sfm.common.tileentity.TileEntityModAdapter;
import ca.teamdman.sfml.data.QueryBudget;

class ModAdapterBlockTest {

    @Test
    void placingAgainstAnInterfaceTargetsItsFace() {
        BlockModAdapter block = new BlockModAdapter();
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            assertEquals(
                side.getOpposite()
                    .ordinal(),
                block.onBlockPlaced(null, 0, 64, 0, side.ordinal(), 0, 0, 0, 0));
        }
    }

    @Test
    void savedOwnerSurvivesReloadAndMalformedOwnershipHasNoAccess() {
        TileEntity.addMapping(TileEntityModAdapter.class, "sfm.modAdapter");
        UUID owner = UUID.randomUUID();
        TileEntityModAdapter placed = new TileEntityModAdapter();
        placed.setOwner(new GameProfile(owner, "AdapterOwner"));
        placed.blockMetadata = ForgeDirection.EAST.ordinal();
        assertEquals(ForgeDirection.EAST, placed.targetSide());
        assertFalse(placed.canUpdate());

        NBTTagCompound saved = new NBTTagCompound();
        placed.writeToNBT(saved);
        TileEntityModAdapter restored = new TileEntityModAdapter();
        restored.readFromNBT(saved);
        assertEquals(owner, restored.getOwner());
        assertEquals("AdapterOwner", restored.getOwnerName());

        saved.setString("sfm:adapterOwner", "invalid");
        restored.readFromNBT(saved);
        assertNull(restored.getOwner());
        assertNull(restored.open(new QueryBudget()));
    }

    @Test
    void accessCheckReportsOwnerAndServerPrerequisites() {
        TileEntityModAdapter adapter = new TileEntityModAdapter();
        java.util.List<String> problems = new java.util.ArrayList<>();
        assertNull(adapter.open(new QueryBudget(), problems::add));
        adapter.setOwner(new GameProfile(UUID.randomUUID(), "AdapterOwner"));
        assertNull(adapter.open(new QueryBudget(), problems::add));
        assertEquals(java.util.Arrays.asList("owner", "server"), problems);
    }
}
