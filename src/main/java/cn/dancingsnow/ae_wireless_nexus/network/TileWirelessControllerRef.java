package cn.dancingsnow.ae_wireless_nexus.network;

import appeng.api.networking.IGridNode;
import cn.dancingsnow.ae_wireless_nexus.tile.TileWirelessController;

/** Short-lived controller reference supplied only while a lease is active. */
public final class TileWirelessControllerRef {

    private final TileWirelessController controller;

    public TileWirelessControllerRef(TileWirelessController controller) {
        this.controller = controller;
    }

    public IGridNode getGridNode() {
        return controller == null ? null
            : controller.getProxy()
                .getNode();
    }
}
