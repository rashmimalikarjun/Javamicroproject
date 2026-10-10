package com.abctelecom.nms.view;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Base class for all content screens. Provides a standard header, a status
 * line and helpers for building read-only JTables. Subclasses are added to
 * the MainFrame CardLayout; {@link #onShow()} is called on every navigation.
 */
public abstract class BasePanel extends JPanel {
    protected final AppContext ctx = AppContext.get();
    protected final JLabel statusLabel = new JLabel(" ");
    protected final DefaultTableModel model =
            new DefaultTableModel(0, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    protected final JTable table = new JTable(model);

    protected BasePanel(String title) {
        super(new java.awt.BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        add(new JLabel("<html><b style='font-size:15px'>" + title + "</b></html>"),
                java.awt.BorderLayout.NORTH);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), java.awt.BorderLayout.CENTER);
        statusLabel.setForeground(new java.awt.Color(70, 70, 70));
        add(statusLabel, java.awt.BorderLayout.SOUTH);
    }

    /** Called when the card becomes visible; load data asynchronously. */
    public void onShow() {}

    protected void status(String msg) { statusLabel.setText(" " + msg); }

    protected void setColumns(String... cols) {
        model.setColumnIdentifiers(cols);
        model.setRowCount(0);
    }

    protected void clearRows() { model.setRowCount(0); }
}
