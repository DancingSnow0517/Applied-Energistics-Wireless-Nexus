package cn.dancingsnow.ae_wireless_nexus.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WirelessChannelUsageTest {

    @Test
    void cannotCarryNodeOnlyRequestsItsOwnChannel() {
        assertEquals(1, WirelessChannelUsage.getMaximum(true, true, false));
        assertEquals(0, WirelessChannelUsage.getMaximum(false, true, false));
    }

    @Test
    void normalAndDenseNodesKeepAeChannelLimits() {
        assertEquals(8, WirelessChannelUsage.getMaximum(false, false, false));
        assertEquals(32, WirelessChannelUsage.getMaximum(false, false, true));
    }
}
