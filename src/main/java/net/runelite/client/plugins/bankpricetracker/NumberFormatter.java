package net.runelite.client.plugins.bankpricetracker;

public class NumberFormatter
{
    public static String formatValue(int value)
    {
        if (value >= 1_000_000_000)
        {
            return String.format("%.1fB", value / 1_000_000_000.0);
        }
        else if (value >= 1_000_000)
        {
            return String.format("%.1fM", value / 1_000_000.0);
        }
        else if (value >= 1_000)
        {
            return String.format("%.1fK", value / 1_000.0);
        }
        else
        {
            return String.valueOf(value);
        }
    }
}
