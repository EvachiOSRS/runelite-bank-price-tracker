package net.runelite.client.plugins.bankpricetracker;

public class Row
{
    private final String name;
    private final int value;
    private final int delta; // absolute GP change

    public Row(String name, int value, int delta)
    {
        this.name = name;
        this.value = value;
        this.delta = delta;
    }

    public String getName()
    {
        return name;
    }

    public int getValue()
    {
        return value;
    }

    public int getDelta()
    {
        return delta;
    }
}



