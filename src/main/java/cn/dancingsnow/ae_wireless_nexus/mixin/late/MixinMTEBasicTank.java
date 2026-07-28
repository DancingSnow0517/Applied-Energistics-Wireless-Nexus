package cn.dancingsnow.ae_wireless_nexus.mixin.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import cn.dancingsnow.ae_wireless_nexus.AEWirelessNexus;
import cn.dancingsnow.ae_wireless_nexus.gui.WirelessSelectionPanel;
import cn.dancingsnow.ae_wireless_nexus.integration.gregtech.GTWirelessEndpoint;
import cn.dancingsnow.ae_wireless_nexus.integration.gregtech.IGTWirelessHost;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicTank;

@Mixin(MTEBasicTank.class)
public abstract class MixinMTEBasicTank {

    @Inject(method = "buildUI", at = @At("HEAD"), cancellable = true, remap = false)
    private void aeWirelessNexus$replaceInvalidZeroSlotGui(PosGuiData data, PanelSyncManager syncManager,
        UISettings settings, CallbackInfoReturnable<ModularPanel> cir) {
        MTEBasicTank tank = (MTEBasicTank) (Object) this;
        IGregTechTileEntity base = tank.getBaseMetaTileEntity();
        if (tank.getSizeInventory() != 0 || !GTWirelessEndpoint.isEligible(base)
            || !(base instanceof IGTWirelessHost host)) return;

        settings.useTheme(AEWirelessNexus.GUI_THEME);
        cir.setReturnValue(
            WirelessSelectionPanel.build(
                "gt_wireless_selector",
                host.aeWirelessNexus$getWirelessEndpoint(),
                data.getPlayer(),
                syncManager,
                false));
    }
}
