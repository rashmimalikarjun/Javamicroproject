package com.abctelecom.nms.view;

import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.util.ErrorHelper;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Reusable master/detail CRUD screen: table + side form + toolbar buttons.
 * Subclasses declare text fields / combo boxes and provide load / save /
 * delete callbacks that run on a background thread via {@link AppContext#async}
 * so the EDT is never blocked by database work.
 */
public abstract class CrudPanel extends BasePanel {
    protected final Map<String, JTextField> fields = new LinkedHashMap<>();
    protected final Map<String, JComboBox<String>> combos = new LinkedHashMap<>();
    private final JPanel form = new JPanel();
    private JButton btnNew, btnSave, btnDelete;
    private boolean editingExisting = false;

    protected CrudPanel(String title) {
        super(title);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createTitledBorder("Details"));
        form.setMaximumSize(new java.awt.Dimension(280, 10000));
        form.setPreferredSize(new java.awt.Dimension(280, 400));
        add(form, BorderLayout.EAST);
        buildToolbar();
        onTableClick(row -> {});
    }

    /** Add a labelled text field to the side form (fixed width for alignment). */
    protected void addField(String label) {
        JTextField tf = new JTextField(12);
        tf.setMaximumSize(new java.awt.Dimension(250, 28));
        tf.setPreferredSize(new java.awt.Dimension(250, 28));
        fields.put(label, tf);
        addRow(label, tf);
    }

    protected void addCombo(String label, List<String> options) {
        JComboBox<String> cb = new JComboBox<>(options.toArray(new String[0]));
        cb.setMaximumSize(new java.awt.Dimension(250, 28));
        cb.setPreferredSize(new java.awt.Dimension(250, 28));
        combos.put(label, cb);
        addRow(label, cb);
    }

    private void addRow(String label, Component c) {
        JPanel row = new JPanel(new BorderLayout(6, 2));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new java.awt.Dimension(260, 48));
        JLabel l = new JLabel(label);
        l.setPreferredSize(new java.awt.Dimension(110, 20));
        row.add(l, BorderLayout.NORTH);
        row.add(c, BorderLayout.CENTER);
        form.add(row);
        form.add(Box.createVerticalStrut(4));
    }

    protected String text(String label) { return fields.get(label).getText().trim(); }
    protected String combo(String label) { return (String) combos.get(label).getSelectedItem(); }

    private void buildToolbar() {
        JPanel bar = new JPanel();
        JButton btnRefresh = new JButton("Refresh");
        btnNew     = new JButton("New");
        btnSave    = new JButton("Save");
        btnDelete  = new JButton("Delete");
        btnRefresh.addActionListener(e -> reload());
        btnNew.addActionListener(e -> startNew());
        btnSave.addActionListener(e -> save());
        btnDelete.addActionListener(e -> delete());
        bar.add(btnRefresh); bar.add(btnNew); bar.add(btnSave); bar.add(btnDelete);
        add(bar, BorderLayout.PAGE_START);
    }

    /** Load rows on a background thread; the ready consumer is invoked there. */
    protected abstract List<Object[]> fetchRows() throws Exception;

    /** Persist create/update from current form values (background thread). */
    protected abstract void doSave(Map<String, String> values, boolean isNew) throws QitromException;

    /** Delete the selected row (background thread). */
    protected abstract void doDelete(Object[] selectedRow) throws QitromException;

    /** Populate the form when a table row is clicked (EDT). */
    protected abstract void fillForm(Object[] row);

    protected void reload() {
        status("Loading...");
        ctx.async(this::fetchRows, rows -> {
            clearRows();
            for (Object[] r : rows) model.addRow(r);
            status(rows.size() + " record(s) loaded.");
        }, err -> ErrorHelper.show(this, err));
    }

    void startNew() {
        editingExisting = false;
        fields.values().forEach(tf -> tf.setText(""));
        combos.values().forEach(cb -> { if (cb.getItemCount() > 0) cb.setSelectedIndex(0); });
        status("New record - fill the form and press Save");
    }

    private void save() {
        Map<String, String> values = new LinkedHashMap<>();
        fields.forEach((k, v) -> values.put(k, v.getText().trim()));
        combos.forEach((k, v) -> values.put(k, (String) v.getSelectedItem()));
        final boolean isNew = !editingExisting;
        status("Saving...");
        ctx.async(() -> { doSave(values, isNew); return Boolean.TRUE; },
                ok -> { status(isNew ? "Created." : "Updated."); editingExisting = true; reload(); },
                err -> { ErrorHelper.show(this, err); status("Save failed."); });
    }

    private void delete() {
        int view = table.getSelectedRow();
        if (view < 0) { JOptionPane.showMessageDialog(this, "Select a row to delete first."); return; }
        Object[] row = snapshotRow(table.convertRowIndexToModel(view));
        if (JOptionPane.showConfirmDialog(this, "Delete this record permanently?",
                "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        status("Deleting...");
        ctx.async(() -> { doDelete(row); return Boolean.TRUE; },
                ok -> { status("Deleted."); startNew(); reload(); },
                err -> { ErrorHelper.show(this, err); status("Delete failed."); });
    }

    protected Object[] snapshotRow(int modelRow) {
        Object[] out = new Object[model.getColumnCount()];
        for (int i = 0; i < out.length; i++) out[i] = model.getValueAt(modelRow, i);
        return out;
    }

    /** Enable/disable action set based on RBAC capability (least privilege UI). */
    protected void setEditable(boolean canEdit) {
        btnNew.setEnabled(canEdit);
        btnSave.setEnabled(canEdit);
        btnDelete.setEnabled(canEdit);
    }

    protected void onTableClick(Consumer<Object[]> handler) {
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int v = table.rowAtPoint(e.getPoint());
                if (v >= 0) {
                    Object[] row = snapshotRow(table.convertRowIndexToModel(v));
                    fillForm(row);
                    editingExisting = true;
                    handler.accept(row);
                }
            }
        });
    }

    /** Helper: parse an int safely for use inside doSave with friendly errors. */
    protected static int parseInt(String s, String field) throws QitromException {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) {
            throw new com.abctelecom.nms.exception.ValidationException(field + " must be a whole number: '" + s + "'");
        }
    }

    protected static double parseDouble(String s, String field) throws QitromException {
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) {
            throw new com.abctelecom.nms.exception.ValidationException(field + " must be a number: '" + s + "'");
        }
    }
}
