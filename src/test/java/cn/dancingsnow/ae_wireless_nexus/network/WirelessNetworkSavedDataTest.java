package cn.dancingsnow.ae_wireless_nexus.network;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class WirelessNetworkSavedDataTest {

    @Test
    void removingControllerDataDeletesTheCompleteRecord() {
        WirelessNetworkSavedData data = new WirelessNetworkSavedData();
        UUID id = UUID.randomUUID();
        WirelessNetworkRecord record = data.getOrCreate(id);
        record.setName("Temporary network");
        record.setAnchor(7, 10, 20, 30);
        record.setTotalChannels(96);
        record.setOnline(true);

        data.remove(id);

        assertNull(data.get(id));
        assertTrue(
            data.records()
                .isEmpty());
    }
}
