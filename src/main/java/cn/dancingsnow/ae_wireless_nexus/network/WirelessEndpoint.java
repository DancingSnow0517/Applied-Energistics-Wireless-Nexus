package cn.dancingsnow.ae_wireless_nexus.network;

import java.util.UUID;

import appeng.api.networking.IGridNode;

/** Runtime contract for a device requesting a wireless channel lease. */
public interface WirelessEndpoint {

    UUID getTargetNetworkId();

    int getWirelessPriority();

    int getRequestedChannels();

    int getBindingPlayerId();

    String getStableEndpointKey();

    IGridNode getWirelessGridNode();

    boolean isWirelessEndpointValid();

    void setWirelessLease(WirelessLeaseStatus status, TileWirelessControllerRef controller);
}
