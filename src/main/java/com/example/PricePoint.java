package net.runelite.client.plugins.bankpricetracker;

import lombok.Getter;

import java.time.Instant;

@Getter
public class PricePoint
{
    private final Instant timestamp;
    private final int price;

    public PricePoint(Instant timestamp, int price)
    {
        this.timestamp = timestamp;
        this.price = price;
    }

}
