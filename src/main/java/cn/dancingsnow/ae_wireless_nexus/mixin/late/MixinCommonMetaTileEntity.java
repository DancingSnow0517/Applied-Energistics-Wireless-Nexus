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
import gregtech.api.metatileentity.CommonMetaTileEntity;

@Mixin(CommonMetaTileEntity.class)
public abstract class MixinCommonMetaTileEntity {

    @Inject(method = "buildUI", at = @At("RETURN"), cancellable = true, remap = false)
    private void aeWirelessNexus$buildSelectorForGuiLessHatch(PosGuiData data, PanelSyncManager syncManager,
        UISettings settings, CallbackInfoReturnable<ModularPanel> cir) {
        if (cir.getReturnValue() != null) return;
        CommonMetaTileEntity mte = (CommonMetaTileEntity) (Object) this;
        IGregTechTileEntity base = mte.getBaseMetaTileEntity();
        if (!GTWirelessEndpoint.isEligible(base) || !(base instanceof IGTWirelessHost)) return;
        settings.useTheme(AEWirelessNexus.GUI_THEME);
        cir.setReturnValue(
            WirelessSelectionPanel.build(
                "gt_wireless_selector",
                ((IGTWirelessHost) base).aeWirelessNexus$getWirelessEndpoint(),
                data.getPlayer(),
                syncManager,
                false));
    }
}
