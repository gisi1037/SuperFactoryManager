package ca.teamdman.sfm.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import ca.teamdman.sfm.common.block.BlockCable;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class RenderFancyCable implements ISimpleBlockRenderingHandler {

    private final int renderId;

    // Dimensions in 16ths
    private static final double C_MIN = 4.0 / 16.0; // 0.25
    private static final double C_MAX = 12.0 / 16.0; // 0.75
    private static final double A_MIN = 5.0 / 16.0; // 0.3125
    private static final double A_MAX = 11.0 / 16.0; // 0.6875

    public RenderFancyCable(int renderId) {
        this.renderId = renderId;
    }

    @Override
    public int getRenderId() {
        return renderId;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        Tessellator tess = Tessellator.instance;
        IIcon icon = block.getIcon(0, metadata);
        if (icon == null) return;

        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        tess.startDrawingQuads();

        // Render Core
        renderCore(tess, 0, 0, 0, icon, 0, true);

        // Render West & East arms so it looks like a connected cable in inventory
        renderArmWest(tess, 0, 0, 0, icon, 0, true, false);
        renderArmEast(tess, 0, 0, 0, icon, 0, true, false);

        tess.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        if (!(block instanceof BlockCable)) return false;
        BlockCable cable = (BlockCable) block;
        IIcon icon = block.getIcon(0, world.getBlockMetadata(x, y, z));
        if (icon == null) return false;

        Tessellator tess = Tessellator.instance;
        int brightness = block.getMixedBrightnessForBlock(world, x, y, z);

        boolean connDown = cable.canConnectTo(world, x, y, z, ForgeDirection.DOWN);
        boolean connUp = cable.canConnectTo(world, x, y, z, ForgeDirection.UP);
        boolean connNorth = cable.canConnectTo(world, x, y, z, ForgeDirection.NORTH);
        boolean connSouth = cable.canConnectTo(world, x, y, z, ForgeDirection.SOUTH);
        boolean connWest = cable.canConnectTo(world, x, y, z, ForgeDirection.WEST);
        boolean connEast = cable.canConnectTo(world, x, y, z, ForgeDirection.EAST);

        // Render central core box
        renderCore(tess, x, y, z, icon, brightness, false);

        // Render connection arms
        if (connDown) {
            Block b = world.getBlock(x, y - 1, z);
            boolean skipCap = b.isOpaqueCube() || b instanceof BlockCable;
            renderArmDown(tess, x, y, z, icon, brightness, false, skipCap);
        }
        if (connUp) {
            Block b = world.getBlock(x, y + 1, z);
            boolean skipCap = b.isOpaqueCube() || b instanceof BlockCable;
            renderArmUp(tess, x, y, z, icon, brightness, false, skipCap);
        }
        if (connNorth) {
            Block b = world.getBlock(x, y, z - 1);
            boolean skipCap = b.isOpaqueCube() || b instanceof BlockCable;
            renderArmNorth(tess, x, y, z, icon, brightness, false, skipCap);
        }
        if (connSouth) {
            Block b = world.getBlock(x, y, z + 1);
            boolean skipCap = b.isOpaqueCube() || b instanceof BlockCable;
            renderArmSouth(tess, x, y, z, icon, brightness, false, skipCap);
        }
        if (connWest) {
            Block b = world.getBlock(x - 1, y, z);
            boolean skipCap = b.isOpaqueCube() || b instanceof BlockCable;
            renderArmWest(tess, x, y, z, icon, brightness, false, skipCap);
        }
        if (connEast) {
            Block b = world.getBlock(x + 1, y, z);
            boolean skipCap = b.isOpaqueCube() || b instanceof BlockCable;
            renderArmEast(tess, x, y, z, icon, brightness, false, skipCap);
        }

        return true;
    }

    private void renderCore(Tessellator tess, double x, double y, double z, IIcon icon, int brightness,
        boolean isInventory) {
        double uMin = icon.getInterpolatedU(8.0);
        double uMax = icon.getInterpolatedU(16.0);
        double vMin = icon.getInterpolatedV(0.0);
        double vMax = icon.getInterpolatedV(8.0);

        renderFaceDown(
            tess,
            x,
            y,
            z,
            C_MIN,
            C_MIN,
            C_MIN,
            C_MAX,
            C_MAX,
            uMin,
            vMin,
            uMax,
            vMax,
            brightness,
            isInventory);
        renderFaceUp(tess, x, y, z, C_MIN, C_MAX, C_MIN, C_MAX, C_MAX, uMin, vMin, uMax, vMax, brightness, isInventory);
        renderFaceNorth(
            tess,
            x,
            y,
            z,
            C_MIN,
            C_MAX,
            C_MIN,
            C_MAX,
            C_MIN,
            uMin,
            vMin,
            uMax,
            vMax,
            brightness,
            isInventory);
        renderFaceSouth(
            tess,
            x,
            y,
            z,
            C_MIN,
            C_MAX,
            C_MIN,
            C_MAX,
            C_MAX,
            uMin,
            vMin,
            uMax,
            vMax,
            brightness,
            isInventory);
        renderFaceWest(
            tess,
            x,
            y,
            z,
            C_MIN,
            C_MIN,
            C_MAX,
            C_MIN,
            C_MAX,
            uMin,
            vMin,
            uMax,
            vMax,
            brightness,
            isInventory);
        renderFaceEast(
            tess,
            x,
            y,
            z,
            C_MAX,
            C_MIN,
            C_MAX,
            C_MIN,
            C_MAX,
            uMin,
            vMin,
            uMax,
            vMax,
            brightness,
            isInventory);
    }

    private void renderArmNorth(Tessellator tess, double x, double y, double z, IIcon icon, int brightness,
        boolean isInventory, boolean skipCap) {
        double uArmMin = icon.getInterpolatedU(0.0);
        double uArmMax = icon.getInterpolatedU(5.0);
        double vArmMin = icon.getInterpolatedV(0.0);
        double vArmMax = icon.getInterpolatedV(6.0);

        // Arm sides
        renderFaceUp(
            tess,
            x,
            y,
            z,
            A_MIN,
            A_MAX,
            0.0,
            A_MAX,
            C_MIN,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceDown(
            tess,
            x,
            y,
            z,
            A_MIN,
            A_MIN,
            0.0,
            A_MAX,
            C_MIN,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceWest(
            tess,
            x,
            y,
            z,
            A_MIN,
            A_MIN,
            A_MAX,
            0.0,
            C_MIN,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceEast(
            tess,
            x,
            y,
            z,
            A_MAX,
            A_MIN,
            A_MAX,
            0.0,
            C_MIN,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);

        // End cap
        if (!skipCap) {
            double uCapMin = icon.getInterpolatedU(9.0);
            double uCapMax = icon.getInterpolatedU(15.0);
            double vCapMin = icon.getInterpolatedV(1.0);
            double vCapMax = icon.getInterpolatedV(7.0);
            renderFaceNorth(
                tess,
                x,
                y,
                z,
                A_MIN,
                A_MAX,
                A_MIN,
                A_MAX,
                0.0,
                uCapMin,
                vCapMin,
                uCapMax,
                vCapMax,
                brightness,
                isInventory);
        }
    }

    private void renderArmSouth(Tessellator tess, double x, double y, double z, IIcon icon, int brightness,
        boolean isInventory, boolean skipCap) {
        double uArmMin = icon.getInterpolatedU(0.0);
        double uArmMax = icon.getInterpolatedU(5.0);
        double vArmMin = icon.getInterpolatedV(0.0);
        double vArmMax = icon.getInterpolatedV(6.0);

        // Arm sides
        renderFaceUp(
            tess,
            x,
            y,
            z,
            A_MIN,
            A_MAX,
            C_MAX,
            A_MAX,
            1.0,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceDown(
            tess,
            x,
            y,
            z,
            A_MIN,
            A_MIN,
            C_MAX,
            A_MAX,
            1.0,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceWest(
            tess,
            x,
            y,
            z,
            A_MIN,
            A_MIN,
            A_MAX,
            C_MAX,
            1.0,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceEast(
            tess,
            x,
            y,
            z,
            A_MAX,
            A_MIN,
            A_MAX,
            C_MAX,
            1.0,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);

        // End cap
        if (!skipCap) {
            double uCapMin = icon.getInterpolatedU(9.0);
            double uCapMax = icon.getInterpolatedU(15.0);
            double vCapMin = icon.getInterpolatedV(1.0);
            double vCapMax = icon.getInterpolatedV(7.0);
            renderFaceSouth(
                tess,
                x,
                y,
                z,
                A_MIN,
                A_MAX,
                A_MIN,
                A_MAX,
                1.0,
                uCapMin,
                vCapMin,
                uCapMax,
                vCapMax,
                brightness,
                isInventory);
        }
    }

    private void renderArmWest(Tessellator tess, double x, double y, double z, IIcon icon, int brightness,
        boolean isInventory, boolean skipCap) {
        double uArmMin = icon.getInterpolatedU(0.0);
        double uArmMax = icon.getInterpolatedU(5.0);
        double vArmMin = icon.getInterpolatedV(0.0);
        double vArmMax = icon.getInterpolatedV(6.0);

        // Arm sides
        renderFaceUp(
            tess,
            x,
            y,
            z,
            0.0,
            A_MAX,
            A_MIN,
            C_MIN,
            A_MAX,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceDown(
            tess,
            x,
            y,
            z,
            0.0,
            A_MIN,
            A_MIN,
            C_MIN,
            A_MAX,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceNorth(
            tess,
            x,
            y,
            z,
            0.0,
            C_MIN,
            A_MIN,
            A_MAX,
            A_MIN,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceSouth(
            tess,
            x,
            y,
            z,
            0.0,
            C_MIN,
            A_MIN,
            A_MAX,
            A_MAX,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);

        // End cap
        if (!skipCap) {
            double uCapMin = icon.getInterpolatedU(9.0);
            double uCapMax = icon.getInterpolatedU(15.0);
            double vCapMin = icon.getInterpolatedV(1.0);
            double vCapMax = icon.getInterpolatedV(7.0);
            renderFaceWest(
                tess,
                x,
                y,
                z,
                0.0,
                A_MIN,
                A_MAX,
                A_MIN,
                A_MAX,
                uCapMin,
                vCapMin,
                uCapMax,
                vCapMax,
                brightness,
                isInventory);
        }
    }

    private void renderArmEast(Tessellator tess, double x, double y, double z, IIcon icon, int brightness,
        boolean isInventory, boolean skipCap) {
        double uArmMin = icon.getInterpolatedU(0.0);
        double uArmMax = icon.getInterpolatedU(5.0);
        double vArmMin = icon.getInterpolatedV(0.0);
        double vArmMax = icon.getInterpolatedV(6.0);

        // Arm sides
        renderFaceUp(
            tess,
            x,
            y,
            z,
            C_MAX,
            A_MAX,
            A_MIN,
            1.0,
            A_MAX,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceDown(
            tess,
            x,
            y,
            z,
            C_MAX,
            A_MIN,
            A_MIN,
            1.0,
            A_MAX,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceNorth(
            tess,
            x,
            y,
            z,
            C_MAX,
            1.0,
            A_MIN,
            A_MAX,
            A_MIN,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceSouth(
            tess,
            x,
            y,
            z,
            C_MAX,
            1.0,
            A_MIN,
            A_MAX,
            A_MAX,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);

        // End cap
        if (!skipCap) {
            double uCapMin = icon.getInterpolatedU(9.0);
            double uCapMax = icon.getInterpolatedU(15.0);
            double vCapMin = icon.getInterpolatedV(1.0);
            double vCapMax = icon.getInterpolatedV(7.0);
            renderFaceEast(
                tess,
                x,
                y,
                z,
                1.0,
                A_MIN,
                A_MAX,
                A_MIN,
                A_MAX,
                uCapMin,
                vCapMin,
                uCapMax,
                vCapMax,
                brightness,
                isInventory);
        }
    }

    private void renderArmDown(Tessellator tess, double x, double y, double z, IIcon icon, int brightness,
        boolean isInventory, boolean skipCap) {
        double uArmMin = icon.getInterpolatedU(0.0);
        double uArmMax = icon.getInterpolatedU(5.0);
        double vArmMin = icon.getInterpolatedV(0.0);
        double vArmMax = icon.getInterpolatedV(6.0);

        // Arm sides
        renderFaceNorth(
            tess,
            x,
            y,
            z,
            A_MIN,
            A_MAX,
            0.0,
            C_MIN,
            A_MIN,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceSouth(
            tess,
            x,
            y,
            z,
            A_MIN,
            A_MAX,
            0.0,
            C_MIN,
            A_MAX,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceWest(
            tess,
            x,
            y,
            z,
            A_MIN,
            0.0,
            C_MIN,
            A_MIN,
            A_MAX,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceEast(
            tess,
            x,
            y,
            z,
            A_MAX,
            0.0,
            C_MIN,
            A_MIN,
            A_MAX,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);

        // End cap
        if (!skipCap) {
            double uCapMin = icon.getInterpolatedU(9.0);
            double uCapMax = icon.getInterpolatedU(15.0);
            double vCapMin = icon.getInterpolatedV(1.0);
            double vCapMax = icon.getInterpolatedV(7.0);
            renderFaceDown(
                tess,
                x,
                y,
                z,
                A_MIN,
                0.0,
                A_MIN,
                A_MAX,
                A_MAX,
                uCapMin,
                vCapMin,
                uCapMax,
                vCapMax,
                brightness,
                isInventory);
        }
    }

    private void renderArmUp(Tessellator tess, double x, double y, double z, IIcon icon, int brightness,
        boolean isInventory, boolean skipCap) {
        double uArmMin = icon.getInterpolatedU(0.0);
        double uArmMax = icon.getInterpolatedU(5.0);
        double vArmMin = icon.getInterpolatedV(0.0);
        double vArmMax = icon.getInterpolatedV(6.0);

        // Arm sides
        renderFaceNorth(
            tess,
            x,
            y,
            z,
            A_MIN,
            A_MAX,
            C_MAX,
            1.0,
            A_MIN,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceSouth(
            tess,
            x,
            y,
            z,
            A_MIN,
            A_MAX,
            C_MAX,
            1.0,
            A_MAX,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceWest(
            tess,
            x,
            y,
            z,
            A_MIN,
            C_MAX,
            1.0,
            A_MIN,
            A_MAX,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);
        renderFaceEast(
            tess,
            x,
            y,
            z,
            A_MAX,
            C_MAX,
            1.0,
            A_MIN,
            A_MAX,
            uArmMin,
            vArmMin,
            uArmMax,
            vArmMax,
            brightness,
            isInventory);

        // End cap
        if (!skipCap) {
            double uCapMin = icon.getInterpolatedU(9.0);
            double uCapMax = icon.getInterpolatedU(15.0);
            double vCapMin = icon.getInterpolatedV(1.0);
            double vCapMax = icon.getInterpolatedV(7.0);
            renderFaceUp(
                tess,
                x,
                y,
                z,
                A_MIN,
                1.0,
                A_MIN,
                A_MAX,
                A_MAX,
                uCapMin,
                vCapMin,
                uCapMax,
                vCapMax,
                brightness,
                isInventory);
        }
    }

    private void renderFaceDown(Tessellator tess, double x, double y, double z, double minX, double minY, double minZ,
        double maxX, double maxZ, double minU, double minV, double maxU, double maxV, int brightness,
        boolean isInventory) {
        if (isInventory) {
            tess.setNormal(0.0F, -1.0F, 0.0F);
        } else {
            tess.setBrightness(brightness);
            tess.setColorOpaque_F(0.5F, 0.5F, 0.5F);
        }
        tess.addVertexWithUV(x + minX, y + minY, z + maxZ, minU, maxV);
        tess.addVertexWithUV(x + minX, y + minY, z + minZ, minU, minV);
        tess.addVertexWithUV(x + maxX, y + minY, z + minZ, maxU, minV);
        tess.addVertexWithUV(x + maxX, y + minY, z + maxZ, maxU, maxV);
    }

    private void renderFaceUp(Tessellator tess, double x, double y, double z, double minX, double maxY, double minZ,
        double maxX, double maxZ, double minU, double minV, double maxU, double maxV, int brightness,
        boolean isInventory) {
        if (isInventory) {
            tess.setNormal(0.0F, 1.0F, 0.0F);
        } else {
            tess.setBrightness(brightness);
            tess.setColorOpaque_F(1.0F, 1.0F, 1.0F);
        }
        tess.addVertexWithUV(x + maxX, y + maxY, z + maxZ, maxU, maxV);
        tess.addVertexWithUV(x + maxX, y + maxY, z + minZ, maxU, minV);
        tess.addVertexWithUV(x + minX, y + maxY, z + minZ, minU, minV);
        tess.addVertexWithUV(x + minX, y + maxY, z + maxZ, minU, maxV);
    }

    private void renderFaceNorth(Tessellator tess, double x, double y, double z, double minX, double maxX, double minY,
        double maxY, double minZ, double minU, double minV, double maxU, double maxV, int brightness,
        boolean isInventory) {
        if (isInventory) {
            tess.setNormal(0.0F, 0.0F, -1.0F);
        } else {
            tess.setBrightness(brightness);
            tess.setColorOpaque_F(0.8F, 0.8F, 0.8F);
        }
        tess.addVertexWithUV(x + minX, y + maxY, z + minZ, maxU, minV);
        tess.addVertexWithUV(x + maxX, y + maxY, z + minZ, minU, minV);
        tess.addVertexWithUV(x + maxX, y + minY, z + minZ, minU, maxV);
        tess.addVertexWithUV(x + minX, y + minY, z + minZ, maxU, maxV);
    }

    private void renderFaceSouth(Tessellator tess, double x, double y, double z, double minX, double maxX, double minY,
        double maxY, double maxZ, double minU, double minV, double maxU, double maxV, int brightness,
        boolean isInventory) {
        if (isInventory) {
            tess.setNormal(0.0F, 0.0F, 1.0F);
        } else {
            tess.setBrightness(brightness);
            tess.setColorOpaque_F(0.8F, 0.8F, 0.8F);
        }
        tess.addVertexWithUV(x + minX, y + maxY, z + maxZ, minU, minV);
        tess.addVertexWithUV(x + minX, y + minY, z + maxZ, minU, maxV);
        tess.addVertexWithUV(x + maxX, y + minY, z + maxZ, maxU, maxV);
        tess.addVertexWithUV(x + maxX, y + maxY, z + maxZ, maxU, minV);
    }

    private void renderFaceWest(Tessellator tess, double x, double y, double z, double minX, double minY, double maxY,
        double minZ, double maxZ, double minU, double minV, double maxU, double maxV, int brightness,
        boolean isInventory) {
        if (isInventory) {
            tess.setNormal(-1.0F, 0.0F, 0.0F);
        } else {
            tess.setBrightness(brightness);
            tess.setColorOpaque_F(0.6F, 0.6F, 0.6F);
        }
        tess.addVertexWithUV(x + minX, y + maxY, z + maxZ, maxU, minV);
        tess.addVertexWithUV(x + minX, y + maxY, z + minZ, minU, minV);
        tess.addVertexWithUV(x + minX, y + minY, z + minZ, minU, maxV);
        tess.addVertexWithUV(x + minX, y + minY, z + maxZ, maxU, maxV);
    }

    private void renderFaceEast(Tessellator tess, double x, double y, double z, double maxX, double minY, double maxY,
        double minZ, double maxZ, double minU, double minV, double maxU, double maxV, int brightness,
        boolean isInventory) {
        if (isInventory) {
            tess.setNormal(1.0F, 0.0F, 0.0F);
        } else {
            tess.setBrightness(brightness);
            tess.setColorOpaque_F(0.6F, 0.6F, 0.6F);
        }
        tess.addVertexWithUV(x + maxX, y + minY, z + maxZ, minU, maxV);
        tess.addVertexWithUV(x + maxX, y + minY, z + minZ, maxU, maxV);
        tess.addVertexWithUV(x + maxX, y + maxY, z + minZ, maxU, minV);
        tess.addVertexWithUV(x + maxX, y + maxY, z + maxZ, minU, minV);
    }
}
