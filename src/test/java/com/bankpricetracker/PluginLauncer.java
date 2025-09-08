package com.bankpricetracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PluginLauncer
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(net.runelite.client.plugins.bankpricetracker.BankPriceTrackerPlugin.class);
		RuneLite.main(args);
	}
}