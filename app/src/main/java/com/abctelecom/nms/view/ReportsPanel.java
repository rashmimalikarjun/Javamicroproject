package com.abctelecom.nms.view;

import com.abctelecom.nms.model.Report;
import com.abctelecom.nms.service.AccessControl.Capability;
import com.abctelecom.nms.service.ReportService;
import com.abctelecom.nms.util.ErrorHelper;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

/** Screen 14: Reports Dashboard - generate CSV reports and list history. */
public class ReportsPanel extends BasePanel {
    private final ReportService svc = new ReportService();
    private final JComboBox<String> typeBox = new JComboBox<>(ReportService.TYPES.toArray(new String[0]));

    public ReportsPanel() {
        super("Reports Dashboard (CSV export)");
        JPanel bar = new JPanel();
        bar.add(new javax.swing.JLabel("Report type:"));
        bar.add(typeBox);
        JButton gen = new JButton("Generate CSV Report");
        JButton ref = new JButton("Refresh History");
        bar.add(gen); bar.add(ref);
        add(bar, BorderLayout.PAGE_START);
        setColumns("ID", "Type", "Title", "CSV Path", "Summary", "Generated At");
        boolean allowed = ctx.can(Capability.GENERATE_REPORTS);
        gen.setEnabled(allowed);
        if (!allowed) status("Your role may not generate reports; history is still visible.");
        gen.addActionListener(e -> {
            status("Generating " + typeBox.getSelectedItem() + "...");
            ctx.async(() -> svc.generate((String) typeBox.getSelectedItem(), ctx.currentUser()),
                    r -> { status("Report written to " + r.getCsvPath()); reload(); },
                    err -> ErrorHelper.show(this, err));
        });
        ref.addActionListener(e -> reload());
    }

    @Override public void onShow() { reload(); }

    private void reload() {
        status("Loading report history...");
        ctx.async(svc::list, rs -> {
            clearRows();
            for (Report r : rs)
                model.addRow(new Object[]{r.getReportId(), r.getReportType(), r.getTitle(),
                        r.getCsvPath(), r.getSummary(), String.valueOf(r.getGeneratedAt())});
            status(rs.size() + " report(s) in history. Files are under the configured data directory.");
        }, err -> ErrorHelper.show(this, err));
    }
}
