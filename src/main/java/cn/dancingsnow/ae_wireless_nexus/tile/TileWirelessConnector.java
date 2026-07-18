package cn.dancingsnow.ae_wireless_nexus.tile;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnection;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.core.worlddata.WorldData;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import cn.dancingsnow.ae_wireless_nexus.AEWirelessNexus;
import cn.dancingsnow.ae_wireless_nexus.gui.WirelessSelectionPanel;
import cn.dancingsnow.ae_wireless_nexus.network.TileWirelessControllerRef;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessBindableEndpoint;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessChannelUsage;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessLeaseStatus;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessNetworkService;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileWirelessConnector extends AENetworkTile implements IGuiHolder<PosGuiData>, WirelessBindableEndpoint {

    private UUID targetNetwork;
    private IGridConnection remoteConnection;
    private int bindingPlayerId = -1;
    private int reconnectCounter;
    private int priority;
    private int requestedChannels;
    private WirelessLeaseStatus leaseStatus = WirelessLeaseStatus.UNBOUND;

    @Override
    protected AENetworkProxy createProxy() {
        AENetworkProxy proxy = super.createProxy();
        proxy.setFlags(GridFlags.DENSE_CAPACITY);
        return proxy;
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.SMART;
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
    public void setWirelessPriority(int priority) {
        this.priority = Math.max(0, Math.min(100, priority));
        markDirty();
        WirelessNetworkService.registerEndpoint(this, worldObj);
    }

    @Override
    public int getRequestedChannels() {
        if (remoteConnection != null) {
            requestedChannels = WirelessChannelUsage.get(remoteConnection, getWirelessGridNode());
        }
        return requestedChannels;
    }

    @Override
    public int getBindingPlayerId() {
        return bindingPlayerId;
    }

    @Override
    public String getStableEndpointKey() {
        return worldObj == null ? "connector:unloaded:" + System.identityHashCode(this)
            : "connector:" + worldObj.provider.dimensionId + ":" + xCoord + ":" + yCoord + ":" + zCoord;
    }

    @Override
    public IGridNode getWirelessGridNode() {
        return getProxy().getNode();
    }

    @Override
    public boolean isWirelessEndpointValid() {
        return worldObj != null && !worldObj.isRemote && !isInvalid();
    }

    @Override
    public WirelessLeaseStatus getWirelessLeaseStatus() {
        return leaseStatus;
    }

    @Override
    public World getEndpointWorld() {
        return worldObj;
    }

    public void openSelection(EntityPlayer player) {
        GuiFactories.tileEntity()
            .open(player, this);
    }

    @Override
    public void bindToNetwork(UUID networkId, EntityPlayer player) {
        if (worldObj == null || worldObj.isRemote || networkId == null || player == null) return;
        if (!WirelessNetworkService.hasPermission(networkId, worldObj, player)) {
            leaseStatus = WirelessLeaseStatus.NO_PERMISSION;
            return;
        }
        destroyRemoteConnection();
        targetNetwork = networkId;
        bindingPlayerId = WorldData.instance()
            .playerData()
            .getPlayerID(player.getGameProfile());
        IGridNode node = getWirelessGridNode();
        if (node != null) node.setPlayerID(bindingPlayerId);
        requestedChannels = WirelessChannelUsage.get(null, node);
        markDirty();
        WirelessNetworkService.registerEndpoint(this, worldObj);
    }

    @Override
    public void unbindFromNetwork() {
        WirelessNetworkService.unregisterEndpoint(this);
        destroyRemoteConnection();
        targetNetwork = null;
        bindingPlayerId = -1;
        requestedChannels = 0;
        leaseStatus = WirelessLeaseStatus.UNBOUND;
        markDirty();
    }

    @Override
    public void setWirelessLease(WirelessLeaseStatus status, TileWirelessControllerRef controller) {
        leaseStatus = status;
        if (status != WirelessLeaseStatus.CONNECTING || controller == null) {
            destroyRemoteConnection();
            return;
        }
        if (remoteConnection != null) {
            leaseStatus = WirelessLeaseStatus.CONNECTED;
            return;
        }
        try {
            IGridNode source = getWirelessGridNode();
            IGridNode target = controller.getGridNode();
            if (source != null && target != null) {
                source.setPlayerID(bindingPlayerId);
                remoteConnection = AEApi.instance()
                    .createGridConnection(source, target);
                leaseStatus = WirelessLeaseStatus.CONNECTED;
            }
        } catch (FailedConnection ignored) {
            leaseStatus = WirelessLeaseStatus.CONNECTING;
        }
    }

    private void destroyRemoteConnection() {
        if (remoteConnection != null) {
            remoteConnection.destroy();
            remoteConnection = null;
        }
    }

    @TileEvent(TileEventType.TICK)
    public void tickWirelessConnector() {
        if (worldObj == null || worldObj.isRemote) return;
        if (++reconnectCounter >= 20) {
            reconnectCounter = 0;
            WirelessNetworkService.registerEndpoint(this, worldObj);
        }
    }

    @Override
    public void invalidate() {
        WirelessNetworkService.unregisterEndpoint(this);
        destroyRemoteConnection();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        WirelessNetworkService.unregisterEndpoint(this);
        destroyRemoteConnection();
        super.onChunkUnload();
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return WirelessSelectionPanel.build("wireless_connector", this, data.getPlayer(), syncManager);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModularScreen createScreen(PosGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(AEWirelessNexus.MODID, mainPanel);
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBT_WirelessConnector(NBTTagCompound data) {
        if (data.hasKey("WirelessTarget")) {
            try {
                targetNetwork = UUID.fromString(data.getString("WirelessTarget"));
            } catch (IllegalArgumentException ignored) {
                targetNetwork = null;
            }
        }
        bindingPlayerId = data.hasKey("WirelessPlayerId") ? data.getInteger("WirelessPlayerId") : -1;
        priority = Math.max(0, Math.min(100, data.getInteger("WirelessPriority")));
        requestedChannels = Math.max(0, Math.min(32, data.getInteger("WirelessRequestedChannels")));
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public void writeToNBT_WirelessConnector(NBTTagCompound data) {
        if (targetNetwork != null) data.setString("WirelessTarget", targetNetwork.toString());
        data.setInteger("WirelessPlayerId", bindingPlayerId);
        data.setInteger("WirelessPriority", priority);
        data.setInteger("WirelessRequestedChannels", requestedChannels);
    }
}
