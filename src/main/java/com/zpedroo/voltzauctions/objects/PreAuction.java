package com.zpedroo.voltzauctions.objects;

import com.zpedroo.multieconomy.objects.general.Currency;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;

public class PreAuction {

    private final ItemStack item;
    private Currency currency;
    private BigInteger minimumBid;
    private long durationInMillis;

    public PreAuction(ItemStack item) {
        this.item = item;
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

    public long getDurationInMillis() {
        return durationInMillis;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setMinimumBid(BigInteger minimumBid) {
        this.minimumBid = minimumBid;
    }

    public void setDurationInMillis(long durationInMillis) {
        this.durationInMillis = durationInMillis;
    }
}