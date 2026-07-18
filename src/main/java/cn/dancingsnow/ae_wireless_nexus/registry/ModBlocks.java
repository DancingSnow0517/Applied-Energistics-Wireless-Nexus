package cn.dancingsnow.ae_wireless_nexus.registry;

import net.minecraft.block.Block;

import appeng.block.AEBaseItemBlock;
import cn.dancingsnow.ae_wireless_nexus.AEWirelessNexus;
import cn.dancingsnow.ae_wireless_nexus.block.BlockWirelessConnector;
import cn.dancingsnow.ae_wireless_nexus.block.BlockWirelessController;
import cn.dancingsnow.ae_wireless_nexus.tile.TileWirelessConnector;
import cn.dancingsnow.ae_wireless_nexus.tile.TileWirelessController;
import cpw.mods.fml.common.registry.GameRegistry;

public final class ModBlocks {

    public static Block WIRELESS_CONTROLLER;
    public static Block WIRELESS_CONNECTOR;

    private ModBlocks() {}

    public static void preInit() {
        WIRELESS_CONTROLLER = new BlockWirelessController();
        WIRELESS_CONNECTOR = new BlockWirelessConnector();

        GameRegistry.registerBlock(WIRELESS_CONTROLLER, AEBaseItemBlock.class, "wireless_controller");
        GameRegistry.registerBlock(WIRELESS_CONNECTOR, "wireless_connector");
        GameRegistry.registerTileEntity(TileWirelessController.class, AEWirelessNexus.MODID + ".wireless_controller");
        GameRegistry.registerTileEntity(TileWirelessConnector.class, AEWirelessNexus.MODID + ".wireless_connector");
    }
}
