package net.runelite.client.plugins.bankpricetracker;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class RowsTableModel extends AbstractTableModel
{
    private final String[] columns = {"Item", "Value", "Change"};
    private List<Row> rows = new ArrayList<>();

    public void setRows(List<Row> rows)
    {
        this.rows = rows;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount()
    {
        return rows.size();
    }

    @Override
    public int getColumnCount()
    {
        return columns.length;
    }

    @Override
    public String getColumnName(int col)
    {
        return columns[col];
    }

    @Override
    public Object getValueAt(int rowIndex, int colIndex)
    {
        Row row = rows.get(rowIndex);
        switch (colIndex)
        {
            case 0: return row.getName();
            case 1: return row.getValue();
            case 2: return row.getDelta();
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        if (columnIndex == 1 || columnIndex == 2)
        {
            return Integer.class;
        }
        return String.class;
    }
}




