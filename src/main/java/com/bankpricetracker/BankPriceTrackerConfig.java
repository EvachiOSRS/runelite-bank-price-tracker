package net.runelite.client.plugins.bankpricetracker;

import net.runelite.client.config.*;

@ConfigGroup("bankpricetracker")
public interface BankPriceTrackerConfig extends Config
{
    @ConfigItem(
            keyName = "timeframe",
            name = "Timeframe",
            description = "Select how far back to track prices"
    )
    default Timeframe timeframe()
    {
        return Timeframe.ONE_DAY;
    }

    @ConfigItem(
            keyName = "panelHeight",
            name = "Panel Height",
            description = "Adjust the height of the side panel"
    )
    default PanelHeight panelHeight()
    {
        return PanelHeight.MEDIUM;
    }
}

