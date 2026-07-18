package cn.dancingsnow.ae_wireless_nexus.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import appeng.block.networking.BlockController;
import appeng.client.render.blocks.RenderBlockController;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.networking.TileController;
import cn.dancingsnow.ae_wireless_nexus.block.BlockWirelessController;

public class RenderBlockWirelessController extends RenderBlockController {

    @Override
    public boolean renderInWorld(BlockController block, IBlockAccess world, int x, int y, int z,
        RenderBlocks renderer) {
        BlockWirelessController wireless = (BlockWirelessController) block;
        boolean xx = isController(world, x - 1, y, z) && isController(world, x + 1, y, z);
        boolean yy = isController(world, x, y - 1, z) && isController(world, x, y + 1, z);
        boolean zz = isController(world, x, y, z - 1) && isController(world, x, y, z + 1);

        int meta = world.getBlockMetadata(x, y, z);
        boolean hasPower = meta > 0;
        boolean isConflict = meta == 2;
        IIcon lights = null;

        if (xx && !yy && !zz) {
            wireless.getRendererInstance()
                .setTemporaryRenderIcon(
                    hasPower ? wireless.getControllerColumnPowered() : wireless.getControllerColumn());
            if (hasPower) {
                lights = (isConflict ? ExtraBlockTextures.BlockControllerColumnConflict
                    : ExtraBlockTextures.BlockControllerColumnLights).getIcon();
            }
            renderer.uvRotateEast = 1;
            renderer.uvRotateWest = 1;
            renderer.uvRotateTop = 1;
            renderer.uvRotateBottom = 1;
        } else if (!xx && yy && !zz) {
            wireless.getRendererInstance()
                .setTemporaryRenderIcon(
                    hasPower ? wireless.getControllerColumnPowered() : wireless.getControllerColumn());
            if (hasPower) {
                lights = (isConflict ? ExtraBlockTextures.BlockControllerColumnConflict
                    : ExtraBlockTextures.BlockControllerColumnLights).getIcon();
            }
            renderer.uvRotateEast = 0;
            renderer.uvRotateNorth = 0;
        } else if (!xx && !yy && zz) {
            wireless.getRendererInstance()
                .setTemporaryRenderIcon(
                    hasPower ? wireless.getControllerColumnPowered() : wireless.getControllerColumn());
            if (hasPower) {
                lights = (isConflict ? ExtraBlockTextures.BlockControllerColumnConflict
                    : ExtraBlockTextures.BlockControllerColumnLights).getIcon();
            }
            renderer.uvRotateNorth = 1;
            renderer.uvRotateSouth = 1;
            renderer.uvRotateTop = 0;
        } else if ((xx ? 1 : 0) + (yy ? 1 : 0) + (zz ? 1 : 0) >= 2) {
            resetUv(renderer);
            wireless.getRendererInstance()
                .setTemporaryRenderIcon(
                    wireless.getControllerInside((Math.abs(x) + Math.abs(y) + Math.abs(z)) % 2 != 0));
        } else if (hasPower) {
            wireless.getRendererInstance()
                .setTemporaryRenderIcon(wireless.getControllerPowered());
            lights = (isConflict ? ExtraBlockTextures.BlockControllerConflict
                : ExtraBlockTextures.BlockControllerLights).getIcon();
        } else {
            wireless.getRendererInstance()
                .setTemporaryRenderIcon(null);
        }

        boolean rendered = renderer.renderStandardBlock(wireless, x, y, z);
        if (lights != null) renderLights(wireless, renderer, x, y, z, lights);

        wireless.getRendererInstance()
            .setTemporaryRenderIcon(null);
        resetUv(renderer);
        return rendered;
    }

    private static boolean isController(IBlockAccess world, int x, int y, int z) {
        if (y < 0) return false;
        Block block = world.getBlock(x, y, z);
        if (block instanceof BlockController) return true;
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileController;
    }

    private static void renderLights(Block block, RenderBlocks renderer, int x, int y, int z, IIcon lights) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
        tessellator.setBrightness(14 << 20 | 14 << 4);
        renderer.renderFaceXNeg(block, x, y, z, lights);
        renderer.renderFaceXPos(block, x, y, z, lights);
        renderer.renderFaceYNeg(block, x, y, z, lights);
        renderer.renderFaceYPos(block, x, y, z, lights);
        renderer.renderFaceZNeg(block, x, y, z, lights);
        renderer.renderFaceZPos(block, x, y, z, lights);
    }

    private static void resetUv(RenderBlocks renderer) {
        renderer.uvRotateEast = 0;
        renderer.uvRotateBottom = 0;
        renderer.uvRotateNorth = 0;
        renderer.uvRotateSouth = 0;
        renderer.uvRotateTop = 0;
        renderer.uvRotateWest = 0;
    }
}
