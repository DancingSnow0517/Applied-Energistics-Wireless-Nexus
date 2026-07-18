package cn.dancingsnow.ae_wireless_nexus.network;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridHost;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.networking.security.ISecurityGrid;
import appeng.me.GridAccessException;
import appeng.tile.networking.TileController;
import cn.dancingsnow.ae_wireless_nexus.tile.TileWirelessController;

public final class WirelessNetworkService {

    public static final int CHANNELS_PER_EXPOSED_FACE = 32;

    private static final Map<String, WeakReference<WirelessEndpoint>> ENDPOINTS = new HashMap<>();
    private static long lastAllocationTick = Long.MIN_VALUE;

    private WirelessNetworkService() {}

    public static void onControllerChanged(TileWirelessController controller) {
        if (!isServerController(controller)) return;

        List<TileController> group = collectControllers(controller, null);
        if (group.isEmpty()) return;

        World world = controller.getWorld();
        WirelessNetworkSavedData data = WirelessNetworkSavedData.get(world);
        if (data == null) return;

        updateGroup(group, data, world);
        allocateLeases(world);
    }

    private static void updateGroup(List<TileController> group, WirelessNetworkSavedData data, World world) {
        List<TileWirelessController> wirelessControllers = getWirelessControllers(group);
        if (wirelessControllers.size() != 1) {
            for (TileWirelessController wireless : wirelessControllers) markControllerOffline(data, wireless);
            data.markDirty();
            return;
        }

        TileWirelessController controller = wirelessControllers.get(0);
        WirelessNetworkRecord record = data.getOrCreate(controller.getNetworkId());
        record.setAnchor(world.provider.dimensionId, controller.getX(), controller.getY(), controller.getZ());
        boolean conflicted = hasControllerConflict(controller);
        record.setTotalChannels(conflicted ? 0 : scanCapacity(group));
        record.setOnline(!conflicted);
        data.markDirty();
    }

    public static void onControllerRemoved(TileWirelessController removed) {
        if (!isServerController(removed)) return;

        World world = removed.getWorld();
        UUID oldId = removed.getNetworkId();
        List<TileController> neighbours = new ArrayList<>();
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tile = world.getTileEntity(
                removed.getX() + side.offsetX,
                removed.getY() + side.offsetY,
                removed.getZ() + side.offsetZ);
            if (tile instanceof TileController) neighbours.add((TileController) tile);
        }

        Set<String> handled = new HashSet<>();
        List<List<TileController>> components = new ArrayList<>();
        for (TileController neighbour : neighbours) {
            String position = positionKey(neighbour.xCoord, neighbour.yCoord, neighbour.zCoord);
            if (handled.contains(position)) continue;
            List<TileController> component = collectControllers(neighbour, removed);
            components.add(component);
            for (TileController member : component) {
                handled.add(positionKey(member.xCoord, member.yCoord, member.zCoord));
            }
        }

        WirelessNetworkSavedData data = WirelessNetworkSavedData.get(world);
        if (data == null) return;
        WirelessNetworkRecord oldRecord = data.get(oldId);
        if (oldRecord != null) {
            oldRecord.setOnline(false);
            oldRecord.setTotalChannels(0);
            oldRecord.setAllocatedChannels(0);
        }
        for (List<TileController> component : components) updateGroup(component, data, world);
        data.markDirty();
        allocateLeases(world);
    }

    public static void setNetworkName(TileWirelessController controller, String name) {
        if (!isServerController(controller)) return;
        WirelessNetworkSavedData data = WirelessNetworkSavedData.get(controller.getWorld());
        if (data == null) return;
        data.getOrCreate(controller.getNetworkId())
            .setName(name);
        data.markDirty();
    }

    public static String getNetworkName(TileWirelessController controller) {
        if (controller == null || controller.getWorld() == null) return "";
        WirelessNetworkSavedData data = WirelessNetworkSavedData.get(controller.getWorld());
        if (data == null) return WirelessNetworkNames.defaultName(controller.getNetworkId());
        return data.getOrCreate(controller.getNetworkId())
            .getName();
    }

    public static List<WirelessNetworkRecord> getVisibleNetworks(World world, EntityPlayer player) {
        WirelessNetworkSavedData data = WirelessNetworkSavedData.get(world);
        if (data == null) return Collections.emptyList();
        List<WirelessNetworkRecord> visible = new ArrayList<>();
        for (WirelessNetworkRecord record : data.records()) {
            TileWirelessController controller = findController(record);
            if (controller != null && hasPermission(controller, player)) visible.add(record);
        }
        visible.sort(Comparator.comparing(WirelessNetworkRecord::getName)
            .thenComparing(record -> record.getId().toString()));
        return visible;
    }

    public static boolean hasPermission(UUID networkId, World world, EntityPlayer player) {
        WirelessNetworkSavedData data = WirelessNetworkSavedData.get(world);
        if (data == null) return false;
        WirelessNetworkRecord record = data.get(networkId);
        TileWirelessController controller = findController(record);
        return controller != null && hasPermission(controller, player);
    }

    public static boolean hasPermission(UUID networkId, World world, int playerId) {
        if (playerId < 0) return false;
        WirelessNetworkSavedData data = WirelessNetworkSavedData.get(world);
        if (data == null) return false;
        WirelessNetworkRecord record = data.get(networkId);
        TileWirelessController controller = findController(record);
        if (controller == null) return false;
        try {
            ISecurityGrid security = controller.getProxy()
                .getSecurity();
            return security.hasPermission(playerId, SecurityPermissions.BUILD);
        } catch (GridAccessException ignored) {
            return false;
        }
    }

    public static void registerEndpoint(WirelessEndpoint endpoint, World world) {
        if (endpoint == null || world == null || world.isRemote || !endpoint.isWirelessEndpointValid()) return;
        ENDPOINTS.put(endpoint.getStableEndpointKey(), new WeakReference<>(endpoint));
        allocateLeases(world);
    }

    public static void unregisterEndpoint(WirelessEndpoint endpoint) {
        if (endpoint == null) return;
        ENDPOINTS.remove(endpoint.getStableEndpointKey());
        endpoint.setWirelessLease(WirelessLeaseStatus.UNBOUND, null);
    }

    public static void allocateLeases(World world) {
        if (world == null || world.isRemote) return;
        long tick = world.getTotalWorldTime();
        if (tick == lastAllocationTick) return;
        lastAllocationTick = tick;

        WirelessNetworkSavedData data = WirelessNetworkSavedData.get(world);
        if (data == null) return;

        Map<UUID, List<EndpointRequest>> byNetwork = new HashMap<>();
        Iterator<Map.Entry<String, WeakReference<WirelessEndpoint>>> iterator = ENDPOINTS.entrySet()
            .iterator();
        while (iterator.hasNext()) {
            WirelessEndpoint endpoint = iterator.next()
                .getValue()
                .get();
            if (endpoint == null || !endpoint.isWirelessEndpointValid()) {
                iterator.remove();
                continue;
            }
            UUID networkId = endpoint.getTargetNetworkId();
            if (networkId == null) {
                endpoint.setWirelessLease(WirelessLeaseStatus.UNBOUND, null);
                continue;
            }
            byNetwork.computeIfAbsent(networkId, ignored -> new ArrayList<>())
                .add(new EndpointRequest(endpoint));
        }

        for (WirelessNetworkRecord record : data.records()) {
            record.setAllocatedChannels(0);
            TileWirelessController controller = findController(record);
            List<EndpointRequest> requests = byNetwork.remove(record.getId());
            if (requests == null) continue;
            if (controller == null) {
                setStatus(requests, WirelessLeaseStatus.TARGET_OFFLINE, null);
                continue;
            }

            List<EndpointRequest> permitted = new ArrayList<>();
            for (EndpointRequest request : requests) {
                if (hasPermission(record.getId(), world, request.endpoint.getBindingPlayerId())) {
                    permitted.add(request);
                } else {
                    request.endpoint.setWirelessLease(WirelessLeaseStatus.NO_PERMISSION, null);
                }
            }

            Set<String> granted = WirelessLeaseAllocator.allocate(record.getTotalChannels(), permitted);
            int allocated = 0;
            TileWirelessControllerRef reference = new TileWirelessControllerRef(controller);
            for (EndpointRequest request : permitted) {
                if (granted.contains(request.getStableKey())) {
                    allocated += request.getChannels();
                    request.endpoint.setWirelessLease(WirelessLeaseStatus.CONNECTING, reference);
                } else {
                    request.endpoint.setWirelessLease(WirelessLeaseStatus.CAPACITY_EXHAUSTED, null);
                }
            }
            record.setAllocatedChannels(allocated);
        }
        for (List<EndpointRequest> requests : byNetwork.values()) {
            setStatus(requests, WirelessLeaseStatus.TARGET_OFFLINE, null);
        }
        data.markDirty();
    }

    public static TileWirelessController findController(WirelessNetworkRecord record) {
        if (record == null || !record.isOnline()) return null;
        World targetWorld = DimensionManager.getWorld(record.getDimension());
        if (targetWorld == null || !targetWorld.blockExists(record.getX(), record.getY(), record.getZ())) return null;
        TileEntity tile = targetWorld.getTileEntity(record.getX(), record.getY(), record.getZ());
        if (!(tile instanceof TileWirelessController)) return null;
        TileWirelessController controller = (TileWirelessController) tile;
        return record.getId()
            .equals(controller.getNetworkId()) ? controller : null;
    }

    public static int scanCapacity(TileWirelessController controller) {
        return scanCapacity(collectControllers(controller, null));
    }

    private static int scanCapacity(List<TileController> group) {
        Set<String> controllerPositions = new HashSet<>();
        for (TileController controller : group) {
            controllerPositions.add(positionKey(controller.xCoord, controller.yCoord, controller.zCoord));
        }

        int channels = 0;
        for (TileController controller : group) {
            World world = controller.getWorldObj();
            for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                int x = controller.xCoord + side.offsetX;
                int y = controller.yCoord + side.offsetY;
                int z = controller.zCoord + side.offsetZ;
                if (controllerPositions.contains(positionKey(x, y, z))) continue;
                if (!isBlockedByAE(world, x, y, z)) channels += CHANNELS_PER_EXPOSED_FACE;
            }
        }
        return channels;
    }

    private static boolean isBlockedByAE(World world, int x, int y, int z) {
        return world.getTileEntity(x, y, z) instanceof IGridHost;
    }

    public static List<TileController> collectControllers(TileWirelessController origin) {
        return collectControllers(origin, null);
    }

    private static List<TileController> collectControllers(TileController origin, TileController excluded) {
        if (origin == null || origin.getWorldObj() == null || origin == excluded) return Collections.emptyList();

        World world = origin.getWorldObj();
        List<TileController> result = new ArrayList<>();
        Queue<int[]> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(new int[] { origin.xCoord, origin.yCoord, origin.zCoord });

        while (!queue.isEmpty() && result.size() < 343) {
            int[] position = queue.remove();
            String key = positionKey(position[0], position[1], position[2]);
            if (!visited.add(key)) continue;

            TileEntity tile = world.getTileEntity(position[0], position[1], position[2]);
            if (!(tile instanceof TileController controller) || tile == excluded) continue;
            result.add(controller);
            for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                queue.add(
                    new int[] { position[0] + side.offsetX, position[1] + side.offsetY, position[2] + side.offsetZ });
            }
        }
        return result;
    }

    public static boolean hasMultipleWirelessControllers(Iterable<? extends TileController> controllers) {
        int found = 0;
        for (TileController controller : controllers) {
            if (controller instanceof TileWirelessController && ++found > 1) return true;
        }
        return false;
    }

    private static List<TileWirelessController> getWirelessControllers(Iterable<? extends TileController> group) {
        List<TileWirelessController> result = new ArrayList<>();
        for (TileController controller : group) {
            if (controller instanceof TileWirelessController) result.add((TileWirelessController) controller);
        }
        return result;
    }

    private static void markControllerOffline(WirelessNetworkSavedData data, TileWirelessController controller) {
        WirelessNetworkRecord record = data.getOrCreate(controller.getNetworkId());
        record.setAnchor(
            controller.getWorld().provider.dimensionId,
            controller.getX(),
            controller.getY(),
            controller.getZ());
        record.setOnline(false);
        record.setTotalChannels(0);
        record.setAllocatedChannels(0);
    }

    private static boolean hasControllerConflict(TileWirelessController controller) {
        try {
            return controller.getProxy()
                .getPath()
                .getControllerState() == ControllerState.CONTROLLER_CONFLICT;
        } catch (GridAccessException ignored) {
            return false;
        }
    }

    private static boolean hasPermission(TileWirelessController controller, EntityPlayer player) {
        try {
            return controller.getProxy()
                .getSecurity()
                .hasPermission(player, SecurityPermissions.BUILD);
        } catch (GridAccessException ignored) {
            return false;
        }
    }

    private static boolean isServerController(TileWirelessController controller) {
        return controller != null && controller.getWorld() != null && !controller.getWorld().isRemote;
    }

    private static void setStatus(Collection<EndpointRequest> requests, WirelessLeaseStatus status,
        TileWirelessControllerRef reference) {
        for (EndpointRequest request : requests) request.endpoint.setWirelessLease(status, reference);
    }

    private static String positionKey(int x, int y, int z) {
        return x + ":" + y + ":" + z;
    }

    private static final class EndpointRequest implements WirelessLeaseAllocator.Request {

        private final WirelessEndpoint endpoint;

        private EndpointRequest(WirelessEndpoint endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public int getPriority() {
            return endpoint.getWirelessPriority();
        }

        @Override
        public int getChannels() {
            return endpoint.getRequestedChannels();
        }

        @Override
        public String getStableKey() {
            return endpoint.getStableEndpointKey();
        }
    }
}
