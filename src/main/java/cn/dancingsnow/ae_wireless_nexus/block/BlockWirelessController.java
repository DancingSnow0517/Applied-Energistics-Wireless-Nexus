package cn.dancingsnow.ae_wireless_nexus.block;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import appeng.block.networking.BlockController;
import cn.dancingsnow.ae_wireless_nexus.AEWirelessNexus;
import cn.dancingsnow.ae_wireless_nexus.client.render.RenderBlockWirelessController;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessNetworkService;
import cn.dancingsnow.ae_wireless_nexus.tile.TileWirelessController;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockWirelessController extends BlockController {

    private static final String TEXTURE_PREFIX = AEWirelessNexus.MODID + ":BlockWirelessController";

    @SideOnly(Side.CLIENT)
    private IIcon controllerPowered;

    @SideOnly(Side.CLIENT)
    private IIcon controllerColumn;

    @SideOnly(Side.CLIENT)
    private IIcon controllerColumnPowered;

    @SideOnly(Side.CLIENT)
    private IIcon controllerInsideA;

    @SideOnly(Side.CLIENT)
    private IIcon controllerInsideB;

    public BlockWirelessController() {
        super();
        setTileEntity(TileWirelessController.class);
        setBlockName("wireless_controller");
        setBlockTextureName(TEXTURE_PREFIX);
        setCreativeTab(AEWirelessNexus.CREATIVE_TAB);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected RenderBlockWirelessController getRenderer() {
        return new RenderBlockWirelessController();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        super.registerBlockIcons(register);
        controllerPowered = register.registerIcon(TEXTURE_PREFIX + "Powered");
        controllerColumn = register.registerIcon(TEXTURE_PREFIX + "Column");
        controllerColumnPowered = register.registerIcon(TEXTURE_PREFIX + "ColumnPowered");
        controllerInsideA = register.registerIcon(TEXTURE_PREFIX + "InsideA");
        controllerInsideB = register.registerIcon(TEXTURE_PREFIX + "InsideB");
    }

    @SideOnly(Side.CLIENT)
    public IIcon getControllerPowered() {
        return controllerPowered;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getControllerColumn() {
        return controllerColumn;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getControllerColumnPowered() {
        return controllerColumnPowered;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getControllerInside(boolean alternate) {
        return alternate ? controllerInsideB : controllerInsideA;
    }

    @Override
    public boolean onActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY,
        float hitZ) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileWirelessController)) return false;
        if (!world.isRemote) {
            ((TileWirelessController) tile).openConfiguration(player);
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, placer, stack);
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileWirelessController) {
            WirelessNetworkService.onControllerChanged((TileWirelessController) tile);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int metadata) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileWirelessController) {
            WirelessNetworkService.onControllerRemoved((TileWirelessController) tile);
        }
        super.breakBlock(world, x, y, z, block, metadata);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, net.minecraft.block.Block block) {
        super.onNeighborBlockChange(world, x, y, z, block);
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileWirelessController) {
            WirelessNetworkService.onControllerChanged((TileWirelessController) tile);
        }
    }
}
