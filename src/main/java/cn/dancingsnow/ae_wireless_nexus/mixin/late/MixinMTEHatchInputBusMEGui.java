package cn.dancingsnow.ae_wireless_nexus.mixin.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.layout.Flow;

import cn.dancingsnow.ae_wireless_nexus.integration.gregtech.GTWirelessUI;
import cn.dancingsnow.ae_wireless_nexus.integration.gregtech.IGTGuiAccessor;
import gregtech.common.gui.modularui.hatch.MTEHatchInputBusMEGui;

@Mixin(MTEHatchInputBusMEGui.class)
public abstract class MixinMTEHatchInputBusMEGui {

    @Inject(method = "createBottomRightCornerFlow", at = @At("RETURN"), cancellable = true, remap = false)
    private void aeWirelessNexus$addWirelessButton(ModularPanel panel, PanelSyncManager syncManager,
        CallbackInfoReturnable<Flow> cir) {
        cir.setReturnValue(
            GTWirelessUI.addSelectorButton(
                ((IGTGuiAccessor) this).aeWirelessNexus$getBaseMetaTileEntity(),
                panel,
                syncManager,
                cir.getReturnValue()));
    }
}
