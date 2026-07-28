package cn.dancingsnow.ae_wireless_nexus;

import net.minecraftforge.common.MinecraftForge;

import cn.dancingsnow.ae_wireless_nexus.client.WirelessKitTooltipHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(new WirelessKitTooltipHandler());
    }

}
