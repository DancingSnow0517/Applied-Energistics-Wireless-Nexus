package cn.dancingsnow.ae_wireless_nexus.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.DimensionManager;

import cn.dancingsnow.ae_wireless_nexus.AEWirelessNexus;

public final class WirelessNetworkSavedData extends WorldSavedData {

    private static final String DATA_KEY = AEWirelessNexus.MODID + ".networks";
    private static final String RECORDS_KEY = "Networks";

    private final Map<UUID, WirelessNetworkRecord> records = new HashMap<>();

    public WirelessNetworkSavedData() {
        super(DATA_KEY);
    }

    public WirelessNetworkSavedData(String name) {
        super(name);
    }

    public static WirelessNetworkSavedData get(World world) {
        if (world == null) return null;

        World primaryWorld = DimensionManager.getWorld(0);
        MapStorage storage = (primaryWorld == null ? world : primaryWorld).mapStorage;
        if (storage == null) return null;
        WirelessNetworkSavedData data = (WirelessNetworkSavedData) storage
            .loadData(WirelessNetworkSavedData.class, DATA_KEY);
        if (data == null) {
            data = new WirelessNetworkSavedData();
            storage.setData(DATA_KEY, data);
        }
        return data;
    }

    public WirelessNetworkRecord getOrCreate(UUID id) {
        WirelessNetworkRecord record = records.get(id);
        if (record == null) {
            record = new WirelessNetworkRecord(id);
            records.put(id, record);
            markDirty();
        }
        return record;
    }

    public WirelessNetworkRecord get(UUID id) {
        return records.get(id);
    }

    public Collection<WirelessNetworkRecord> records() {
        return Collections.unmodifiableCollection(new ArrayList<>(records.values()));
    }

    public void remove(UUID id) {
        if (records.remove(id) != null) markDirty();
    }

    public void mergeInto(UUID keep, UUID remove) {
        if (keep.equals(remove)) return;
        WirelessNetworkRecord kept = getOrCreate(keep);
        WirelessNetworkRecord removed = records.remove(remove);
        if (removed != null) {
            if (kept.getName()
                .startsWith("ME Wireless Network ")
                && !removed.getName()
                    .startsWith("ME Wireless Network ")) {
                kept.setName(removed.getName());
            }
            markDirty();
        }
    }

    public WirelessNetworkRecord copyTo(UUID source, UUID target) {
        WirelessNetworkRecord sourceRecord = records.get(source);
        WirelessNetworkRecord targetRecord = getOrCreate(target);
        if (sourceRecord != null) targetRecord.setName(sourceRecord.getName());
        markDirty();
        return targetRecord;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        records.clear();
        NBTTagList list = tag.getTagList(RECORDS_KEY, 10);
        for (int i = 0; i < list.tagCount(); i++) {
            WirelessNetworkRecord record = WirelessNetworkRecord.readFromNBT(list.getCompoundTagAt(i));
            if (record != null) {
                record.setOnline(false);
                record.setAllocatedChannels(0);
                records.put(record.getId(), record);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (WirelessNetworkRecord record : records.values()) list.appendTag(record.writeToNBT());
        tag.setTag(RECORDS_KEY, list);
    }
}
