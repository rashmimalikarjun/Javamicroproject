package com.abctelecom.nms.view;

import com.abctelecom.nms.dao.AlertDao;
import com.abctelecom.nms.model.Alert;
import com.abctelecom.nms.model.BaseStation;
import com.abctelecom.nms.service.AlertService;
import com.abctelecom.nms.service.BaseStationService;
import com.abctelecom.nms.util.ErrorHelper;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

/** Screen 3: Main Dashboard - fleet summary, top-loaded cells and recent alerts. */
public class DashboardPanel extends BasePanel {
    private final BaseStationService bs = new BaseStationService();
    private final AlertService alerts = new AlertService();
    private final AlertDao alertDao = new AlertDao();

    public DashboardPanel() {
        super("Main Dashboard");
        JPanel bar = new JPanel();
        JButton refresh = new JButton("Refresh");
        JButton eval    = new JButton("Run Alert Evaluation");
        bar.add(refresh); bar.add(eval);
        add(bar, BorderLayout.PAGE_START);
        setColumns("Section", "Item", "Value", "Detail");
        refresh.addActionListener(e -> reload());
        eval.addActionListener(e -> {
            status("Evaluating thresholds...");
            ctx.async(() -> alerts.evaluateThresholds(), n -> {
                status(n + " new alert(s) raised.");
                reload();
            }, err -> ErrorHelper.show(this, err));
        });
    }

    @Override public void onShow() { reload(); }

    private void reload() {
        status("Loading dashboard...");
        ctx.async(() -> {
            List<Object[]> rows = new ArrayList<>();
            List<BaseStation> stations = bs.list();
            int totalCap = 0, totalLoad = 0, on = 0;
            for (BaseStation b : stations) {
                totalCap += b.getCapacityMbps();
                totalLoad += Math.min(b.getCurrentLoadMbps(), b.getCapacityMbps());
                if ("ON".equals(b.getPowerStatus())) on++;
            }
            double util = totalCap == 0 ? 0 : 100.0 * totalLoad / totalCap;
            rows.add(new Object[]{"FLEET", "Base stations", stations.size(), "powered ON: " + on});
            rows.add(new Object[]{"FLEET", "Aggregate utilization", String.format("%.1f%%", util),
                    totalLoad + " / " + totalCap + " Mbps"});
            stations.sort((a, b2) -> Double.compare(b2.utilizationPct(), a.utilizationPct()));
            int i = 0;
            for (BaseStation b : stations) {
                if (i++ >= 5) break;
                rows.add(new Object[]{"TOP LOADED CELLS", b.getStationId(),
                        String.format("%.1f%%", b.utilizationPct()),
                        b.getCellName() + " (" + b.getTechnology() + ")"});
            }
            for (Alert a : alertDao.findAll(6)) {
                rows.add(new Object[]{"RECENT ALERTS", a.getSeverity() + " " + a.getAlertType(),
                        a.getEntityId(), a.getMessage()});
            }
            return rows;
        }, rows -> {
            clearRows();
            for (Object[] r : rows) model.addRow(r);
            status("Dashboard refreshed. Use the navigation menu to open management screens.");
        }, err -> ErrorHelper.show(this, err));
    }
}
