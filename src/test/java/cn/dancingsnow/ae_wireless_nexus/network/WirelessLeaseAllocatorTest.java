package cn.dancingsnow.ae_wireless_nexus.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

class WirelessLeaseAllocatorTest {

    @Test
    void higherPriorityWins() {
        Set<String> granted = WirelessLeaseAllocator
            .allocate(32, Arrays.asList(request(0, 32, "low"), request(10, 32, "high")));
        assertEquals(Collections.singleton("high"), granted);
    }

    @Test
    void equalPriorityUsesStableKey() {
        Set<String> granted = WirelessLeaseAllocator
            .allocate(32, Arrays.asList(request(0, 32, "z"), request(0, 32, "a")));
        assertEquals(Collections.singleton("a"), granted);
    }

    private static WirelessLeaseAllocator.Request request(int priority, int channels, String key) {
        return new WirelessLeaseAllocator.Request() {

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public int getChannels() {
                return channels;
            }

            @Override
            public String getStableKey() {
                return key;
            }
        };
    }
}
