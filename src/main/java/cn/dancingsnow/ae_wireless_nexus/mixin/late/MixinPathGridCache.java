package cn.dancingsnow.ae_wireless_nexus.mixin.late;

import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.networking.IGrid;
import appeng.api.networking.events.MENetworkControllerChange;
import appeng.api.networking.pathing.ControllerState;
import appeng.me.cache.PathGridCache;
import appeng.tile.networking.TileController;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessNetworkService;

@Mixin(PathGridCache.class)
public abstract class MixinPathGridCache {

    @Shadow(remap = false)
    @Final
    private Set<TileController> controllers;

    @Shadow(remap = false)
    @Final
    private IGrid myGrid;

    @Shadow(remap = false)
    private ControllerState controllerState;

    @Inject(method = "recalcController", at = @At("RETURN"), remap = false)
    private void aeWirelessNexus$rejectMultipleWirelessControllers(CallbackInfo ci) {
        if (WirelessNetworkService.hasMultipleWirelessControllers(controllers)
            && controllerState != ControllerState.CONTROLLER_CONFLICT) {
            controllerState = ControllerState.CONTROLLER_CONFLICT;
            myGrid.postEvent(new MENetworkControllerChange());
        }
    }
}
