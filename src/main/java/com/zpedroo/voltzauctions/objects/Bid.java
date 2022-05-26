package com.zpedroo.voltzauctions.objects;

import java.math.BigInteger;
import java.util.UUID;

public class Bid {

    private final UUID authorUniqueId;
    private final BigInteger bidAmount;
    private final long timestampInMillis;

    public Bid(UUID authorUniqueId, BigInteger bidAmount, long timestampInMillis) {
        this.authorUniqueId = authorUniqueId;
        this.bidAmount = bidAmount;
        this.timestampInMillis = timestampInMillis;
    }

    public UUID getAuthorUniqueId() {
        return authorUniqueId;
    }

    public BigInteger getBidAmount() {
        return bidAmount;
    }

    public long getTimestampInMillis() {
        return timestampInMillis;
    }
}