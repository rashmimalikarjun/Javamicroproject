package com.abctelecom.nms.service;

import com.abctelecom.nms.config.AppConfig;
import com.abctelecom.nms.dao.*;
import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.exception.ValidationException;
import com.abctelecom.nms.model.*;
import com.abctelecom.nms.util.CsvUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Report generation: builds datasets from stored data, exports CSV, records metadata. */
public class ReportService {
    public static final List<String> TYPES = List.of(
        "NETWORK_UTILIZATION", "TRAFFIC", "CELL_LOAD", "EDGE_RESOURCES",
        "QOS", "ENERGY", "OPTIMIZATION_RECOMMENDATIONS", "DAILY_PERFORMANCE");

    private final BaseStationDao stations = new BaseStationDao();
    private final EdgeServerDao edge = new EdgeServerDao();
    private final TrafficFlowDao flows = new TrafficFlowDao();
    private final SubscriberDao subs = new SubscriberDao();
    private final OptimizationJobDao jobs = new OptimizationJobDao();
    private final OptimizationResultDao optResults = new OptimizationResultDao();
    private final NetworkKpiDao kpis = new NetworkKpiDao();
    private final ReportDao reports = new ReportDao();
    private final AuditDao audit = new AuditDao();
    private final KpiService kpiService = new KpiService();

    /** Generate a report of the given type; returns the created Report (with csvPath). */
    public Report generate(String type, User actor) throws QitromException {
        AccessControl.require(actor, AccessControl.Capability.GENERATE_REPORTS);
        if (!TYPES.contains(type)) throw new ValidationException("Unknown report type: " + type);
        String ts = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        Path file = Path.of(AppConfig.getDataDir(), "report_" + type.toLowerCase() + "_" + ts + ".csv");
        List<String> header;
        List<List<String>> rows = new ArrayList<>();
        String summary;

        switch (type) {
            case "NETWORK_UTILIZATION": {
                header = List.of("metric", "value", "unit", "simulated");
                for (NetworkKpi k : kpiService.computeCurrent())
                    rows.add(List.of(k.getKpiName(), String.valueOf(k.getKpiValue()), k.getUnit(),
                                      k.isSimulated() ? "SIMULATED" : "COMPUTED"));
                summary = "Full KPI snapshot at " + ts;
                break;
            }
            case "CELL_LOAD": {
                header = List.of("station_id", "cell_name", "technology", "capacity_mbps",
                                 "load_mbps", "utilization_pct", "power_status");
                for (BaseStation b : stations.findAll())
                    rows.add(List.of(b.getStationId(), b.getCellName(), b.getTechnology(),
                        String.valueOf(b.getCapacityMbps()), String.valueOf(b.getCurrentLoadMbps()),
                        String.format("%.2f", b.utilizationPct()), b.getPowerStatus()));
                summary = "Per-cell load table";
                break;
            }
            case "TRAFFIC": {
                header = List.of("flow_id", "source", "dest", "bandwidth_mbps", "latency_ms",
                                 "packet_loss_pct", "throughput_mbps", "congestion");
                for (TrafficFlow f : flows.findAll())
                    rows.add(List.of(f.getFlowId(), f.getSourceNode(), f.getDestNode(),
                        String.valueOf(f.getBandwidthMbps()), String.valueOf(f.getLatencyMs()),
                        String.valueOf(f.getPacketLossPct()), String.valueOf(f.getThroughputMbps()),
                        f.getCongestionStatus()));
                summary = "All traffic flows";
                break;
            }
            case "EDGE_RESOURCES": {
                header = List.of("server_id", "location", "cpu_pct", "mem_used_gb", "mem_total_gb",
                                 "storage_used_tb", "storage_total_tb", "edge_load_pct", "status", "apps");
                for (EdgeServer e : edge.findAll())
                    rows.add(List.of(e.getServerId(), e.getSiteLocation(),
                        String.valueOf(e.getCpuUtilizationPct()), String.valueOf(e.getMemoryUsedGb()),
                        String.valueOf(e.getMemoryTotalGb()), String.valueOf(e.getStorageUsedTb()),
                        String.valueOf(e.getStorageTotalTb()), String.valueOf(e.getEdgeLoadPct()),
                        e.getStatus(), String.valueOf(e.getRunningApps())));
                summary = "Edge fleet resource table";
                break;
            }
            case "QOS": {
                header = List.of("subscriber_id", "name", "plan", "qos_level", "cell", "usage_mb");
                for (Subscriber s : subs.findAll())
                    rows.add(List.of(s.getSubscriberId(), s.getName(), s.getServicePlan(),
                        String.valueOf(s.getQosLevel()), String.valueOf(s.getConnectedCell()),
                        String.valueOf(s.getDataUsageMb())));
                summary = "Subscriber QoS distribution";
                break;
            }
            case "ENERGY": {
                header = List.of("model_input_util_pct", "active_stations", "avg_edge_cpu_pct",
                                 "energy_kwh_per_hour", "note");
                double util = stations.totalUtilizationPct();
                double avgCpu = edge.findAll().stream().mapToDouble(EdgeServer::getCpuUtilizationPct)
                                    .average().orElse(0);
                int active = stations.countActive();
                rows.add(List.of(String.format("%.2f", util), String.valueOf(active),
                        String.format("%.2f", avgCpu),
                        String.valueOf(KpiService.simulateEnergyKwh(util, active, avgCpu)),
                        "SIMULATED deterministic model - see docs"));
                summary = "Simulated energy estimate";
                break;
            }
            case "OPTIMIZATION_RECOMMENDATIONS": {
                header = List.of("job_id", "entity_id", "metric", "before_pct", "after_pct", "recommendation");
                for (OptimizationJob j : jobs.findAll()) {
                    if (j.getStatus() != OptimizationJob.Status.COMPLETED) continue;
                    for (OptimizationResult r : optResults.findByJob(j.getJobId()))
                        rows.add(List.of(String.valueOf(j.getJobId()), r.getEntityId(), r.getMetric(),
                            String.valueOf(r.getBeforeValue()), String.valueOf(r.getAfterValue()),
                            String.valueOf(r.getRecommendation())));
                }
                summary = "All completed optimization recommendations (local simulation)";
                break;
            }
            case "DAILY_PERFORMANCE":
            default: {
                header = List.of("kpi_name", "value", "unit", "simulated", "snapshot_at");
                for (NetworkKpi k : kpis.latestSnapshots())
                    rows.add(List.of(k.getKpiName(), String.valueOf(k.getKpiValue()), k.getUnit(),
                        k.isSimulated() ? "SIMULATED" : "COMPUTED",
                        String.valueOf(k.getSnapshotAt())));
                summary = "Latest stored KPI snapshots";
                break;
            }
        }

        try {
            CsvUtil.write(file, header, rows);
        } catch (IOException e) {
            throw new DaoException("CSV export failed: " + e.getMessage(), e);
        }
        Report rep = new Report();
        rep.setReportType(type);
        rep.setTitle(type + " report " + ts);
        rep.setCsvPath(file.toString());
        rep.setSummary(summary);
        rep.setGeneratedBy(actor.getUserId());
        int id = reports.create(rep);
        rep.setReportId(id);
        audit.record(actor.getUserId(), "GENERATE_REPORT", "REPORT", type, "csv=" + file);
        return rep;
    }

    public List<Report> list() throws DaoException { return reports.findAll(); }
}
