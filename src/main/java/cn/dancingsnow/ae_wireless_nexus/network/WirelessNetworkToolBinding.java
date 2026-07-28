package cn.dancingsnow.ae_wireless_nexus.network;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;

import appeng.api.AEApi;
import appeng.helpers.WireLessToolHelper;
import cn.dancingsnow.ae_wireless_nexus.AEWirelessNexus;
import cn.dancingsnow.ae_wireless_nexus.tile.TileWirelessController;

public final class WirelessNetworkToolBinding {

    private static final String BINDING_TAG = AEWirelessNexus.MODID;
    private static final String NETWORK_ID_TAG = "NetworkId";
    private static final String NETWORK_NAME_TAG = "NetworkName";

    private WirelessNetworkToolBinding() {}

    public static boolean isWirelessKit(ItemStack stack) {
        return stack != null && AEApi.instance()
            .definitions()
            .items()
            .toolWirelessKit()
            .isSameAs(stack);
    }

    public static void selectNetwork(ItemStack tool, TileWirelessController controller, EntityPlayer player) {
        if (!isWirelessKit(tool) || controller == null || player == null) return;

        String networkName = controller.getNetworkName();
        if (!WirelessNetworkService.hasPermission(controller, player)) {
            notifyPlayer(player, "message.ae_wireless_nexus.wireless_kit.no_permission", networkName);
            return;
        }

        if (!tool.hasTagCompound()) WireLessToolHelper.newNBT(tool);
        NBTTagCompound binding = new NBTTagCompound();
        binding.setString(
            NETWORK_ID_TAG,
            controller.getNetworkId()
                .toString());
        binding.setString(NETWORK_NAME_TAG, networkName);
        tool.getTagCompound()
            .setTag(BINDING_TAG, binding);
        player.inventory.markDirty();
        notifyPlayer(player, "message.ae_wireless_nexus.wireless_kit.selected", networkName);
    }

    public static void bindConnector(ItemStack tool, WirelessBindableEndpoint connector, EntityPlayer player) {
        UUID networkId = getNetworkId(tool);
        if (networkId == null || connector == null || player == null) return;

        boolean permitted = WirelessNetworkService.hasPermission(networkId, connector.getEndpointWorld(), player);
        connector.bindToNetwork(networkId, player);
        String networkName = getNetworkDisplayName(tool);
        if (permitted) {
            notifyPlayer(
                player,
                "message.ae_wireless_nexus.wireless_kit.connected",
                connector.getEndpointDisplayName(),
                networkName);
        } else {
            notifyPlayer(player, "message.ae_wireless_nexus.wireless_kit.no_permission", networkName);
        }
    }

    public static boolean hasBinding(ItemStack tool) {
        return getNetworkId(tool) != null;
    }

    public static String getNetworkDisplayName(ItemStack tool) {
        UUID networkId = getNetworkId(tool);
        if (networkId == null) return "";
        NBTTagCompound binding = getBinding(tool);
        String networkName = binding.getString(NETWORK_NAME_TAG);
        return networkName.isEmpty() ? networkId.toString() : networkName;
    }

    private static UUID getNetworkId(ItemStack tool) {
        if (!isWirelessKit(tool)) return null;
        String value = getBinding(tool).getString(NETWORK_ID_TAG);
        if (value.isEmpty()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static NBTTagCompound getBinding(ItemStack tool) {
        if (tool == null || !tool.hasTagCompound()) return new NBTTagCompound();
        return tool.getTagCompound()
            .getCompoundTag(BINDING_TAG);
    }

    private static void notifyPlayer(EntityPlayer player, String translationKey, Object... arguments) {
        player.addChatMessage(new ChatComponentTranslation(translationKey, arguments));
    }
}
