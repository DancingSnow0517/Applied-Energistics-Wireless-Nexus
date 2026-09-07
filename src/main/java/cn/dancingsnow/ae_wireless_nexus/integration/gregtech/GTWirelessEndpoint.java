package cn.dancingsnow.ae_wireless_nexus.integration.gregtech;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnection;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.core.worlddata.WorldData;
import appeng.me.helpers.IGridProxyable;
import cn.dancingsnow.ae_wireless_nexus.AEWirelessNexus;
import cn.dancingsnow.ae_wireless_nexus.network.TileWirelessControllerRef;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessBindableEndpoint;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessChannelUsage;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessLeaseStatus;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessNetworkService;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;

public final class GTWirelessEndpoint implements WirelessBindableEndpoint {

    private final IGregTechTileEntity base;
    private UUID targetNetwork;
    private int bindingPlayerId = -1;
    private int priority;
    private int requestedChannels;
    private WirelessLeaseStatus status = WirelessLeaseStatus.UNBOUND;
    private IGridConnection remoteConnection;
    private boolean connectionFailureLogged;

    public GTWirelessEndpoint(IGregTechTileEntity base) {
        this.base = base;
    }

    public static boolean isEligible(IGregTechTileEntity base) {
        if (base == null || !base.canAccessData()) return false;
        IMetaTileEntity mte = base.getMetaTileEntity();
        return mte != null && isEligibleMetaTileEntityType(mte.getClass());
    }

    static boolean isEligibleMetaTileEntityType(Class<?> type) {
        return type != null && MTEHatch.class.isAssignableFrom(type) && IGridProxyable.class.isAssignableFrom(type);
    }

    @Override
    public UUID getTargetNetworkId() {
        return targetNetwork;
    }

    @Override
    public int getWirelessPriority() {
        return priority;
    }

    @Override
    public int getRequestedChannels() {
        IGridNode node = getWirelessGridNode();
        if (remoteConnection != null) requestedChannels = WirelessChannelUsage.get(remoteConnection, node);
        else requestedChannels = Math.max(requestedChannels, WirelessChannelUsage.get(null, node));
        return requestedChannels;
    }

    @Override
    public int getBindingPlayerId() {
        return bindingPlayerId;
    }

    @Override
    public String getStableEndpointKey() {
        World world = getEndpointWorld();
        return world == null ? "gt:unloaded:" + System.identityHashCode(base)
            : "gt:" + world.provider.dimensionId
                + ":"
                + base.getXCoord()
                + ":"
                + base.getYCoord()
                + ":"
                + base.getZCoord();
    }

    @Override
    public IGridNode getWirelessGridNode() {
        if (!isEligible(base)) return null;
        return ((IGridProxyable) base.getMetaTileEntity()).getProxy()
            .getNode();
    }

    @Override
    public boolean isWirelessEndpointValid() {
        World world = getEndpointWorld();
        return isEligible(base) && world != null && !world.isRemote && !base.isDead();
    }

    @Override
    public World getEndpointWorld() {
        return base.getWorld();
    }

    @Override
    public IChatComponent getEndpointDisplayName() {
        return new ChatComponentTranslation(
            base.getMetaTileEntity()
                .getLocalNameKey());
    }

    @Override
    public WirelessLeaseStatus getWirelessLeaseStatus() {
        return status;
    }

    @Override
    public void bindToNetwork(UUID networkId, EntityPlayer player) {
        World world = getEndpointWorld();
        if (world == null || world.isRemote || networkId == null || player == null) return;
        if (!WirelessNetworkService.hasPermission(networkId, world, player)) {
            status = WirelessLeaseStatus.NO_PERMISSION;
            return;
        }
        destroyConnection();
        targetNetwork = networkId;
        bindingPlayerId = WorldData.instance()
            .playerData()
            .getPlayerID(player.getGameProfile());
        IGridNode node = getWirelessGridNode();
        if (node != null) node.setPlayerID(bindingPlayerId);
        requestedChannels = WirelessChannelUsage.get(null, node);
        base.markDirty();
        WirelessNetworkService.registerEndpoint(this, world);
    }

    @Override
    public void unbindFromNetwork() {
        WirelessNetworkService.unregisterEndpoint(this);
        destroyConnection();
        targetNetwork = null;
        bindingPlayerId = -1;
        requestedChannels = WirelessChannelUsage.get(null, getWirelessGridNode());
        status = WirelessLeaseStatus.UNBOUND;
        base.markDirty();
    }

    @Override
    public void setWirelessPriority(int priority) {
        this.priority = Math.max(0, Math.min(100, priority));
        base.markDirty();
        WirelessNetworkService.registerEndpoint(this, getEndpointWorld());
    }

    @Override
    public void setWirelessLease(WirelessLeaseStatus status, TileWirelessControllerRef controller) {
        this.status = status;
        if (status != WirelessLeaseStatus.CONNECTING || controller == null) {
            destroyConnection();
            return;
        }
        if (remoteConnection != null) {
            if (isConnectionAlive()) {
                this.status = WirelessLeaseStatus.CONNECTED;
                return;
            }
            // The AE2 side already tore this connection down (controller chunk unload, grid
            // split, security break, ...). Drop the stale reference and reconnect below.
            remoteConnection = null;
        }
        try {
            IGridNode source = getWirelessGridNode();
            IGridNode target = controller.getGridNode();
            if (source != null && target != null) {
                source.setPlayerID(bindingPlayerId);
                remoteConnection = AEApi.instance()
                    .createGridConnection(source, target);
                this.status = WirelessLeaseStatus.CONNECTED;
                connectionFailureLogged = false;
            }
        } catch (FailedConnection e) {
            if (!connectionFailureLogged) {
                connectionFailureLogged = true;
                AEWirelessNexus.LOG
                    .warn("Wireless endpoint {} failed to create grid connection: {}", getStableEndpointKey(), e);
            }
            this.status = WirelessLeaseStatus.CONNECTING;
        }
    }

    private boolean isConnectionAlive() {
        IGridNode node = getWirelessGridNode();
        return node != null && node.getConnections()
            .contains(remoteConnection);
    }

    public void tick(long tick) {
        if (tick % 20 == 0) WirelessNetworkService.registerEndpoint(this, getEndpointWorld());
    }

    public void unload() {
        WirelessNetworkService.unregisterEndpoint(this);
        destroyConnection();
    }

    public void readFromNBT(NBTTagCompound tag) {
        if (tag.hasKey("AEWirelessNexusTarget")) {
            try {
                targetNetwork = UUID.fromString(tag.getString("AEWirelessNexusTarget"));
            } catch (IllegalArgumentException ignored) {
                targetNetwork = null;
            }
        }
        bindingPlayerId = tag.hasKey("AEWirelessNexusPlayer") ? tag.getInteger("AEWirelessNexusPlayer") : -1;
        priority = Math.max(0, Math.min(100, tag.getInteger("AEWirelessNexusPriority")));
        requestedChannels = Math.max(0, Math.min(32, tag.getInteger("AEWirelessNexusRequestedChannels")));
    }

    public void writeToNBT(NBTTagCompound tag) {
        if (targetNetwork != null) tag.setString("AEWirelessNexusTarget", targetNetwork.toString());
        tag.setInteger("AEWirelessNexusPlayer", bindingPlayerId);
        tag.setInteger("AEWirelessNexusPriority", priority);
        tag.setInteger("AEWirelessNexusRequestedChannels", requestedChannels);
    }

    private void destroyConnection() {
        if (remoteConnection != null) {
            remoteConnection.destroy();
            remoteConnection = null;
        }
        connectionFailureLogged = false;
    }
}
