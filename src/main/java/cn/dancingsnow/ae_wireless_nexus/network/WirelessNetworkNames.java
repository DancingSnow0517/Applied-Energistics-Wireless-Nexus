package cn.dancingsnow.ae_wireless_nexus.network;

/** Validation rules for the player-visible network name. */
public final class WirelessNetworkNames {

    public static final int MAX_LENGTH = 32;

    private WirelessNetworkNames() {}

    public static String sanitize(String value) {
        if (value == null) return "";

        StringBuilder result = new StringBuilder(Math.min(value.length(), MAX_LENGTH));
        int codePoints = 0;
        for (int offset = 0; offset < value.length() && codePoints < MAX_LENGTH;) {
            int codePoint = value.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (Character.isISOControl(codePoint) || codePoint == '\n' || codePoint == '\r') continue;
            result.appendCodePoint(codePoint);
            codePoints++;
        }
        return result.toString()
            .trim();
    }

    public static String defaultName(java.util.UUID id) {
        String shortId = id.toString()
            .substring(0, 8);
        return "ME Wireless Network " + shortId;
    }
}
