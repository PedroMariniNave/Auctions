package com.zpedroo.voltzauctions.managers;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.voltzauctions.objects.Auction;
import com.zpedroo.voltzauctions.objects.Bid;
import com.zpedroo.voltzauctions.objects.PlayerData;
import com.zpedroo.voltzauctions.utils.api.OfflinePlayerAPI;
import com.zpedroo.voltzauctions.utils.config.Messages;
import com.zpedroo.voltzauctions.utils.config.NumberFormatter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.UUID;

public class AuctionManager extends DataManager {

    private static AuctionManager instance;
    public static AuctionManager getInstance() { return instance; }

    public AuctionManager() {
        instance = this;
    }

    public void checkAllAuctionsExpiration() {
        new HashSet<>(getCache().getAuctions().values()).stream().filter(
                auction -> System.currentTimeMillis() >= auction.getExpirationDateInMillis()
        ).forEach(finishedAuction -> {
            getCache().getAuctions().remove(finishedAuction.getId());
            getCache().getAuctionsIdsToDelete().add(finishedAuction.getId());

            Bid lastBid = finishedAuction.getLastBid();

            UUID sellerUUID = finishedAuction.getSellerUniqueId();
            UUID winnerUUID = finishedAuction.getLastBid() == null ? null : finishedAuction.getLastBid().getAuthorUniqueId();
            OfflinePlayer sellerPlayer = Bukkit.getOfflinePlayer(sellerUUID);
            OfflinePlayer lastBidAuthorPlayer = lastBid == null ? null : Bukkit.getOfflinePlayer(winnerUUID);

            Player player = lastBid == null ? OfflinePlayerAPI.getPlayer(sellerPlayer.getName()) : OfflinePlayerAPI.getPlayer(lastBidAuthorPlayer.getName());
            if (player == null) return;

            PlayerData data = getPlayerData(player);
            if (data == null) return;

            ItemStack item = finishedAuction.getItem();
            data.addItemToCollect(item);

            if (lastBid == null) {
                Bukkit.broadcastMessage(StringUtils.replaceEach(Messages.AUCTION_FINISHED_WITHOUT_BID, new String[]{
                        "{player}",
                        "{item}"
                }, new String[]{
                        player.getName(),
                        item.hasItemMeta() ? item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().toString() : item.getType().toString()
                }));
                return;
            }

            CurrencyAPI.addCurrencyAmount(sellerPlayer, finishedAuction.getCurrency(), lastBid.getBidAmount());

            Bukkit.broadcastMessage(StringUtils.replaceEach(Messages.AUCTION_FINISHED_HAS_BID, new String[]{
                    "{player}",
                    "{winner}",
                    "{item}",
                    "{bid_amount}"
            }, new String[]{
                    sellerPlayer.getName(),
                    player.getName(),
                    item.hasItemMeta() ? item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().toString() : item.getType().toString(),
                    NumberFormatter.getInstance().formatValueWithCurrency(lastBid.getBidAmount(), finishedAuction.getCurrency())
            }));
        });
    }

    public void cancelAuctionAndRefoundItem(Player player, Auction auction) {
        if (!isActiveAuction(auction)) return;

        getCache().getAuctions().remove(auction.getId());

        PlayerData data = getPlayerData(player);
        if (data == null) return;

        data.addItemToCollect(auction.getItem());
    }

    public boolean isActiveAuction(Auction auction) {
        return getCache().getActiveAuctions().contains(auction);
    }

    public void createAuctionAndCache(Player player, ItemStack item, Currency currency, BigInteger minimumBid, long expirationDateInMillis) {
        long auctionId = System.nanoTime();
        Auction auction = new Auction(auctionId, player.getUniqueId(), item, currency, minimumBid, null, expirationDateInMillis);
        auction.setUpdate(true);

        getCache().getAuctions().put(auctionId, auction);
    }
}