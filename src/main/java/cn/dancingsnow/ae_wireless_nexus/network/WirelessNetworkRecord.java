package cn.dancingsnow.ae_wireless_nexus.network;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;

/** Mutable server-side state for one named wireless network. */
public final class WirelessNetworkRecord {

    private final UUID id;
    private String name;
    private int dimension;
    private int x;
    private int y;
    private int z;
    private int totalChannels;
    private int allocatedChannels;
    private boolean online;

    public WirelessNetworkRecord(UUID id) {
        this.id = id;
        this.name = WirelessNetworkNames.defaultName(id);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        String sanitized = WirelessNetworkNames.sanitize(name);
        if (!sanitized.isEmpty()) this.name = sanitized;
    }

    public int getDimension() {
        return dimension;
    }

    public void setAnchor(int dimension, int x, int y, int z) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getTotalChannels() {
        return totalChannels;
    }

    public void setTotalChannels(int totalChannels) {
        this.totalChannels = Math.max(0, totalChannels);
        this.allocatedChannels = Math.min(this.allocatedChannels, this.totalChannels);
    }

    public int getAllocatedChannels() {
        return allocatedChannels;
    }

    public void setAllocatedChannels(int allocatedChannels) {
        this.allocatedChannels = Math.max(0, Math.min(allocatedChannels, totalChannels));
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Id", id.toString());
        tag.setString("Name", name);
        tag.setInteger("Dimension", dimension);
        tag.setInteger("X", x);
        tag.setInteger("Y", y);
        tag.setInteger("Z", z);
        tag.setInteger("TotalChannels", totalChannels);
        return tag;
    }

    public static WirelessNetworkRecord readFromNBT(NBTTagCompound tag) {
        UUID id;
        try {
            id = UUID.fromString(tag.getString("Id"));
        } catch (IllegalArgumentException e) {
            return null;
        }

        WirelessNetworkRecord record = new WirelessNetworkRecord(id);
        String name = WirelessNetworkNames.sanitize(tag.getString("Name"));
        if (!name.isEmpty()) record.name = name;
        record.dimension = tag.getInteger("Dimension");
        record.x = tag.getInteger("X");
        record.y = tag.getInteger("Y");
        record.z = tag.getInteger("Z");
        record.totalChannels = Math.max(0, tag.getInteger("TotalChannels"));
        record.allocatedChannels = 0;
        record.online = false;
        return record;
    }
}
