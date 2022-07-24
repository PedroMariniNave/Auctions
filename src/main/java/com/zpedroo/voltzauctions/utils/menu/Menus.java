package com.zpedroo.voltzauctions.utils.menu;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.voltzauctions.listeners.PlayerChatListener;
import com.zpedroo.voltzauctions.managers.AuctionManager;
import com.zpedroo.voltzauctions.managers.DataManager;
import com.zpedroo.voltzauctions.objects.Auction;
import com.zpedroo.voltzauctions.objects.PlayerData;
import com.zpedroo.voltzauctions.objects.PreAuction;
import com.zpedroo.voltzauctions.utils.FileUtils;
import com.zpedroo.voltzauctions.utils.builder.InventoryBuilder;
import com.zpedroo.voltzauctions.utils.builder.InventoryUtils;
import com.zpedroo.voltzauctions.utils.builder.ItemBuilder;
import com.zpedroo.voltzauctions.utils.config.Messages;
import com.zpedroo.voltzauctions.utils.config.NumberFormatter;
import com.zpedroo.voltzauctions.utils.config.TimeFormatter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Menus extends InventoryUtils {

    private static Menus instance;
    public static Menus getInstance() { return instance; }

    private final ItemStack nextPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Next-Page").build();
    private final ItemStack previousPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Previous-Page").build();

    public Menus() {
        instance = this;
    }

    public void openMainMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.MAIN;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        for (String items : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + items, new String[]{
                    "{player}"
            }, new String[]{
                    player.getName()
            }).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + items + ".slot");
            String action = FileUtils.get().getString(file, "Inventory.items." + items + ".action");

            inventory.addItem(item, slot, () -> {
                switch (action.toUpperCase()) {
                    case "NEW_AUCTION":
                        if (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR)) {
                            player.closeInventory();
                            player.sendMessage(Messages.NEED_ITEM_IN_HAND);
                            return;
                        }

                        ItemStack itemInHand = player.getItemInHand().clone();
                        openItemConfirmationMenu(player, itemInHand);
                        break;
                    case "ACTIVE_AUCTIONS":
                        openActiveAuctionsMenu(player);
                        break;
                    case "PLAYER_AUCTIONS":
                        openPlayerAuctionsMenu(player);
                        break;
                    case "PLAYER_ITEMS":
                        openPlayerItemsMenu(player);
                        break;
                }
            }, ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }

    public void openActiveAuctionsMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.ACTIVE_AUCTIONS;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        int nextPageSlot = FileUtils.get().getInt(file, "Inventory.next-page-slot");
        int previousPageSlot = FileUtils.get().getInt(file, "Inventory.previous-page-slot");

        InventoryBuilder inventory = new InventoryBuilder(title, size, previousPageItem, previousPageSlot, nextPageItem, nextPageSlot);
        List<Auction> activeAuctions = DataManager.getInstance().getCache().getActiveAuctions();

        if (!activeAuctions.isEmpty()) {
            List<String> itemLore = FileUtils.get().getStringList(file, "Item-Lore");
            String[] slots = FileUtils.get().getString(file, "Inventory.slots").replace(" ", "").split(",");
            int i = -1;
            for (Auction auction : activeAuctions) {
                if (StringUtils.equals(auction.getSellerUniqueId().toString(), player.getUniqueId().toString())) continue;
                if (++i >= slots.length) i = 0;

                ItemStack item = auction.getItem();
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>(4);

                    for (String toAdd : itemLore) {
                        lore.add(StringUtils.replaceEach(ChatColor.translateAlternateColorCodes('&', toAdd), new String[]{
                                "{seller}",
                                "{last_bid_author}",
                                "{last_bid_amount}",
                                "{last_bid_when}",
                                "{expiration}"
                        }, new String[]{
                                Bukkit.getOfflinePlayer(auction.getSellerUniqueId()).getName(),
                                auction.getLastBid() == null ? "-/-" : Bukkit.getOfflinePlayer(auction.getLastBid().getAuthorUniqueId()).getName(),
                                auction.getLastBid() == null ? NumberFormatter.getInstance().formatValueWithCurrency(auction.getMinimumBid(), auction.getCurrency())
                                        : NumberFormatter.getInstance().formatValueWithCurrency(auction.getLastBid().getBidAmount(), auction.getCurrency()),
                                auction.getLastBid() == null ? "-/-" : TimeFormatter.millisToFormattedDate(auction.getLastBid().getTimestampInMillis()),
                                TimeFormatter.millisToFormattedTime(auction.getExpirationDateInMillis() - System.currentTimeMillis())
                        }));
                    }

                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }

                int slot = Integer.parseInt(slots[i]);

                inventory.addItem(item, slot, () -> {
                    if (!AuctionManager.getInstance().isActiveAuction(auction)) {
                        player.sendMessage(Messages.INVALID_AUCTION);
                        return;
                    }

                    inventory.close(player);

                    PlayerChatListener.getPlayersBidding().put(player, auction);

                    for (int x = 0; x < 25; ++x) {
                        player.sendMessage("");
                    }

                    ItemStack auctionItem = auction.getItem();
                    Currency currency = auction.getCurrency();
                    long expirationInMillis = auction.getExpirationDateInMillis();

                    for (String msg : Messages.CHOOSE_BID) {
                        player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                                "{item}",
                                "{currency}",
                                "{actual_bid}",
                                "{expiration}"
                        }, new String[]{
                                auctionItem.hasItemMeta() ? auctionItem.getItemMeta().hasDisplayName() ? auctionItem.getItemMeta().getDisplayName() :
                                        auctionItem.getType().toString() : auctionItem.getType().toString(),
                                currency.getCurrencyDisplay(),
                                auction.getLastBid() == null ? NumberFormatter.getInstance().formatValueWithCurrency(auction.getMinimumBid(), auction.getCurrency()) :
                                        NumberFormatter.getInstance().formatValueWithCurrency(auction.getLastBid().getBidAmount(), auction.getCurrency()),
                                TimeFormatter.millisToFormattedTime(expirationInMillis - System.currentTimeMillis())
                        }));
                    }
                }, ActionType.ALL_CLICKS);
            }
        } else {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Empty").build();
            int slot = FileUtils.get().getInt(file, "Empty.slot");

            inventory.addItem(item, slot);
        }

        inventory.open(player);
    }

    public void openPlayerAuctionsMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.PLAYER_AUCTIONS;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        int nextPageSlot = FileUtils.get().getInt(file, "Inventory.next-page-slot");
        int previousPageSlot = FileUtils.get().getInt(file, "Inventory.previous-page-slot");

        InventoryBuilder inventory = new InventoryBuilder(title, size, previousPageItem, previousPageSlot, nextPageItem, nextPageSlot);
        List<Auction> playerAuctions = DataManager.getInstance().getCache().getPlayerActiveAuctions(player);

        if (playerAuctions != null && !playerAuctions.isEmpty()) {
            List<String> itemLore = FileUtils.get().getStringList(file, "Item-Lore");
            String[] slots = FileUtils.get().getString(file, "Inventory.slots").replace(" ", "").split(",");
            int i = -1;
            for (Auction auction : playerAuctions) {
                if (++i >= slots.length) i = 0;

                ItemStack item = auction.getItem();
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>(4);

                    for (String toAdd : itemLore) {
                        lore.add(StringUtils.replaceEach(ChatColor.translateAlternateColorCodes('&', toAdd), new String[]{
                                "{last_bid_author}",
                                "{last_bid_amount}",
                                "{last_bid_when}",
                                "{expiration}"
                        }, new String[]{
                                auction.getLastBid() == null ? "-/-" : Bukkit.getOfflinePlayer(auction.getLastBid().getAuthorUniqueId()).getName(),
                                auction.getLastBid() == null ? NumberFormatter.getInstance().formatValueWithCurrency(auction.getMinimumBid(), auction.getCurrency())
                                        : NumberFormatter.getInstance().formatValueWithCurrency(auction.getLastBid().getBidAmount(), auction.getCurrency()),
                                auction.getLastBid() == null ? "-/-" : TimeFormatter.millisToFormattedDate(auction.getLastBid().getTimestampInMillis()),
                                TimeFormatter.millisToFormattedTime(auction.getExpirationDateInMillis() - System.currentTimeMillis())
                        }));
                    }

                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }

                int slot = Integer.parseInt(slots[i]);

                inventory.addItem(item, slot, () -> {
                    if (!AuctionManager.getInstance().isActiveAuction(auction)) {
                        player.sendMessage(Messages.INVALID_AUCTION);
                        return;
                    }

                    if (AuctionManager.getInstance().isActiveAuction(auction) && auction.getLastBid() != null) {
                        player.sendMessage(Messages.HAS_BID);
                        return;
                    }

                    AuctionManager.getInstance().cancelAuctionAndRefoundItem(player, auction);
                    openPlayerAuctionsMenu(player);
                }, ActionType.ALL_CLICKS);
            }
        } else {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Empty").build();
            int slot = FileUtils.get().getInt(file, "Empty.slot");

            inventory.addItem(item, slot);
        }

        inventory.open(player);
    }

    public void openPlayerItemsMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.PLAYER_ITEMS;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        int nextPageSlot = FileUtils.get().getInt(file, "Inventory.next-page-slot");
        int previousPageSlot = FileUtils.get().getInt(file, "Inventory.previous-page-slot");

        InventoryBuilder inventory = new InventoryBuilder(title, size, previousPageItem, previousPageSlot, nextPageItem, nextPageSlot);

        PlayerData data = DataManager.getInstance().getPlayerData(player);
        List<ItemStack> playerItems = data.getItemsToCollect();

        if (!playerItems.isEmpty()) {
            List<String> itemLore = FileUtils.get().getStringList(file, "Item-Lore");
            String[] slots = FileUtils.get().getString(file, "Inventory.slots").replace(" ", "").split(",");
            int i = -1;
            for (ItemStack item : playerItems) {
                if (++i >= slots.length) i = 0;

                ItemStack itemClone = item.clone();
                ItemMeta meta = itemClone.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>(4);

                    for (String toAdd : itemLore) {
                        lore.add(ChatColor.translateAlternateColorCodes('&', toAdd));
                    }

                    meta.setLore(lore);
                    itemClone.setItemMeta(meta);
                }

                int slot = Integer.parseInt(slots[i]);

                inventory.addItem(itemClone, slot, () -> {
                    data.removeItemToCollect(item);

                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(item);
                    } else {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    }

                    openPlayerItemsMenu(player);
                }, ActionType.ALL_CLICKS);
            }
        } else {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Empty").build();
            int slot = FileUtils.get().getInt(file, "Empty.slot");

            inventory.addItem(item, slot);
        }

        inventory.open(player);
    }

    public void openItemConfirmationMenu(Player player, ItemStack itemToConfirm) {
        FileUtils.Files file = FileUtils.Files.ITEM_CONFIRMATION;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        for (String items : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + items).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + items + ".slot");
            String action = FileUtils.get().getString(file, "Inventory.items." + items + ".action");

            inventory.addItem(item, slot, () -> {
                switch (action.toUpperCase()) {
                    case "CONFIRM":
                        openSelectDurationMenu(player, new PreAuction(itemToConfirm));
                        break;
                    case "CANCEL":
                        player.closeInventory();
                        break;
                }
            }, ActionType.ALL_CLICKS);
        }

        int itemToConfirmSlot = FileUtils.get().getInt(file, "Inventory.item-slot");
        inventory.addItem(itemToConfirm, itemToConfirmSlot);

        inventory.open(player);
    }

    public void openSelectDurationMenu(Player player, PreAuction preAuction) {
        FileUtils.Files file = FileUtils.Files.SELECT_DURATION;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        for (String str : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + str + ".slot");
            String action = FileUtils.get().getString(file, "Inventory.items." + str + ".action");

            inventory.addItem(item, slot, () -> {
                if (StringUtils.contains(action, "TIME:")) {
                    String[] actionSplit = action.split(":");

                    int durationInMinutes = Integer.parseInt(actionSplit[1]);
                    if (durationInMinutes <= 0) return;

                    preAuction.setDurationInMillis(TimeUnit.MINUTES.toMillis(durationInMinutes));
                    openSelectCurrencyMenu(player, preAuction);
                }
            }, ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }

    public void openSelectCurrencyMenu(Player player, PreAuction preAuction) {
        FileUtils.Files file = FileUtils.Files.SELECT_CURRENCY;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        ItemStack auctionItem = preAuction.getItem();
        long durationInMillis = preAuction.getDurationInMillis();

        for (String str : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + str + ".slot");

            String currencyName = FileUtils.get().getString(file, "Inventory.items." + str + ".currency");
            Currency currency = CurrencyAPI.getCurrency(currencyName);
            inventory.addItem(item, slot, () -> {
                if (currency == null) return;

                preAuction.setCurrency(currency);
                inventory.close(player);

                PlayerChatListener.getPlayersSettingMinimumBid().put(player, preAuction);

                for (int x = 0; x < 25; ++x) {
                    player.sendMessage("");
                }

                for (String msg : Messages.CHOOSE_MINIMUM_BID) {
                    player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                            "{item}",
                            "{currency}",
                            "{expiration}"
                    }, new String[]{
                            auctionItem.hasItemMeta() ? auctionItem.getItemMeta().hasDisplayName() ? auctionItem.getItemMeta().getDisplayName() :
                                    auctionItem.getType().toString() : auctionItem.getType().toString(),
                            currency.getCurrencyDisplay(),
                            TimeFormatter.millisToFormattedTime(durationInMillis)
                    }));
                }
            }, ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }
}