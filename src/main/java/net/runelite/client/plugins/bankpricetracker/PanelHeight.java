package net.runelite.client.plugins.bankpricetracker;

public enum PanelHeight
{
    SMALL(400),
    MEDIUM(800),
    LARGE(1200);

    private final int pixels;

    PanelHeight(int pixels)
    {
        this.pixels = pixels;
    }

    public int getPixels()
    {
        return pixels;
    }
}
