package cn.dancingsnow.ae_wireless_nexus.integration.gregtech;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;

import cn.dancingsnow.ae_wireless_nexus.gui.WirelessSelectionPanel;
import cn.dancingsnow.ae_wireless_nexus.registry.ModBlocks;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public final class GTWirelessUI {

    private GTWirelessUI() {}

    public static Flow addSelectorButton(IGregTechTileEntity base, ModularPanel panel, PanelSyncManager syncManager,
        Flow flow) {
        if (flow == null || !GTWirelessEndpoint.isEligible(base) || !(base instanceof IGTWirelessHost)) return flow;

        GTWirelessEndpoint endpoint = ((IGTWirelessHost) base).aeWirelessNexus$getWirelessEndpoint();
        IPanelHandler selector = syncManager.syncedPanel(
            "ae_wireless_nexus_selector",
            true,
            (childSyncManager, handler) -> WirelessSelectionPanel
                .build("gt_wireless_selector", endpoint, syncManager.getPlayer(), childSyncManager));
        InteractionSyncHandler open = new InteractionSyncHandler().setOnMousePressed(mouse -> selector.openPanel());
        ButtonWidget<?> button = new ButtonWidget<>().size(18)
            .syncHandler(open)
            .tooltip(t -> t.addLine(IKey.lang("gui.ae_wireless_nexus.open_selector")))
            .overlay(
                new ItemDrawable(new ItemStack(ModBlocks.WIRELESS_CONNECTOR)).asIcon()
                    .margin(1));
        return flow.child(button);
    }
}
