package com.zpedroo.voltzauctions.managers;

import com.zpedroo.voltzauctions.managers.cache.DataCache;
import com.zpedroo.voltzauctions.mysql.DBConnection;
import com.zpedroo.voltzauctions.objects.PlayerData;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class DataManager {

    private static DataManager instance;
    public static DataManager getInstance() { return instance; }

    private final DataCache dataCache = new DataCache();

    public DataManager() {
        instance = this;
    }

    public PlayerData getPlayerData(Player player) {
        PlayerData data = dataCache.getPlayerData().get(player);
        if (data == null) {
            data = DBConnection.getInstance().getDBManager().getPlayerDataFromDatabase(player);
            dataCache.getPlayerData().put(player, data);
        }

        return data;
    }

    public void savePlayerData(Player player) {
        PlayerData data = dataCache.getPlayerData().get(player);
        if (data == null || !data.isQueueUpdate()) return;

        DBConnection.getInstance().getDBManager().savePlayerData(data);
    }

    public void saveAllAuctionsData() {
        new HashSet<>(dataCache.getAuctionsIdsToDelete()).forEach(auction -> {
            DBConnection.getInstance().getDBManager().deleteAuction(auction);
        });

        new HashSet<>(dataCache.getAuctions().values()).forEach(auction -> {
            if (!auction.isQueueUpdate()) return;

            DBConnection.getInstance().getDBManager().saveAuctionData(auction);
        });
    }

    public void saveAllPlayersData() {
        new HashSet<>(dataCache.getPlayerData().keySet()).forEach(this::savePlayerData);
    }

    public int getAuctionsAmount() {
        return dataCache.getActiveAuctions().size();
    }

    public DataCache getCache() {
        return dataCache;
    }
}