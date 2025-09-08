package net.runelite.client.plugins.bankpricetracker;

import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class BankPriceTrackerPanel extends PluginPanel
{
    private final RowsTableModel tableModel = new RowsTableModel();

    public BankPriceTrackerPanel(BankPriceTrackerConfig config)
    {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, config.panelHeight().getPixels()));

        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(20);

        // ✅ Add sorter
        TableRowSorter<RowsTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // ✅ Default: sort Change column descending (most profit at top)
        sorter.toggleSortOrder(2); // column index for "Change"
        sorter.toggleSortOrder(2); // toggle twice to ensure descending

        // Custom renderer for Value column
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column)
            {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Integer)
                {
                    label.setText(NumberFormatter.formatValue((int) value));
                }
                return label;
            }
        });

        // Custom renderer for Change column
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column)
            {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (value instanceof Integer)
                {
                    int delta = (int) value;
                    label.setText(NumberFormatter.formatValue(delta));

                    if (delta > 0) label.setForeground(Color.GREEN);
                    else if (delta < 0) label.setForeground(Color.RED);
                    else label.setForeground(Color.WHITE);
                }
                else
                {
                    label.setText("-");
                    label.setForeground(Color.WHITE);
                }
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refresh(List<Row> rows)
    {
        tableModel.setRows(rows);
    }

    public void updatePanelHeight(int height)
    {
        setPreferredSize(new Dimension(300, height));
        revalidate();
    }
}









