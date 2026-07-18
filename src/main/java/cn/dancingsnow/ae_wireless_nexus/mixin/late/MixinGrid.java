package cn.dancingsnow.ae_wireless_nexus.mixin.late;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.me.Grid;
import appeng.me.MachineSet;
import appeng.tile.networking.TileController;
import cn.dancingsnow.ae_wireless_nexus.tile.TileWirelessController;

@Mixin(Grid.class)
public abstract class MixinGrid {

    @Shadow(remap = false)
    @Final
    private Map<Class<? extends IGridHost>, MachineSet> machines;

    @Inject(method = "getMachines", at = @At("RETURN"), cancellable = true, remap = false)
    private void aeWirelessNexus$includeWirelessControllers(Class<? extends IGridHost> machineClass,
        CallbackInfoReturnable<IMachineSet> cir) {
        if (machineClass != TileController.class) return;

        MachineSet wirelessControllers = machines.get(TileWirelessController.class);
        if (wirelessControllers == null || wirelessControllers.isEmpty()) return;

        MachineSet controllers = new MachineSet(TileController.class);
        for (IGridNode node : cir.getReturnValue()) controllers.add(node);
        controllers.addAll(wirelessControllers);
        cir.setReturnValue(controllers);
    }
}
