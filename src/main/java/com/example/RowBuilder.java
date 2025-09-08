package net.runelite.client.plugins.bankpricetracker;

import net.runelite.api.Item;
import net.runelite.client.game.ItemManager;

import java.util.*;
import java.util.stream.Collectors;

public class RowBuilder
{
    public static List<Row> buildRows(
            Item[] items,
            Map<Integer, Integer> latestPriceCache,
            Map<Integer, List<PricePoint>> priceHistory,
            ItemManager itemManager,
            Timeframe timeframe
    )
    {
        if (items == null || items.length == 0)
        {
            return Collections.emptyList();
        }

        List<Row> rows = new ArrayList<>();

        for (Item item : items)
        {
            if (item == null || item.getId() <= 0)
            {
                continue;
            }

            int id = item.getId();
            int qty = item.getQuantity();

            // ✅ Get name
            String name = itemManager.getItemComposition(id).getName();

            // ✅ Get latest price (from cache or live)
            int latestPrice = latestPriceCache.getOrDefault(id, itemManager.getItemPrice(id));
            if (latestPrice <= 0)
            {
                continue; // skip untradeable or unpriced items
            }

            int totalValue = latestPrice * qty;

            // ✅ Compute absolute GP change
            int delta = 0;
            List<PricePoint> history = priceHistory.getOrDefault(id, Collections.emptyList());
            if (!history.isEmpty())
            {
                long cutoff = System.currentTimeMillis() - timeframe.getDuration().toMillis();
                Optional<PricePoint> oldest = history.stream()
                        .filter(p -> p.getTimestamp().toEpochMilli() <= cutoff)
                        .findFirst();

                if (oldest.isPresent())
                {
                    int oldPrice = oldest.get().getPrice();
                    delta = (latestPrice - oldPrice) * qty;
                }
            }

            rows.add(new Row(name, totalValue, delta));
        }

        // ✅ Sort rows by profitability (delta) descending
        return rows.stream()
                .sorted(Comparator.comparingInt(Row::getDelta).reversed())
                .collect(Collectors.toList());
    }
}







