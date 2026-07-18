package cn.dancingsnow.ae_wireless_nexus.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Pure deterministic allocator used by the runtime service and unit tests. */
public final class WirelessLeaseAllocator {

    private WirelessLeaseAllocator() {}

    public static Set<String> allocate(int capacity, Collection<? extends Request> requests) {
        List<Request> sorted = new ArrayList<>(requests);
        Collections.sort(
            sorted,
            Comparator.comparingInt(Request::getPriority)
                .reversed()
                .thenComparing(Request::getStableKey));

        int remaining = Math.max(0, capacity);
        Set<String> granted = new HashSet<>();
        for (Request request : sorted) {
            int channels = Math.max(0, request.getChannels());
            if (channels <= remaining) {
                granted.add(request.getStableKey());
                remaining -= channels;
            }
        }
        return granted;
    }

    public interface Request {

        int getPriority();

        int getChannels();

        String getStableKey();
    }
}
