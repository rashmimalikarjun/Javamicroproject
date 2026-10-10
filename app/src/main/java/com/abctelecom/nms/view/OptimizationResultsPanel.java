package com.abctelecom.nms.view;

import com.abctelecom.nms.config.AppConfig;
import com.abctelecom.nms.dao.OptimizationResultDao;
import com.abctelecom.nms.exception.ValidationException;
import com.abctelecom.nms.model.OptimizationJob;
import com.abctelecom.nms.model.OptimizationResult;
import com.abctelecom.nms.service.OptimizationService;
import com.abctelecom.nms.util.CsvUtil;
import com.abctelecom.nms.util.ErrorHelper;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Screen 13: Optimization Results - job history, per-job plan lines, CSV export. */
public class OptimizationResultsPanel extends BasePanel {
    private final OptimizationService svc = new OptimizationService();
    private final JComboBox<String> jobBox = new JComboBox<>();
    private final List<OptimizationJob> jobs = new ArrayList<>();
    private final List<OptimizationResult> currentResults = new ArrayList<>();

    public OptimizationResultsPanel() {
        super("Optimization Results (Local Simulation Output)");
        JPanel bar = new JPanel();
        JButton load = new JButton("Load Jobs");
        JButton view = new JButton("Show Selected Job Plan");
        JButton csv  = new JButton("Export Plan to CSV");
        bar.add(new javax.swing.JLabel("Job:"));
        bar.add(jobBox);
        bar.add(load); bar.add(view); bar.add(csv);
        add(bar, BorderLayout.PAGE_START);
        setColumns("Entity", "Metric", "Before", "After", "Recommendation (LOCAL SIMULATION)", "Generated");

        load.addActionListener(e -> {
            status("Loading jobs...");
            ctx.async(svc::listJobs, js -> {
                jobs.clear(); jobBox.removeAllItems();
                for (OptimizationJob j : js) {
                    jobs.add(j);
                    jobBox.addItem(String.format("#%d %s [%s]", j.getJobId(), j.getJobName(), j.getStatus()));
                }
                status(js.size() + " job(s). Select one and click 'Show Selected Job Plan'.");
            }, err -> ErrorHelper.show(this, err));
        });

        view.addActionListener(e -> {
            OptimizationJob j = selected();
            if (j == null) return;
            status("Loading results for job #" + j.getJobId() + "...");
            ctx.async(() -> svc.resultsFor(j.getJobId()), rs -> {
                clearRows();
                currentResults.clear();
                for (OptimizationResult r : rs) {
                    currentResults.add(r);
                    model.addRow(new Object[]{r.getEntityId(), r.getMetric(),
                            String.format("%.2f", r.getBeforeValue()), String.format("%.2f", r.getAfterValue()),
                            r.getRecommendation(), String.valueOf(r.getGeneratedAt())});
                }
                status(rs.size() + " recommendation line(s) for job #" + j.getJobId()
                        + ". Input CSV: " + j.getInputCsvPath());
            }, err -> ErrorHelper.show(this, err));
        });

        csv.addActionListener(e -> {
            if (currentResults.isEmpty()) {
                ErrorHelper.show(this, new ValidationException("Load a job's plan first, then export."));
                return;
            }
            status("Exporting...");
            ctx.async(() -> {
                String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
                Path file = Path.of(AppConfig.getDataDir(), "opt_results_" + ts + ".csv");
                List<String> header = List.of("entity_id", "metric", "before_value",
                        "after_value", "recommendation", "generated_at");
                List<List<String>> rows = new ArrayList<>();
                for (OptimizationResult r : currentResults)
                    rows.add(List.of(r.getEntityId(), r.getMetric(),
                            String.valueOf(r.getBeforeValue()), String.valueOf(r.getAfterValue()),
                            r.getRecommendation(), String.valueOf(r.getGeneratedAt())));
                try { CsvUtil.write(file, header, rows); }
                catch (IOException io) { throw new com.abctelecom.nms.exception.DaoException("CSV write failed: " + io.getMessage(), io); }
                return file.toString();
            }, path -> status("Exported to " + path), err -> ErrorHelper.show(this, err));
        });
    }

    private OptimizationJob selected() {
        int i = jobBox.getSelectedIndex();
        if (i < 0) { javax.swing.JOptionPane.showMessageDialog(this, "Load jobs first."); return null; }
        return jobs.get(i);
    }
}
