package com.zpedroo.voltzauctions.objects;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.objects.general.Currency;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.UUID;

public class Auction {

    private final long id;
    private final UUID sellerUniqueId;
    private final ItemStack item;
    private Currency currency;
    private BigInteger minimumBid;
    private Bid lastBid;
    private final long expirationDateInMillis;
    private boolean update;

    public Auction(long id, UUID sellerUniqueId, ItemStack item, Currency currency, BigInteger minimumBid, Bid lastBid, long expirationDateInMillis) {
        this.id = id;
        this.sellerUniqueId = sellerUniqueId;
        this.item = item;
        this.currency = currency;
        this.minimumBid = minimumBid;
        this.lastBid = lastBid;
        this.expirationDateInMillis = expirationDateInMillis;
        this.update = false;
    }

    public long getId() {
        return id;
    }

    public UUID getSellerUniqueId() {
        return sellerUniqueId;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigInteger getMinimumBid() {
        return minimumBid;
    }

    public Bid getLastBid() {
        return lastBid;
    }

    public long getExpirationDateInMillis() {
        return expirationDateInMillis;
    }

    public boolean isQueueUpdate() {
        return update;
    }

    public void setLastBid(Bid lastBid) {
        this.lastBid = lastBid;
        this.update = true;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setMinimumBid(BigInteger minimumBid) {
        this.minimumBid = minimumBid;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }
}