package cn.dancingsnow.ae_wireless_nexus.tile;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.networking.TileController;
import cn.dancingsnow.ae_wireless_nexus.AEWirelessNexus;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessNetworkRecord;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessNetworkSavedData;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessNetworkService;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileWirelessController extends TileController implements IGuiHolder<PosGuiData> {

    private UUID networkId = UUID.randomUUID();
    private int refreshCounter;

    public UUID getNetworkId() {
        return networkId;
    }

    public void setNetworkId(UUID networkId) {
        if (networkId != null) this.networkId = networkId;
    }

    public World getWorld() {
        return worldObj;
    }

    public int getX() {
        return xCoord;
    }

    public int getY() {
        return yCoord;
    }

    public int getZ() {
        return zCoord;
    }

    public String getNetworkName() {
        return WirelessNetworkService.getNetworkName(this);
    }

    public void setNetworkName(String name) {
        WirelessNetworkService.setNetworkName(this, name);
    }

    public void openConfiguration(net.minecraft.entity.player.EntityPlayer player) {
        GuiFactories.tileEntity()
            .open(player, this);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        StringSyncValue name = new StringSyncValue(this::getNetworkName, this::setNetworkName).allowC2S();
        IntSyncValue total = new IntSyncValue(() -> recordValue(Value.TOTAL));
        IntSyncValue allocated = new IntSyncValue(() -> recordValue(Value.ALLOCATED));
        IntSyncValue available = new IntSyncValue(() -> recordValue(Value.AVAILABLE));
        syncManager.syncValue("wireless_total", total);
        syncManager.syncValue("wireless_allocated", allocated);
        syncManager.syncValue("wireless_available", available);

        return ModularPanel.defaultPanel("wireless_controller", 200, 118)
            .child(
                Flow.column()
                    .full()
                    .padding(8)
                    .childPadding(6)
                    .child(new TextWidget(IKey.lang("gui.ae_wireless_nexus.controller.title")))
                    .child(
                        new TextFieldWidget().value(name)
                            .setMaxLength(64)
                            .setValidator(cn.dancingsnow.ae_wireless_nexus.network.WirelessNetworkNames::sanitize)
                            .widthRel(1F)
                            .height(16))
                    .child(
                        new TextWidget(
                            IKey.lang(
                                "gui.ae_wireless_nexus.channels.total",
                                () -> new Object[] { total.getIntValue() })))
                    .child(
                        new TextWidget(
                            IKey.lang(
                                "gui.ae_wireless_nexus.channels.allocated",
                                () -> new Object[] { allocated.getIntValue() })))
                    .child(
                        new TextWidget(
                            IKey.lang(
                                "gui.ae_wireless_nexus.channels.available",
                                () -> new Object[] { available.getIntValue() }))));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModularScreen createScreen(PosGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(AEWirelessNexus.MODID, mainPanel);
    }

    private int recordValue(Value value) {
        if (worldObj == null) return 0;
        WirelessNetworkSavedData savedData = WirelessNetworkSavedData.get(worldObj);
        WirelessNetworkRecord record = savedData == null ? null : savedData.get(networkId);
        if (record == null) return 0;
        switch (value) {
            case TOTAL:
                return record.getTotalChannels();
            case ALLOCATED:
                return record.getAllocatedChannels();
            default:
                return Math.max(0, record.getTotalChannels() - record.getAllocatedChannels());
        }
    }

    private enum Value {
        TOTAL,
        ALLOCATED,
        AVAILABLE
    }

    @Override
    public void onNeighborChange(boolean force) {
        super.onNeighborChange(force);
        WirelessNetworkService.onControllerChanged(this);
    }

    @Override
    public void onReady() {
        super.onReady();
        WirelessNetworkService.onControllerChanged(this);
    }

    @TileEvent(TileEventType.TICK)
    public void tickWirelessNetwork() {
        if (worldObj == null || worldObj.isRemote) return;
        if (++refreshCounter >= 20) {
            refreshCounter = 0;
            WirelessNetworkService.onControllerChanged(this);
        }
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBT_WirelessNetwork(NBTTagCompound data) {
        if (data.hasKey("WirelessNetworkId")) {
            try {
                networkId = UUID.fromString(data.getString("WirelessNetworkId"));
            } catch (IllegalArgumentException ignored) {
                networkId = UUID.randomUUID();
            }
        }
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public void writeToNBT_WirelessNetwork(NBTTagCompound data) {
        data.setString("WirelessNetworkId", networkId.toString());
    }
}
