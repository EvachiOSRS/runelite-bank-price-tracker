package net.runelite.client.plugins.bankpricetracker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.ItemContainer;
import net.runelite.api.InventoryID;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import javax.inject.Inject;
import javax.swing.*;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.*;

@PluginDescriptor(
        name = "Bank Price Tracker",
        description = "Tracks item price changes in the bank over time",
        tags = {"bank", "price", "ge", "tracker"}
)
public class BankPriceTrackerPlugin extends Plugin
{
    static final String CONFIG_GROUP = "bankpricetracker";
    private static final String HISTORY_KEY = "history";

    @Inject private Client client;
    @Inject private ClientToolbar clientToolbar;
    @Inject private BankPriceTrackerConfig config;
    @Inject private ItemManager itemManager;
    @Inject private ConfigManager configManager;
    @Inject private ClientThread clientThread;

    private NavigationButton navButton;
    private BankPriceTrackerPanel panel;
    private final Map<Integer, List<PricePoint>> priceHistory = new HashMap<>();
    private final Map<Integer, Integer> latestPriceCache = new HashMap<>();
    private final Gson gson = new Gson();

    @Provides
    BankPriceTrackerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BankPriceTrackerConfig.class);
    }

    @Override
    protected void startUp()
    {
        SwingUtilities.invokeLater(() -> {
            BufferedImage icon;
            try
            {
                icon = ImageUtil.loadImageResource(getClass(), "bank_icon.png");
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to load bank icon", e);
            }

            panel = new BankPriceTrackerPanel(config);
            navButton = NavigationButton.builder()
                    .tooltip("Bank Price Tracker")
                    .icon(icon) // ✅ guaranteed icon
                    .priority(6)
                    .panel(panel)
                    .build();
            clientToolbar.addNavigation(navButton);
        });

        loadHistory();

        // Optional: if bank already open at startup, populate once
        clientThread.invokeLater(() -> {
            ItemContainer bank = client.getItemContainer(InventoryID.BANK);
            if (bank != null)
            {
                List<Row> rows = RowBuilder.buildRows(
                        bank.getItems(), latestPriceCache, priceHistory, itemManager, config.timeframe());
                SwingUtilities.invokeLater(() -> panel.refresh(rows));
            }
        });
    }

    @Override
    protected void shutDown()
    {
        if (navButton != null)
        {
            clientToolbar.removeNavigation(navButton);
            navButton = null;
        }
        saveHistory();
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {
        // Bank UI opened
        if (event.getGroupId() == InterfaceID.BANK)
        {
            clientThread.invokeLater(() -> {
                ItemContainer bank = client.getItemContainer(InventoryID.BANK);
                if (bank != null)
                {
                    List<Row> rows = RowBuilder.buildRows(
                            bank.getItems(), latestPriceCache, priceHistory, itemManager, config.timeframe());
                    SwingUtilities.invokeLater(() -> panel.refresh(rows));
                }
            });
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged e)
    {
        // Compare against the inventory id, not a widget id
        if (e.getContainerId() == InventoryID.BANK.getId())
        {
            clientThread.invokeLater(() -> updatePanel(e.getItemContainer()));
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!event.getGroup().equals(CONFIG_GROUP) || panel == null)
        {
            return;
        }

        // Height update (Swing)
        SwingUtilities.invokeLater(() -> panel.updatePanelHeight(config.panelHeight().getPixels()));

        // Rebuild rows for current bank (Client thread)
        clientThread.invokeLater(() -> {
            ItemContainer bank = client.getItemContainer(InventoryID.BANK);
            net.runelite.api.Item[] items = bank != null ? bank.getItems() : new net.runelite.api.Item[0];

            List<Row> rows = RowBuilder.buildRows(
                    items, latestPriceCache, priceHistory, itemManager, config.timeframe());

            SwingUtilities.invokeLater(() -> panel.refresh(rows));
        });
    }

    private void updatePanel(ItemContainer bank)
    {
        Arrays.stream(bank.getItems())
                .filter(item -> item != null && item.getId() > 0)
                .forEach(item -> {
                    int id = item.getId();
                    int price = itemManager.getItemPrice(id);
                    if (price > 0)
                    {
                        latestPriceCache.put(id, price);
                        priceHistory.computeIfAbsent(id, k -> new ArrayList<>())
                                .add(new PricePoint(Instant.now(), price));
                    }
                });

        pruneHistory();
        saveHistory();

        List<Row> rows = RowBuilder.buildRows(
                bank.getItems(), latestPriceCache, priceHistory, itemManager, config.timeframe());

        SwingUtilities.invokeLater(() -> panel.refresh(rows));
    }

    private void pruneHistory()
    {
        Instant cutoff = Instant.now().minus(Timeframe.ONE_MONTH.getDuration());
        for (List<PricePoint> history : priceHistory.values())
        {
            history.removeIf(p -> p.getTimestamp().isBefore(cutoff));
        }
    }

    private void saveHistory()
    {
        try
        {
            String json = gson.toJson(priceHistory);
            configManager.setConfiguration(CONFIG_GROUP, HISTORY_KEY, json);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void loadHistory()
    {
        String json = configManager.getConfiguration(CONFIG_GROUP, HISTORY_KEY);
        if (json == null || json.isEmpty())
        {
            return;
        }
        try
        {
            Type type = new TypeToken<Map<Integer, List<PricePoint>>>(){}.getType();
            Map<Integer, List<PricePoint>> loaded = gson.fromJson(json, type);
            if (loaded != null)
            {
                priceHistory.clear();
                priceHistory.putAll(loaded);
            }
            pruneHistory();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}





