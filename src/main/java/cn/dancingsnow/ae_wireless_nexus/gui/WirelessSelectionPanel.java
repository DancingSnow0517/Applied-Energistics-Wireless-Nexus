package cn.dancingsnow.ae_wireless_nexus.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.DynamicLinkedSyncHandler;
import com.cleanroommc.modularui.value.sync.GenericListSyncHandler;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.DynamicSyncedWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.ScrollingTextWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import cn.dancingsnow.ae_wireless_nexus.AEWirelessNexus;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessBindableEndpoint;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessNetworkRecord;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessNetworkService;

public final class WirelessSelectionPanel {

    private static final int CARD_HEIGHT = 26;
    private static final int LIST_HEIGHT = 93;

    private WirelessSelectionPanel() {}

    public static ModularPanel build(String panelName, WirelessBindableEndpoint endpoint, EntityPlayer player,
        PanelSyncManager syncManager, boolean showCloseButton) {
        IntSyncValue priority = new IntSyncValue(endpoint::getWirelessPriority, endpoint::setWirelessPriority)
            .allowC2S();
        StringSyncValue status = new StringSyncValue(
            () -> "gui.ae_wireless_nexus.status." + endpoint.getWirelessLeaseStatus()
                .name()
                .toLowerCase(java.util.Locale.ROOT));
        GenericListSyncHandler<NetworkEntry> networks = GenericListSyncHandler.<NetworkEntry>builder()
            .getter(() -> entries(endpoint, player))
            .serializer(WirelessSelectionPanel::writeEntry)
            .deserializer(WirelessSelectionPanel::readEntry)
            .immutableCopy()
            .build();
        syncManager.syncValue("wireless_status", status);
        syncManager.syncValue("wireless_networks", networks);

        ScrollPosition scrollPosition = new ScrollPosition();
        DynamicLinkedSyncHandler<GenericListSyncHandler<NetworkEntry>> listHandler = new DynamicLinkedSyncHandler<>(
            networks).widgetProvider(
                (dynamicSyncManager,
                    value) -> networkList(value.getValue(), endpoint, player, dynamicSyncManager, scrollPosition));
        syncManager.syncValue("wireless_network_list", listHandler);

        InteractionSyncHandler disconnect = new InteractionSyncHandler()
            .setOnMousePressed(mouse -> endpoint.unbindFromNetwork());

        ModularPanel panel = ModularPanel.defaultPanel(panelName, 232, 167)
            .themeOverride(AEWirelessNexus.GUI_THEME)
            .child(
                Flow.column()
                    .full()
                    .padding(8)
                    .childPadding(4)
                    .crossAxisAlignment(Alignment.CrossAxis.START)
                    .child(
                        new TextWidget<>(IKey.lang("gui.ae_wireless_nexus.network_selector.title")).widthRel(1F)
                            .textAlign(Alignment.Center)
                            .marginBottom(2))
                    .child(
                        Flow.row()
                            .widthRel(1F)
                            .height(16)
                            .childPadding(4)
                            .child(
                                new TextWidget<>(
                                    IKey.comp(
                                        IKey.lang("gui.ae_wireless_nexus.network_selector.status"),
                                        IKey.SPACE,
                                        IKey.lang(status::getStringValue))).expanded())
                            .child(new TextWidget<>(IKey.lang("gui.ae_wireless_nexus.priority")))
                            .child(
                                new TextFieldWidget().value(priority)
                                    .numbersInt(0, 100)
                                    .width(38)
                                    .height(14)))
                    .child(
                        new DynamicSyncedWidget<>().widthRel(1F)
                            .height(LIST_HEIGHT)
                            .initialChild(
                                networkList(networks.getValue(), endpoint, player, syncManager, scrollPosition))
                            .syncHandler(listHandler))
                    .child(
                        Flow.row()
                            .widthRel(1F)
                            .height(18)
                            .childPadding(4)
                            .child(
                                new TextWidget<>(
                                    IKey.lang(
                                        "gui.ae_wireless_nexus.network_selector.count",
                                        () -> new Object[] { networks.getValue()
                                            .size() })).expanded()
                                                .color(0xFF596469))
                            .child(
                                new ButtonWidget<>().size(64, 16)
                                    .syncHandler(disconnect)
                                    .child(
                                        new TextWidget<>(IKey.lang("gui.ae_wireless_nexus.disconnect")).full()
                                            .textAlign(Alignment.Center)))));
        if (showCloseButton) panel.child(ButtonWidget.panelCloseButton());
        return panel;
    }

    private static IWidget networkList(List<NetworkEntry> entries, WirelessBindableEndpoint endpoint,
        EntityPlayer player, PanelSyncManager syncManager, ScrollPosition scrollPosition) {
        PreservingListWidget list = new PreservingListWidget(scrollPosition).widthRel(1F)
            .height(LIST_HEIGHT)
            .padding(3)
            .crossAxisAlignment(Alignment.CrossAxis.START)
            .background(
                new Rectangle().color(0xFF252C30),
                new Rectangle().color(0xFF737D81)
                    .hollow());
        if (entries.isEmpty()) {
            return list.child(
                new TextWidget<>(IKey.lang("gui.ae_wireless_nexus.network_selector.empty")).widthRel(1F)
                    .height(CARD_HEIGHT)
                    .textAlign(Alignment.CenterLeft)
                    .color(0xFFAEBABC));
        }
        for (NetworkEntry entry : entries) {
            InteractionSyncHandler select = syncManager.getOrCreateSyncHandler(
                "wireless_select_" + entry.id,
                InteractionSyncHandler.class,
                () -> new InteractionSyncHandler()
                    .setOnMousePressed(mouse -> endpoint.bindToNetwork(entry.id, player)));
            list.child(networkCard(entry, select));
        }
        return list;
    }

    private static final class PreservingListWidget extends ListWidget<IWidget, PreservingListWidget> {

        private final ScrollPosition scrollPosition;
        private boolean restoreScroll = true;

        private PreservingListWidget(ScrollPosition scrollPosition) {
            this.scrollPosition = scrollPosition;
        }

        @Override
        public boolean layoutWidgets() {
            boolean layoutComplete = super.layoutWidgets();
            if (layoutComplete && restoreScroll) {
                getScrollData().scrollTo(getScrollArea(), scrollPosition.value);
                restoreScroll = false;
            }
            return layoutComplete;
        }

        @Override
        public void dispose() {
            scrollPosition.value = getScrollY();
            super.dispose();
        }
    }

    private static final class ScrollPosition {

        private int value;
    }

    private static IWidget networkCard(NetworkEntry entry, InteractionSyncHandler select) {
        int available = Math.max(0, entry.totalChannels - entry.allocatedChannels);
        String detailKey = entry.selected ? "gui.ae_wireless_nexus.network_selector.selected_available"
            : "gui.ae_wireless_nexus.network_selector.available";
        return new ButtonWidget<>().widthRel(1F)
            .height(CARD_HEIGHT)
            .marginBottom(3)
            .background(cardBackground(entry.selected, false))
            .hoverBackground(cardBackground(entry.selected, true))
            .syncHandler(select)
            .child(
                Flow.column()
                    .full()
                    .padding(4)
                    .crossAxisAlignment(Alignment.CrossAxis.START)
                    .child(
                        Flow.row()
                            .widthRel(1F)
                            .height(10)
                            .childPadding(4)
                            .child(
                                new ScrollingTextWidget(IKey.str(entry.name)).expanded()
                                    .height(10)
                                    .color(0xFFF0F6F7))
                            .child(
                                new TextWidget<>(entry.allocatedChannels + " / " + entry.totalChannels)
                                    .color(0xFFD9E4E6)))
                    .child(
                        new TextWidget<>(IKey.lang(detailKey, available)).widthRel(1F)
                            .textAlign(Alignment.CenterLeft)
                            .scale(0.8F)
                            .color(entry.selected ? 0xFF8FE4E1 : 0xFFAEBABC)));
    }

    private static IDrawable cardBackground(boolean selected, boolean hovered) {
        int fill = selected ? (hovered ? 0xFF1E5A5C : 0xFF174B4D) : (hovered ? 0xFF3B464B : 0xFF323A3E);
        int border = selected ? 0xFF42D3CF : 0xFF596469;
        return IDrawable.of(
            new Rectangle().color(fill),
            new Rectangle().color(border)
                .hollow(selected ? 2 : 1));
    }

    private static List<NetworkEntry> entries(WirelessBindableEndpoint endpoint, EntityPlayer player) {
        if (endpoint.getEndpointWorld() == null) return Collections.emptyList();
        UUID selectedId = endpoint.getTargetNetworkId();
        List<NetworkEntry> entries = new ArrayList<>();
        for (WirelessNetworkRecord record : WirelessNetworkService
            .getVisibleNetworks(endpoint.getEndpointWorld(), player)) {
            entries.add(
                new NetworkEntry(
                    record.getId(),
                    record.getName(),
                    record.getAllocatedChannels(),
                    record.getTotalChannels(),
                    record.getId()
                        .equals(selectedId)));
        }
        return entries;
    }

    private static void writeEntry(PacketBuffer buffer, NetworkEntry entry) throws IOException {
        buffer.writeLong(entry.id.getMostSignificantBits());
        buffer.writeLong(entry.id.getLeastSignificantBits());
        buffer.writeStringToBuffer(entry.name);
        buffer.writeInt(entry.allocatedChannels);
        buffer.writeInt(entry.totalChannels);
        buffer.writeBoolean(entry.selected);
    }

    private static NetworkEntry readEntry(PacketBuffer buffer) throws IOException {
        UUID id = new UUID(buffer.readLong(), buffer.readLong());
        String name = buffer.readStringFromBuffer(64);
        return new NetworkEntry(id, name, buffer.readInt(), buffer.readInt(), buffer.readBoolean());
    }

    private static final class NetworkEntry {

        private final UUID id;
        private final String name;
        private final int allocatedChannels;
        private final int totalChannels;
        private final boolean selected;

        private NetworkEntry(UUID id, String name, int allocatedChannels, int totalChannels, boolean selected) {
            this.id = id;
            this.name = name;
            this.allocatedChannels = allocatedChannels;
            this.totalChannels = totalChannels;
            this.selected = selected;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof NetworkEntry other)) return false;
            return allocatedChannels == other.allocatedChannels && totalChannels == other.totalChannels
                && selected == other.selected
                && id.equals(other.id)
                && name.equals(other.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, allocatedChannels, totalChannels, selected);
        }
    }
}
