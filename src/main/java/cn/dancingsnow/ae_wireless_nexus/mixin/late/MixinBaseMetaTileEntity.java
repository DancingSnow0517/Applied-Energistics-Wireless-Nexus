package cn.dancingsnow.ae_wireless_nexus.mixin.late;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cn.dancingsnow.ae_wireless_nexus.integration.gregtech.GTWirelessEndpoint;
import cn.dancingsnow.ae_wireless_nexus.integration.gregtech.IGTWirelessHost;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.modularui2.MetaTileEntityGuiHandler;

@Mixin(BaseMetaTileEntity.class)
public abstract class MixinBaseMetaTileEntity implements IGTWirelessHost {

    @Unique
    private GTWirelessEndpoint aeWirelessNexus$endpoint;

    @Override
    public GTWirelessEndpoint aeWirelessNexus$getWirelessEndpoint() {
        if (aeWirelessNexus$endpoint == null) {
            aeWirelessNexus$endpoint = new GTWirelessEndpoint((IGregTechTileEntity) (Object) this);
        }
        return aeWirelessNexus$endpoint;
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"))
    private void aeWirelessNexus$writeWirelessData(NBTTagCompound tag, CallbackInfo ci) {
        if (GTWirelessEndpoint.isEligible((IGregTechTileEntity) (Object) this) || aeWirelessNexus$endpoint != null) {
            aeWirelessNexus$getWirelessEndpoint().writeToNBT(tag);
        }
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    private void aeWirelessNexus$readWirelessData(NBTTagCompound tag, CallbackInfo ci) {
        if (tag.hasKey("AEWirelessNexusTarget") || tag.hasKey("AEWirelessNexusPlayer")) {
            aeWirelessNexus$getWirelessEndpoint().readFromNBT(tag);
        }
    }

    @Inject(method = "updateEntityProfiled", at = @At("RETURN"), remap = false)
    private void aeWirelessNexus$tickWirelessEndpoint(CallbackInfo ci) {
        IGregTechTileEntity base = (IGregTechTileEntity) (Object) this;
        if (GTWirelessEndpoint.isEligible(base) && base.isServerSide()) {
            aeWirelessNexus$getWirelessEndpoint().tick(base.getTimer());
        }
    }

    @Inject(method = "invalidate", at = @At("HEAD"))
    private void aeWirelessNexus$invalidateWirelessEndpoint(CallbackInfo ci) {
        if (aeWirelessNexus$endpoint != null) aeWirelessNexus$endpoint.unload();
    }

    @Inject(method = "onUnload", at = @At("HEAD"), remap = false)
    private void aeWirelessNexus$unloadWirelessEndpoint(CallbackInfo ci) {
        if (aeWirelessNexus$endpoint != null) aeWirelessNexus$endpoint.unload();
    }

    @Inject(method = "onRightclick", at = @At("RETURN"), cancellable = true, remap = false)
    private void aeWirelessNexus$openSelectorForGuiLessHatch(EntityPlayer player, ForgeDirection side, float hitX,
        float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {
        IGregTechTileEntity base = (IGregTechTileEntity) (Object) this;
        if (cir.getReturnValue() || player.isSneaking() || !GTWirelessEndpoint.isEligible(base)) return;
        if (base.isServerSide()) MetaTileEntityGuiHandler.open(player, base.getMetaTileEntity());
        cir.setReturnValue(true);
    }
}
