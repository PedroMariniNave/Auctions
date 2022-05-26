package com.zpedroo.voltzauctions.objects;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private final List<ItemStack> itemsToCollect;
    private boolean update;

    public PlayerData(UUID uuid, List<ItemStack> itemsToCollect) {
        this.uuid = uuid;
        this.itemsToCollect = itemsToCollect;
        this.update = false;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public List<ItemStack> getItemsToCollect() {
        return itemsToCollect;
    }

    public boolean isQueueUpdate() {
        return update;
    }

    public void addItemToCollect(ItemStack item) {
        this.itemsToCollect.add(item);
        this.update = true;
    }

    public void removeItemToCollect(ItemStack item) {
        this.itemsToCollect.remove(item);
        this.update = true;
    }
}