package cn.dancingsnow.ae_wireless_nexus.registry;

import static gregtech.api.util.GTRecipeBuilder.INGOTS;
import static gregtech.api.util.GTRecipeBuilder.MINUTES;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;
import static gregtech.api.util.GTRecipeConstants.AssemblyLine;
import static gregtech.api.util.GTRecipeConstants.RESEARCH_ITEM;
import static gregtech.api.util.GTRecipeConstants.SCANNING;

import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import gregtech.api.enums.TierEU;
import gregtech.api.util.GTRecipeBuilder;
import gregtech.api.util.recipe.Scanning;
import gtPlusPlus.core.material.MaterialsAlloy;

/**
 * Recipe registration is intentionally left empty until the item costs are confirmed.
 * The postInit entry point is kept so enabling recipes later does not change lifecycle wiring.
 */
public final class ModRecipes {

    private ModRecipes() {}

    public static void postInit() {
        ItemStack controller = AEApi.instance()
            .definitions()
            .blocks()
            .controller()
            .maybeStack(1)
            .orNull();
        ItemStack wirelessHub = AEApi.instance()
            .definitions()
            .blocks()
            .wirelessHub()
            .maybeStack(1)
            .orNull();
        ItemStack wirelessTool = AEApi.instance()
            .definitions()
            .items()
            .toolWirelessKit()
            .maybeStack(1)
            .orNull();
        ItemStack wireless = AEApi.instance()
            .definitions()
            .materials()
            .wireless()
            .maybeStack(4)
            .orNull();

        if (controller == null || wirelessHub == null || wirelessTool == null || wireless == null) {
            return;
        }

        GTRecipeBuilder.builder()
            .metadata(RESEARCH_ITEM, controller)
            .metadata(SCANNING, new Scanning(5 * MINUTES, TierEU.RECIPE_LuV))
            .itemInputs(controller, wirelessHub, wirelessTool, wireless)
            .fluidInputs(MaterialsAlloy.INDALLOY_140.getFluidStack(8 * INGOTS))
            .itemOutputs(new ItemStack(ModBlocks.WIRELESS_CONTROLLER))
            .duration(30 * SECONDS)
            .eut(TierEU.RECIPE_LuV)
            .addTo(AssemblyLine);
    }
}
