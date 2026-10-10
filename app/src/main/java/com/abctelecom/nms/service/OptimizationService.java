package com.abctelecom.nms.service;

import com.abctelecom.nms.config.AppConfig;
import com.abctelecom.nms.dao.AuditDao;
import com.abctelecom.nms.dao.BaseStationDao;
import com.abctelecom.nms.dao.OptimizationJobDao;
import com.abctelecom.nms.dao.OptimizationResultDao;
import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.exception.ValidationException;
import com.abctelecom.nms.model.BaseStation;
import com.abctelecom.nms.model.OptimizationJob;
import com.abctelecom.nms.model.OptimizationResult;
import com.abctelecom.nms.model.User;
import com.abctelecom.nms.util.CsvUtil;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Optimization workflow orchestration:
 *  select eligible resources -> validate -> export CSV dataset -> create job ->
 *  run deterministic LOCAL SIMULATION -> persist results.
 * The optimizer is a pluggable NetworkOptimizer (future QpiAI Opt adapter slot).
 */
public class OptimizationService {

    public static final String SCOPE_BS_LOAD_BALANCING = "BASE_STATION_LOAD_BALANCING";

    private final BaseStationDao stations = new BaseStationDao();
    private final OptimizationJobDao jobs = new OptimizationJobDao();
    private final OptimizationResultDao results = new OptimizationResultDao();
    private final AuditDao audit = new AuditDao();
    private final NetworkOptimizer optimizer;

    public OptimizationService() { this(new LocalGreedyOptimizer()); }

    public OptimizationService(NetworkOptimizer optimizer) { this.optimizer = optimizer; }

    /** Eligible = powered ON, has capacity, participates in load balancing. */
    public List<BaseStation> eligibleResources() throws DaoException {
        List<BaseStation> out = new ArrayList<>();
        for (BaseStation b : stations.findAll())
            if ("ON".equals(b.getPowerStatus()) && b.getCapacityMbps() > 0) out.add(b);
        return out;
    }

    /** Export the optimization input dataset as CSV; returns file path. Documented columns:
     *  entity_id, capacity_mbps, load_mbps, utilization_pct, technology */
    public Path exportDatasetCsv(List<BaseStation> eligible, String scope) throws QitromException {
        if (eligible.isEmpty()) throw new ValidationException("No eligible resources selected - nothing to optimize");
        try {
            String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
            Path file = Path.of(AppConfig.getDataDir(), "opt_input_" + scope + "_" + ts + ".csv");
            List<String> header = List.of("entity_id", "capacity_mbps", "load_mbps", "utilization_pct", "technology");
            List<List<String>> rows = new ArrayList<>();
            for (BaseStation b : eligible) {
                rows.add(List.of(b.getStationId(), String.valueOf(b.getCapacityMbps()),
                        String.valueOf(b.getCurrentLoadMbps()),
                        String.format("%.2f", b.utilizationPct()), b.getTechnology()));
            }
            CsvUtil.write(file, header, rows);
            return file;
        } catch (java.io.IOException e) {
            throw new DaoException("Failed to write optimization CSV: " + e.getMessage(), e);
        }
    }

    /** Create the job record (status PENDING). Validates inputs and RBAC. */
    public int submitJob(String jobName, List<BaseStation> eligible, double targetUtilizationPct, User actor)
            throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.RUN_OPTIMIZATION);
        Validators.required(jobName, "Job name");
        if (eligible == null || eligible.size() < 2)
            throw new ValidationException("Select at least two eligible resources for load balancing");
        if (targetUtilizationPct < 10 || targetUtilizationPct > 100)
            throw new ValidationException("Target utilization must be between 10% and 100%");
        for (BaseStation b : eligible)
            if (b.getCurrentLoadMbps() < 0 || b.getCurrentLoadMbps() > b.getCapacityMbps())
                throw new ValidationException("Invalid load/capacity for " + b.getStationId());

        Path csv = exportDatasetCsv(eligible, SCOPE_BS_LOAD_BALANCING);
        OptimizationJob j = new OptimizationJob();
        j.setJobName(jobName.trim());
        j.setAlgorithm(optimizer.algorithmId());
        j.setStatus(OptimizationJob.Status.PENDING);
        j.setInputCsvPath(csv.toString());
        j.setScope(SCOPE_BS_LOAD_BALANCING);
        j.setSubmittedBy(actor.getUserId());
        int jobId = jobs.create(j);
        audit.record(actor.getUserId(), "SUBMIT_OPTIMIZATION_JOB", "OPTIMIZATION_JOB",
                     String.valueOf(jobId), "algorithm=" + j.getAlgorithm() + " (local simulation)");
        return jobId;
    }

    /** Execute the simulation for a PENDING job and persist results. Run off the EDT. */
    public List<OptimizationResult> executeJob(int jobId, double targetUtilizationPct) throws QitromException {
        OptimizationJob job = jobs.findById(jobId);
        if (job == null) throw new ValidationException("Unknown job id: " + jobId);
        if (job.getStatus() != OptimizationJob.Status.PENDING)
            throw new ValidationException("Job is not PENDING (status=" + job.getStatus() + ")");
        jobs.markRunning(jobId);
        try {
            List<BaseStation> eligible = eligibleResources();
            List<Map<String, Double>> rows = new ArrayList<>();
            List<String> ids = new ArrayList<>();
            for (BaseStation b : eligible) {
                Map<String, Double> m = new HashMap<>();
                m.put("capacityMbps", (double) b.getCapacityMbps());
                m.put("loadMbps", (double) b.getCurrentLoadMbps());
                rows.add(m);
                ids.add(b.getStationId());
            }
            List<NetworkOptimizer.PlanLine> plan = optimizer.optimize(rows, ids, targetUtilizationPct);
            if (plan.isEmpty()) throw new ValidationException("Optimizer produced no results (empty dataset)");

            List<OptimizationResult> persisted = new ArrayList<>();
            for (NetworkOptimizer.PlanLine p : plan) {
                OptimizationResult r = new OptimizationResult();
                r.setJobId(jobId);
                r.setEntityId(p.entityId);
                r.setMetric(p.metric);
                r.setBeforeValue(p.before);
                r.setAfterValue(p.after);
                r.setRecommendation("[LOCAL SIMULATION] " + p.recommendation);
                results.insert(r);
                persisted.add(r);
            }
            jobs.markCompleted(jobId);
            return persisted;
        } catch (Exception e) {
            jobs.markFailed(jobId, e.getMessage());
            if (e instanceof QitromException qe) throw qe;
            throw new DaoException("Optimization failed: " + e.getMessage(), e);
        }
    }

    public List<OptimizationJob> listJobs() throws DaoException { return jobs.findAll(); }
    public List<OptimizationResult> resultsFor(int jobId) throws DaoException { return results.findByJob(jobId); }
}
