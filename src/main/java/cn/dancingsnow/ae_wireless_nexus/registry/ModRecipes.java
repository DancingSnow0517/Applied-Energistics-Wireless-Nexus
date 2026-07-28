package cn.dancingsnow.ae_wireless_nexus.registry;

import static gregtech.api.util.GTRecipeBuilder.INGOTS;
import static gregtech.api.util.GTRecipeBuilder.MINUTES;
import static gregtech.api.util.GTRecipeBuilder.PANIC_MODE_NULL;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;
import static gregtech.api.util.GTRecipeConstants.AssemblyLine;
import static gregtech.api.util.GTRecipeConstants.RESEARCH_ITEM;
import static gregtech.api.util.GTRecipeConstants.SCANNING;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.jetbrains.annotations.NotNull;

import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.enums.TierEU;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTRecipeBuilder;
import gregtech.api.util.recipe.Scanning;
import gtPlusPlus.core.material.MaterialsAlloy;

/**
 * Recipe registration is intentionally left empty until the item costs are confirmed.
 * The postInit entry point is kept so enabling recipes later does not change lifecycle wiring.
 */
public final class ModRecipes {

    public static final String AE2_ID = "appliedenergistics2";

    private ModRecipes() {}

    public static void postInit() {
        GTRecipeBuilder.builder()
            .metadata(RESEARCH_ITEM, getModItem(AE2_ID, "tile.BlockController"))
            .metadata(SCANNING, new Scanning(5 * MINUTES, TierEU.RECIPE_LuV))
            .itemInputs(
                getModItem(AE2_ID, "tile.BlockController"),
                getModItem(AE2_ID, "tile.BlockWirelessHub", 1, 0),
                getModItem(AE2_ID, "item.ToolWirelessKit"),
                getModItem(AE2_ID, "item.ItemMultiMaterial", 4, 41))
            .fluidInputs(MaterialsAlloy.INDALLOY_140.getFluidStack(8 * INGOTS))
            .itemOutputs(new ItemStack(ModBlocks.WIRELESS_CONTROLLER))
            .duration(30 * SECONDS)
            .eut(TierEU.RECIPE_LuV)
            .addTo(AssemblyLine);

        GTModHandler.addCraftingRecipe(
            new ItemStack(ModBlocks.WIRELESS_CONNECTOR),
            GTModHandler.RecipeBits.BITS_STD,
            new Object[] { "ABA", "BCB", "ABA", 'A', getModItem(AE2_ID, "item.ItemMultiMaterial", 1, 41), 'B',
                getModItem(AE2_ID, "tile.BlockWirelessConnector", 1, 0), 'C',
                getModItem(AE2_ID, "tile.BlockController", 1, 0) });
    }

    private static @NotNull ItemStack getModItem(String modId, String itemId) {
        return getModItem(modId, itemId, 1, 0);
    }

    private static @NotNull ItemStack getModItem(String modId, String itemId, int amount) {
        return getModItem(modId, itemId, amount, 0);
    }

    private static @NotNull ItemStack getModItem(String modId, String itemId, int amount, int meta) {
        Item item = GameRegistry.findItem(modId, itemId);
        ItemStack result;
        if (item == null) {
            if (!PANIC_MODE_NULL) {
                result = new ItemStack(Blocks.fire);
                result.setStackDisplayName(EnumChatFormatting.RED + "Missing Item: " + modId + ":" + itemId);
            } else {
                throw new IllegalStateException("Missing Item: " + modId + ":" + itemId);
            }
        } else {
            result = new ItemStack(item, amount, meta);
        }
        return result;
    }
}
