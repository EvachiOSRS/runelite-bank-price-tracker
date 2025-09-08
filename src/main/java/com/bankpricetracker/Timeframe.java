package net.runelite.client.plugins.bankpricetracker;

import lombok.Getter;

import java.time.Duration;

@Getter
public enum Timeframe
{
    THIRTY_MINUTES(Duration.ofMinutes(30)),
    ONE_DAY(Duration.ofDays(1)),
    ONE_WEEK(Duration.ofDays(7)),
    TWO_WEEKS(Duration.ofDays(14)),
    ONE_MONTH(Duration.ofDays(30));

    private final Duration duration;

    Timeframe(Duration duration)
    {
        this.duration = duration;
    }

}
