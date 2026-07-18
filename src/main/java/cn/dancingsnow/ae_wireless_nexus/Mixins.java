package cn.dancingsnow.ae_wireless_nexus;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixins implements IMixins {

    AE2_CONTROLLER_CONFLICT(
        new MixinBuilder("AE2 wireless controller integration").addRequiredMod(TargetedMod.APPLIED_ENERGISTICS_2)
            .setPhase(Phase.LATE)
            .addCommonMixins("MixinGrid", "MixinPathGridCache")),
    GREGTECH_INTEGRATION(new MixinBuilder("GregTech wireless network integration").addRequiredMod(TargetedMod.GREGTECH)
        .setPhase(Phase.LATE)
        .addCommonMixins(
            "MixinBaseMetaTileEntity",
            "MixinCommonMetaTileEntity",
            "MixinMTEHatchInputBusMEGui",
            "MixinMTETieredMachineBlockBaseGui"));

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @Override
    public @NotNull MixinBuilder getBuilder() {
        return builder;
    }
}
