package cn.dancingsnow.ae_wireless_nexus.network;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface WirelessBindableEndpoint extends WirelessEndpoint {

    World getEndpointWorld();

    WirelessLeaseStatus getWirelessLeaseStatus();

    void bindToNetwork(UUID networkId, EntityPlayer player);

    void unbindFromNetwork();

    void setWirelessPriority(int priority);
}
