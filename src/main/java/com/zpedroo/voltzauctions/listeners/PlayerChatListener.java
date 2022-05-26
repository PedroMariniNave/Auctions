package com.zpedroo.voltzauctions.listeners;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.voltzauctions.VoltzAuctions;
import com.zpedroo.voltzauctions.managers.AuctionManager;
import com.zpedroo.voltzauctions.objects.Auction;
import com.zpedroo.voltzauctions.objects.Bid;
import com.zpedroo.voltzauctions.objects.PreAuction;
import com.zpedroo.voltzauctions.utils.config.Messages;
import com.zpedroo.voltzauctions.utils.config.NumberFormatter;
import com.zpedroo.voltzauctions.utils.menu.Menus;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class PlayerChatListener implements Listener {

    private static final Map<Player, PreAuction> playersSettingMinimumBid = new HashMap<>(2);
    private static final Map<Player, Auction> playersBidding = new HashMap<>(4);

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSettingMinimumBidChat(AsyncPlayerChatEvent event) {
        if (!playersSettingMinimumBid.containsKey(event.getPlayer())) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        PreAuction preAuction = playersSettingMinimumBid.remove(player);
        BigInteger minimumBid = NumberFormatter.getInstance().filter(event.getMessage());
        if (minimumBid.signum() < 0) {
            player.sendMessage(Messages.INVALID_AMOUNT);
            return;
        }

//        preAuction.setMinimumBid(minimumBid); // useless for now

        ItemStack auctionItem = preAuction.getItem();
        Currency currency = preAuction.getCurrency();
        long durationInMillis = preAuction.getDurationInMillis();

        if (player.getInventory().first(auctionItem) == -1) {
            player.sendMessage(Messages.ITEM_NOT_FOUND);
            return;
        }

        player.getInventory().removeItem(auctionItem);
        AuctionManager.getInstance().createAuctionAndCache(player, auctionItem, currency, minimumBid, durationInMillis + System.currentTimeMillis());
        VoltzAuctions.get().getServer().getScheduler().runTaskLater(VoltzAuctions.get(), () -> Menus.getInstance().openPlayerAuctionsMenu(player), 0L);

        Bukkit.broadcastMessage(StringUtils.replaceEach(Messages.NEW_AUCTION, new String[]{
                "{player}",
                "{min_bid}",
                "{item}"
        }, new String[]{
                player.getName(),
                NumberFormatter.getInstance().formatValueWithCurrency(minimumBid, preAuction.getCurrency()),
                auctionItem.hasItemMeta() ? auctionItem.getItemMeta().hasDisplayName() ? auctionItem.getItemMeta().getDisplayName()
                        : auctionItem.getType().toString() : auctionItem.getType().toString()
        }));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBiddingChat(AsyncPlayerChatEvent event) {
        if (!playersBidding.containsKey(event.getPlayer())) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        Auction auction = playersBidding.remove(player);
        BigInteger bidAmount = NumberFormatter.getInstance().filter(event.getMessage());
        BigInteger actualBidAmount = auction.getLastBid() == null ? auction.getMinimumBid() : auction.getLastBid().getBidAmount();
        Currency currency = auction.getCurrency();
        if (bidAmount.compareTo(actualBidAmount) <= 0) {
            player.sendMessage(StringUtils.replaceEach(Messages.VALUE_BELOW, new String[]{
                    "{actual_bid_amount}",
                    "{actual_bid_amount_currency}"
            }, new String[]{
                    NumberFormatter.getInstance().format(actualBidAmount),
                    NumberFormatter.getInstance().formatValueWithCurrency(actualBidAmount, currency)
            }));
            return;
        }

        BigInteger currencyAmount = CurrencyAPI.getCurrencyAmount(player, currency);
        if (currencyAmount.compareTo(bidAmount) < 0) {
            player.sendMessage(StringUtils.replaceEach(Messages.INSUFFICIENT_CURRENCY, new String[]{
                    "{has}",
                    "{need}"
            }, new String[]{
                    NumberFormatter.getInstance().format(currencyAmount),
                    NumberFormatter.getInstance().format(bidAmount)
            }));
            return;
        }

        CurrencyAPI.removeCurrencyAmount(player, currency, bidAmount);
        if (auction.getLastBid() != null) {
            OfflinePlayer lastBidAuthor = Bukkit.getOfflinePlayer(auction.getLastBid().getAuthorUniqueId());

            CurrencyAPI.addCurrencyAmount(lastBidAuthor, currency, actualBidAmount);
        }

        auction.setLastBid(new Bid(player.getUniqueId(), bidAmount, System.currentTimeMillis()));

        ItemStack auctionItem = auction.getItem();
        Bukkit.broadcastMessage(StringUtils.replaceEach(Messages.NEW_BID, new String[]{
                "{player}",
                "{bid_amount}",
                "{item}"
        }, new String[]{
                player.getName(),
                NumberFormatter.getInstance().formatValueWithCurrency(bidAmount, currency),
                auctionItem.hasItemMeta() ? auctionItem.getItemMeta().hasDisplayName() ? auctionItem.getItemMeta().getDisplayName()
                        : auctionItem.getType().toString() : auctionItem.getType().toString()
        }));
    }

    public static Map<Player, PreAuction> getPlayersSettingMinimumBid() {
        return playersSettingMinimumBid;
    }

    public static Map<Player, Auction> getPlayersBidding() {
        return playersBidding;
    }
}