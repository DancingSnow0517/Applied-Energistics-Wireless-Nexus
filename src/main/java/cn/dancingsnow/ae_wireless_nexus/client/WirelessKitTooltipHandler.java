package cn.dancingsnow.ae_wireless_nexus.client;

import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import cn.dancingsnow.ae_wireless_nexus.network.WirelessNetworkToolBinding;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public final class WirelessKitTooltipHandler {

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!WirelessNetworkToolBinding.hasBinding(event.itemStack)) return;
        event.toolTip.add(
            StatCollector.translateToLocalFormatted(
                "tooltip.ae_wireless_nexus.wireless_kit.bound_network",
                WirelessNetworkToolBinding.getNetworkDisplayName(event.itemStack)));
    }
}
