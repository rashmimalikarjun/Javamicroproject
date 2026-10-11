package com.abctelecom.nms.service;

import com.abctelecom.nms.dao.*;
import com.abctelecom.nms.exception.DaoException;
import com.abctelecom.nms.model.NetworkKpi;

import java.util.ArrayList;
import java.util.List;

/**
 * KPI computation. Most metrics are computed from stored data (isSimulated=false).
 * Energy consumption is SIMULATED via a documented deterministic model and flagged.
 */
public class KpiService {
    private final BaseStationDao bs = new BaseStationDao();
    private final SubscriberDao subs = new SubscriberDao();
    private final TrafficFlowDao flows = new TrafficFlowDao();
    private final EdgeServerDao edge = new EdgeServerDao();
    private final NetworkSliceDao slices = new NetworkSliceDao();
    private final NetworkKpiDao kpiDao = new NetworkKpiDao();

    /** Deterministic simulated energy model: each ON base station consumes a
     *  technology-dependent baseline plus load-proportional power (documented assumption). */
    public static double simulateEnergyKwh(double avgUtilPct, int activeStations, double avgEdgeCpuPct) {
        double perStationBase = 12.0;               // kWh per hour idle-ish
        double techFactor = 1.15;                  // 5G-heavy average
        double loadPart = (avgUtilPct / 100.0) * 8.0;
        double edgePart = (avgEdgeCpuPct / 100.0) * 5.0;
        return round2(activeStations * (perStationBase * techFactor + loadPart) + edgePart);
    }

    public List<NetworkKpi> computeCurrent() throws DaoException {
        List<NetworkKpi> out = new ArrayList<>();
        double util = bs.totalUtilizationPct();
        out.add(new NetworkKpi("NETWORK_UTILIZATION", round2(util), "%", false));
        out.add(new NetworkKpi("AVG_LATENCY", round2(flows.avgLatencyMs()), "ms", false));
        out.add(new NetworkKpi("TOTAL_THROUGHPUT", round2(flows.totalThroughputMbps()), "Mbps", false));
        out.add(new NetworkKpi("AVG_PACKET_LOSS", round2(flows.avgPacketLossPct()), "%", false));
        out.add(new NetworkKpi("ACTIVE_USERS", subs.countActiveUsers(), "subscribers", false));
        out.add(new NetworkKpi("ACTIVE_BASE_STATIONS", bs.countActive(), "stations", false));
        out.add(new NetworkKpi("SLICE_POOL_USED", round2(slices.totalAllocationPct()), "%", false));

        var servers = edge.findAll();
        double avgEdgeCpu = servers.stream().mapToDouble(e -> e.getCpuUtilizationPct()).average().orElse(0);
        double avgEdgeMem = servers.stream().mapToDouble(e -> e.memoryPct()).average().orElse(0);
        out.add(new NetworkKpi("EDGE_CPU_AVG", round2(avgEdgeCpu), "%", false));
        out.add(new NetworkKpi("EDGE_MEMORY_AVG", round2(avgEdgeMem), "%", false));
        out.add(new NetworkKpi("RESOURCE_UTILIZATION", round2((util + avgEdgeCpu) / 2.0), "%", false));

        // SIMULATED metric - explicitly flagged
        out.add(new NetworkKpi("ENERGY_CONSUMPTION", simulateEnergyKwh(util, bs.countActive(), avgEdgeCpu), "kWh/hour", true));
        return out;
    }

    /** Compute + persist a snapshot (called by the background KPI monitor thread). */
    public List<NetworkKpi> snapshotAndStore() throws DaoException {
        List<NetworkKpi> ks = computeCurrent();
        for (NetworkKpi k : ks) kpiDao.insert(k);
        return ks;
    }

    public List<NetworkKpi> latestStored() throws DaoException { return kpiDao.latestSnapshots(); }

    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
