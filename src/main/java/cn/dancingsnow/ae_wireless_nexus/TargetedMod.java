package cn.dancingsnow.ae_wireless_nexus;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.gtnhmixins.builders.ITargetMod;
import com.gtnewhorizon.gtnhmixins.builders.TargetModBuilder;

public enum TargetedMod implements ITargetMod {

    APPLIED_ENERGISTICS_2("appliedenergistics2"),
    GREGTECH("gregtech");

    private final TargetModBuilder builder;

    TargetedMod(String modId) {
        this.builder = new TargetModBuilder().setModId(modId);
    }

    @Override
    public @NotNull TargetModBuilder getBuilder() {
        return builder;
    }
}
