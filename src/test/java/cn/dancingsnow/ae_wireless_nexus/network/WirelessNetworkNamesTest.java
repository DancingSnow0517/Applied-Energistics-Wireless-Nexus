package cn.dancingsnow.ae_wireless_nexus.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WirelessNetworkNamesTest {

    @Test
    void preservesVisibleUnicodeAndRemovesControls() {
        assertEquals("主网络 A", WirelessNetworkNames.sanitize("  主\n网\u0000络 A  "));
    }

    @Test
    void limitsByUnicodeCodePoint() {
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < 31; i++) value.append("网");
        value.append("\uD83D\uDE80extra");
        String sanitized = WirelessNetworkNames.sanitize(value.toString());
        assertEquals(32, sanitized.codePointCount(0, sanitized.length()));
        assertEquals("\uD83D\uDE80", sanitized.substring(sanitized.length() - 2));
    }
}
