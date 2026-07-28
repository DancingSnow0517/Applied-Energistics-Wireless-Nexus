package cn.dancingsnow.ae_wireless_nexus.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cn.dancingsnow.ae_wireless_nexus.AEWirelessNexus;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessNetworkToolBinding;
import cn.dancingsnow.ae_wireless_nexus.tile.TileWirelessConnector;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import xonin.backhand.api.core.BackhandUtils;

public class BlockWirelessConnector extends BlockContainer {

    public BlockWirelessConnector() {
        super(Material.iron);
        setBlockName("wireless_connector");
        setHardness(2.0F);
        setResistance(8.0F);
        setCreativeTab(AEWirelessNexus.CREATIVE_TAB);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileWirelessConnector();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileWirelessConnector)) return false;
        ItemStack heldItem = player.getHeldItem();
        if (WirelessNetworkToolBinding.hasBinding(heldItem)) {
            if (!world.isRemote) {
                WirelessNetworkToolBinding.bindConnector(heldItem, (TileWirelessConnector) tile, player);
            }
            return true;
        }
        if (!world.isRemote) {
            ((TileWirelessConnector) tile).openSelection(player);
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, placer, stack);
        if (world.isRemote || !(placer instanceof EntityPlayer player)) return;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileWirelessConnector connector) {
            WirelessNetworkToolBinding.bindConnector(BackhandUtils.getOffhandItem(player), connector, player);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int metadata) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileWirelessConnector) {
            ((TileWirelessConnector) tile).unbindFromNetwork();
        }
        super.breakBlock(world, x, y, z, block, metadata);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        this.blockIcon = register.registerIcon("ae_wireless_nexus:BlockWirelessConnecter");
    }
}
