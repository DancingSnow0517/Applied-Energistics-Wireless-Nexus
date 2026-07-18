package cn.dancingsnow.ae_wireless_nexus.gui;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import cn.dancingsnow.ae_wireless_nexus.network.WirelessBindableEndpoint;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessNetworkRecord;
import cn.dancingsnow.ae_wireless_nexus.network.WirelessNetworkService;

public final class WirelessSelectionPanel {

    private static final int ROWS_PER_PAGE = 5;

    private WirelessSelectionPanel() {}

    public static ModularPanel build(String panelName, WirelessBindableEndpoint endpoint, EntityPlayer player,
        PanelSyncManager syncManager) {
        int[] page = { 0 };
        IntSyncValue priority = new IntSyncValue(endpoint::getWirelessPriority, endpoint::setWirelessPriority)
            .allowC2S();
        StringSyncValue status = new StringSyncValue(
            () -> "gui.ae_wireless_nexus.status." + endpoint.getWirelessLeaseStatus()
                .name()
                .toLowerCase(java.util.Locale.ROOT));
        StringSyncValue pageLabel = new StringSyncValue(() -> pageLabel(endpoint, player, page[0]));
        syncManager.syncValue("wireless_status", status);
        syncManager.syncValue("wireless_page", pageLabel);

        Flow rows = Flow.column()
            .widthRel(1F)
            .height(92)
            .childPadding(2);
        for (int i = 0; i < ROWS_PER_PAGE; i++) {
            final int row = i;
            StringSyncValue rowId = new StringSyncValue(() -> rowId(endpoint, player, page[0], row));
            StringSyncValue rowLabel = new StringSyncValue(() -> rowLabel(endpoint, player, page[0], row));
            syncManager.syncValue("wireless_row_id", i, rowId);
            syncManager.syncValue("wireless_row_label", i, rowLabel);

            InteractionSyncHandler select = new InteractionSyncHandler().setOnMousePressed(mouse -> {
                try {
                    String id = rowId.getStringValue();
                    if (!id.isEmpty()) endpoint.bindToNetwork(UUID.fromString(id), player);
                } catch (IllegalArgumentException ignored) {}
            });
            rows.child(
                new ButtonWidget<>().height(16)
                    .widthRel(1F)
                    .syncHandler(select)
                    .child(new TextWidget<>(IKey.dynamic(rowLabel::getStringValue)).maxWidth(190)));
        }

        InteractionSyncHandler previous = new InteractionSyncHandler()
            .setOnMousePressed(mouse -> { if (page[0] > 0) page[0]--; });
        InteractionSyncHandler next = new InteractionSyncHandler().setOnMousePressed(mouse -> {
            int pages = pageCount(endpoint, player);
            if (page[0] + 1 < pages) page[0]++;
        });
        InteractionSyncHandler disconnect = new InteractionSyncHandler()
            .setOnMousePressed(mouse -> endpoint.unbindFromNetwork());

        return ModularPanel.defaultPanel(panelName, 220, 166)
            .child(
                Flow.column()
                    .full()
                    .padding(8)
                    .childPadding(4)
                    .child(new TextWidget<>(IKey.lang("gui.ae_wireless_nexus.network_selector.title")))
                    .child(
                        Flow.row()
                            .widthRel(1F)
                            .height(16)
                            .childPadding(4)
                            .child(new TextWidget<>(IKey.lang("gui.ae_wireless_nexus.priority")))
                            .child(
                                new TextFieldWidget().value(priority)
                                    .numbersInt(0, 100)
                                    .width(48)
                                    .height(14))
                            .child(sizedText(IKey.lang(status::getStringValue), 80)))
                    .child(rows)
                    .child(
                        Flow.row()
                            .widthRel(1F)
                            .height(18)
                            .childPadding(4)
                            .child(
                                new ButtonWidget<>().size(34, 16)
                                    .syncHandler(previous)
                                    .child(new TextWidget<>("<")))
                            .child(sizedText(IKey.dynamic(pageLabel::getStringValue), 50))
                            .child(
                                new ButtonWidget<>().size(34, 16)
                                    .syncHandler(next)
                                    .child(new TextWidget<>(">")))
                            .child(
                                new ButtonWidget<>().size(54, 16)
                                    .syncHandler(disconnect)
                                    .child(new TextWidget<>(IKey.lang("gui.ae_wireless_nexus.disconnect"))))));
    }

    private static List<WirelessNetworkRecord> records(WirelessBindableEndpoint endpoint, EntityPlayer player) {
        if (endpoint.getEndpointWorld() == null) return Collections.emptyList();
        return WirelessNetworkService.getVisibleNetworks(endpoint.getEndpointWorld(), player);
    }

    private static String rowId(WirelessBindableEndpoint endpoint, EntityPlayer player, int page, int row) {
        WirelessNetworkRecord record = recordAt(endpoint, player, page, row);
        return record == null ? ""
            : record.getId()
                .toString();
    }

    private static String rowLabel(WirelessBindableEndpoint endpoint, EntityPlayer player, int page, int row) {
        WirelessNetworkRecord record = recordAt(endpoint, player, page, row);
        if (record == null) return "";
        return record.getName() + "  " + record.getAllocatedChannels() + "/" + record.getTotalChannels();
    }

    private static WirelessNetworkRecord recordAt(WirelessBindableEndpoint endpoint, EntityPlayer player, int page,
        int row) {
        List<WirelessNetworkRecord> records = records(endpoint, player);
        int index = page * ROWS_PER_PAGE + row;
        return index < 0 || index >= records.size() ? null : records.get(index);
    }

    private static int pageCount(WirelessBindableEndpoint endpoint, EntityPlayer player) {
        return Math.max(1, (records(endpoint, player).size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
    }

    private static String pageLabel(WirelessBindableEndpoint endpoint, EntityPlayer player, int page) {
        int count = pageCount(endpoint, player);
        return Math.min(page + 1, count) + " / " + count;
    }

    private static TextWidget<?> sizedText(IKey key, int width) {
        TextWidget<?> widget = new TextWidget<>(key);
        widget.width(width);
        return widget;
    }
}
