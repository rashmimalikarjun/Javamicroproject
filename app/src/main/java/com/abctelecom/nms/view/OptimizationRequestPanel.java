package com.abctelecom.nms.view;

import com.abctelecom.nms.exception.ValidationException;
import com.abctelecom.nms.model.BaseStation;
import com.abctelecom.nms.service.AccessControl.Capability;
import com.abctelecom.nms.service.OptimizationService;
import com.abctelecom.nms.util.ErrorHelper;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Screen 12: Optimization Request. Selects eligible resources, exports the
 * CSV dataset, creates a job and runs the LOCAL DETERMINISTIC SIMULATION
 * (clearly NOT QpiAI Opt / not quantum) on a background thread.
 */
public class OptimizationRequestPanel extends BasePanel {
    private final OptimizationService svc = new OptimizationService();
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> list = new JList<>(listModel);
    private final JTextField jobName = new JTextField(16);
    private final JTextField targetUtil = new JTextField("70", 6);
    private final List<BaseStation> eligible = new ArrayList<>();
    private final JLabel algoLabel = new JLabel(
        "<html><b>Algorithm:</b> LocalGreedyOptimizer v1 &mdash; deterministic local heuristic SIMULATION.<br>" +
        "This is <font color='red'>NOT</font> QpiAI Opt and <font color='red'>NOT</font> a quantum algorithm.</html>");

    public OptimizationRequestPanel() {
        super("Optimization Request (Local Simulation)");
        JPanel top = new JPanel(new BorderLayout(6, 4));
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createTitledBorder("Job parameters"));
        form.add(row("Job name:", jobName));
        form.add(row("Target utilization %:", targetUtil));
        JButton loadBtn = new JButton("Load Eligible Resources");
        JButton runBtn  = new JButton("Export CSV, Submit & Run Job");
        loadBtn.setAlignmentX(LEFT_ALIGNMENT);
        runBtn.setAlignmentX(LEFT_ALIGNMENT);
        form.add(Box.createVerticalStrut(6));
        form.add(loadBtn);
        form.add(Box.createVerticalStrut(4));
        form.add(runBtn);
        form.add(Box.createVerticalStrut(6));
        form.add(algoLabel);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        top.add(form, BorderLayout.WEST);
        top.add(new JScrollPane(list), BorderLayout.CENTER);
        add(top, BorderLayout.CENTER);

        boolean allowed = ctx.can(Capability.RUN_OPTIMIZATION);
        runBtn.setEnabled(allowed);
        if (!allowed) status("Your role may not submit optimization jobs (view-only here).");

        loadBtn.addActionListener(e -> {
            status("Loading eligible resources (power ON, capacity > 0)...");
            ctx.async(() -> {
                List<BaseStation> l = svc.eligibleResources();
                return l;
            }, l -> {
                eligible.clear(); listModel.clear();
                int i = 0;
                for (BaseStation b : l) {
                    eligible.add(b);
                    listModel.addElement(String.format("%s | %s | load %d/%d Mbps (%.1f%%)",
                            b.getStationId(), b.getTechnology(), b.getCurrentLoadMbps(),
                            b.getCapacityMbps(), b.utilizationPct()));
                    i++;
                }
                if (i > 0) list.setSelectedIndices(allIndices(i));
                status(i + " eligible resource(s). Select rows to include, then submit.");
            }, err -> ErrorHelper.show(this, err));
        });

        runBtn.addActionListener(e -> {
            List<Integer> selIdx = list.getSelectedIndices().length == 0
                    ? allIndices(eligible.size()) : box(eligible.size(), list.getSelectedIndices());
            List<BaseStation> chosen = new ArrayList<>();
            for (int idx : selIdx) chosen.add(eligible.get(idx));
            double target;
            try { target = Double.parseDouble(targetUtil.getText().trim()); }
            catch (NumberFormatException ex) {
                ErrorHelper.show(this, new ValidationException("Target utilization must be a number"));
                return;
            }
            status("Submitting job...");
            ctx.async(() -> {
                int jobId = svc.submitJob(jobName.getText(), chosen, target, ctx.currentUser());
                List<?> results = svc.executeJob(jobId, target);
                return "Job #" + jobId + " COMPLETED with " + results.size()
                        + " recommendation line(s). CSV exported. See Optimization Results screen.";
            }, msg -> status(msg), err -> { ErrorHelper.show(this, err); status("Job failed - see error dialog."); });
        });
    }

    private static int[] allIndices(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = i;
        return a;
    }
    private static List<Integer> box(int n, int[] sel) {
        List<Integer> out = new ArrayList<>();
        if (sel.length == 0) for (int i = 0; i < n; i++) out.add(i); else for (int s : sel) out.add(s);
        return out;
    }
    private JPanel row(String label, javax.swing.JComponent c) {
        JPanel p = new JPanel(new BorderLayout(4, 2));
        p.setMaximumSize(new java.awt.Dimension(320, 32));
        p.add(new JLabel(label), BorderLayout.WEST);
        p.add(c, BorderLayout.CENTER);
        return p;
    }
}
