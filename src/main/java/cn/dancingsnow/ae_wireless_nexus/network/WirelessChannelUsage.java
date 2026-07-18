package cn.dancingsnow.ae_wireless_nexus.network;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;

/** Reads the channel demand using the same connection metric as AE's wireless connector. */
public final class WirelessChannelUsage {

    private WirelessChannelUsage() {}

    public static int get(IGridConnection connection, IGridNode node) {
        if (node == null) return 0;
        int minimum = node.hasFlag(GridFlags.REQUIRE_CHANNEL) ? 1 : 0;
        int maximum = getMaximum(
            minimum > 0,
            node.hasFlag(GridFlags.CANNOT_CARRY),
            node.hasFlag(GridFlags.DENSE_CAPACITY));
        int used = connection == null ? minimum : Math.max(minimum, connection.getUsedChannels());
        return Math.max(0, Math.min(maximum, used));
    }

    static int getMaximum(boolean requiresChannel, boolean cannotCarry, boolean denseCapacity) {
        if (cannotCarry) return requiresChannel ? 1 : 0;
        return denseCapacity ? 32 : 8;
    }
}
