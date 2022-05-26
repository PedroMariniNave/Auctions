package com.zpedroo.voltzauctions.managers.cache;

import com.zpedroo.voltzauctions.objects.PlayerData;
import com.zpedroo.voltzauctions.VoltzAuctions;
import com.zpedroo.voltzauctions.mysql.DBConnection;
import com.zpedroo.voltzauctions.objects.Auction;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class DataCache {

    private Map<Long, Auction> auctions;
    private final List<Long> auctionsToDelete;
    private final Map<Player, PlayerData> playerData;

    public DataCache() {
        VoltzAuctions.get().getServer().getScheduler().runTaskLater(VoltzAuctions.get(), () -> {
            this.auctions = DBConnection.getInstance().getDBManager().getAuctionsFromDatabase();
        }, 40L);
        this.auctionsToDelete = new ArrayList<>(16);
        this.playerData = new HashMap<>(32);
    }

    public Map<Long, Auction> getAuctions() {
        return auctions;
    }

    public List<Long> getAuctionsIdsToDelete() {
        return auctionsToDelete;
    }

    public Map<Player, PlayerData> getPlayerData() {
        return playerData;
    }

    public List<Auction> getActiveAuctions() {
        return new HashSet<>(
                auctions.values()).stream().filter(auction -> System.currentTimeMillis() < auction.getExpirationDateInMillis()).collect(Collectors.toList()
        );
    }

    public List<Auction> getPlayerActiveAuctions(Player player) {
        return getActiveAuctions().stream().filter(
                auction -> StringUtils.equals(player.getUniqueId().toString(), auction.getSellerUniqueId().toString())).collect(Collectors.toList()
        );
    }
}