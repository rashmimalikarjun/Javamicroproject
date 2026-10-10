package com.abctelecom.nms.view;

import com.abctelecom.nms.model.NetworkKpi;
import com.abctelecom.nms.service.KpiService;
import com.abctelecom.nms.util.ErrorHelper;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

/** Screen 11: KPI Dashboard - computed from stored data; simulated values flagged. */
public class KpiPanel extends BasePanel {
    private final KpiService svc = new KpiService();

    public KpiPanel() {
        super("KPI Dashboard");
        JButton refresh = new JButton("Recompute Now");
        JButton store   = new JButton("Save Snapshot");
        JPanel bar = new JPanel();
        bar.add(refresh); bar.add(store);
        add(bar, BorderLayout.PAGE_START);
        setColumns("KPI", "Value", "Unit", "Simulated?", "Snapshot At");
        refresh.addActionListener(e -> reload());
        store.addActionListener(e -> {
            status("Storing snapshot...");
            ctx.async(svc::snapshotAndStore, rows -> { status("Snapshot stored (" + rows.size() + " KPIs)."); reload(); },
                    err -> ErrorHelper.show(this, err));
        });
    }

    @Override public void onShow() { reload(); }

    private void reload() {
        status("Computing KPIs from stored data...");
        ctx.async(svc::computeCurrent, this::fill, err -> ErrorHelper.show(this, err));
    }

    private void fill(List<NetworkKpi> kpis) {
        clearRows();
        for (NetworkKpi k : kpis)
            model.addRow(new Object[]{k.getKpiName(), String.format("%.2f", k.getKpiValue()),
                    k.getUnit(), k.isSimulated() ? "SIMULATED" : "COMPUTED",
                    String.valueOf(k.getSnapshotAt())});
        status(kpis.size() + " KPIs. COMPUTED = derived from DB tables; SIMULATED = modelled estimate.");
    }
}
