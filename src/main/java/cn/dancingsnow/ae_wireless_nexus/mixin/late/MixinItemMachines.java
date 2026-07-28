package cn.dancingsnow.ae_wireless_nexus.mixin.late;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cn.dancingsnow.ae_wireless_nexus.integration.gregtech.GTWirelessEndpoint;
import cn.dancingsnow.ae_wireless_nexus.integration.gregtech.IGTWirelessHost;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessNetworkToolBinding;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.common.blocks.ItemMachines;
import xonin.backhand.api.core.BackhandUtils;

@Mixin(ItemMachines.class)
public abstract class MixinItemMachines {

    @Inject(method = "placeBlockAt", at = @At("RETURN"), remap = false)
    private void aeWirelessNexus$bindPlacedMEHatch(ItemStack aStack, EntityPlayer aPlayer, World aWorld, int aX, int aY,
        int aZ, int ordinalSide, float hitX, float hitY, float hitZ, int aMeta, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || aWorld.isRemote || aPlayer == null) return;

        TileEntity tile = aWorld.getTileEntity(aX, aY, aZ);
        if (!(tile instanceof IGregTechTileEntity base) || !(tile instanceof IGTWirelessHost host)
            || !GTWirelessEndpoint.isEligible(base)) return;

        WirelessNetworkToolBinding
            .bindConnector(BackhandUtils.getOffhandItem(aPlayer), host.aeWirelessNexus$getWirelessEndpoint(), aPlayer);
    }
}
