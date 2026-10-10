package com.abctelecom.nms.view;

import com.abctelecom.nms.config.AppConfig;
import com.abctelecom.nms.dao.AuditDao;
import com.abctelecom.nms.util.ErrorHelper;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/** Screen 17: Database Backup - portable CSV snapshot of every table + audit trail. */
public class BackupPanel extends BasePanel {
    private final AuditDao audit = new AuditDao();

    public BackupPanel() {
        super("Database Backup (CSV Snapshot)");
        JPanel bar = new JPanel();
        JButton run = new JButton("Create CSV Backup Now");
        JButton auditBtn = new JButton("Show Recent Audit Entries");
        bar.add(run); bar.add(auditBtn);
        add(bar, BorderLayout.PAGE_START);
        setColumns("Table", "Rows Exported", "File");
        run.addActionListener(e -> {
            status("Writing backup...");
            ctx.async(this::doBackup, res -> {
                clearRows();
                for (String line : res) model.addRow(line.split("\\|", -1));
                status("Backup complete (" + res.size() + " tables). Files under " + AppConfig.getDataDir() + "/backup_*");
            }, err -> ErrorHelper.show(this, err));
        });
        auditBtn.addActionListener(e -> {
            status("Loading audit log...");
            ctx.async(() -> audit.recent(50), rows -> {
                clearRows();
                for (Map<String, Object> m : rows)
                    model.addRow(new Object[]{"AUDIT", String.valueOf(m.get("action")),
                            m.get("created_at") + " user=" + m.get("user_id") + " entity=" + m.get("entity_id")});
                status(rows.size() + " audit entries shown.");
            }, err -> ErrorHelper.show(this, err));
        });
    }

    /** Uses the same DAOs as the rest of the app so no SQL knowledge is duplicated in the GUI. */
    private List<String> doBackup() throws Exception {
        String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        Path dir = Path.of(AppConfig.getDataDir(), "backup_" + ts);
        Files.createDirectories(dir);
        List<String> out = new java.util.ArrayList<>();
        out.addAll(dump("base_stations", dir, () -> rows(com.abctelecom.nms.model.BaseStation.class,
                new com.abctelecom.nms.dao.BaseStationDao().findAll()));
        out.addAll(dump("subscribers", dir, () -> rows(com.abctelecom.nms.model.Subscriber.class,
                new com.abctelecom.nms.dao.SubscriberDao().findAll()));
        out.addAll(dump("devices", dir, () -> rows(com.abctelecom.nms.model.Device.class,
                new com.abctelecom.nms.dao.DeviceDao().findAll()));
        out.addAll(dump("network_slices", dir, () -> rows(com.abctelecom.nms.model.NetworkSlice.class,
                new com.abctelecom.nms.dao.NetworkSliceDao().findAll()));
        out.addAll(dump("edge_servers", dir, () -> rows(com.abctelecom.nms.model.EdgeServer.class,
                new com.abctelecom.nms.dao.EdgeServerDao().findAll()));
        out.addAll(dump("traffic_flows", dir, () -> rows(com.abctelecom.nms.model.TrafficFlow.class,
                new com.abctelecom.nms.dao.TrafficFlowDao().findAll()));
        out.addAll(dump("resource_allocations", dir, () -> rows(com.abctelecom.nms.model.ResourceAllocation.class,
                new com.abctelecom.nms.dao.ResourceAllocationDao().findAll()));
        out.addAll(dump("optimization_jobs", dir, () -> rows(com.abctelecom.nms.model.OptimizationJob.class,
                new com.abctelecom.nms.dao.OptimizationJobDao().findAll()));
        // users exported WITHOUT password hashes (security: never back up credential material)
        try (BufferedWriter w = Files.newBufferedWriter(dir.resolve("users_no_credentials.csv"))) {
            w.write("user_id,username,full_name,role,enabled\n");
            int n = 0;
            for (com.abctelecom.nms.model.User u : new com.abctelecom.nms.dao.UserDao().findAll()) {
                w.write(u.getUserId() + "," + u.getUsername() + "," + csv(u.getFullName()) + ","
                        + u.getRole().name() + "," + (u.isEnabled() ? 1 : 0) + "\n");
                n++;
            }
            out.add("users_no_credentials|" + n + "|" + dir.resolve("users_no_credentials.csv"));
        }
        return out;
    }

    private interface ThrowingSupplier { List<?> get() throws Exception; }

    private List<String> dump(String table, Path dir, ThrowingSupplier sup) throws Exception {
        List<?> objs = sup.get();
        Path f = dir.resolve(table + ".csv");
        try (BufferedWriter w = Files.newBufferedWriter(f)) {
            if (!objs.isEmpty()) {
                java.lang.reflect.Field[] fs = objs.get(0).getClass().getDeclaredFields();
                StringBuilder h = new StringBuilder();
                for (java.lang.reflect.Field fd : fs) { h.append(fd.getName()).append(','); }
                w.write(h.substring(0, h.length() - 1) + "\n");
                for (Object o : objs) {
                    StringBuilder sb = new StringBuilder();
                    for (java.lang.reflect.Field fd : fs) {
                        fd.setAccessible(true);
                        Object v = fd.get(o);
                        sb.append(csv(v == null ? "" : String.valueOf(v))).append(',');
                    }
                    w.write(sb.substring(0, sb.length() - 1) + "\n");
                }
            }
        }
        return List.of(table + "|" + objs.size() + "|" + f);
    }

    private static List<Object> rows(Class<?> c, List<?> l) { return new java.util.ArrayList<>(l); }

    private static String csv(String s) {
        if (s.contains(",") || s.contains("\"")) return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }
}
