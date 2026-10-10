package com.abctelecom.nms.service;

import java.util.List;
import java.util.Map;

/**
 * Abstraction over an optimization backend. The default implementation is a
 * deterministic LOCAL heuristic SIMULATION (LocalGreedyOptimizer). This interface
 * exists so a future adapter (e.g., for the QpiAI Opt API) can be plugged in —
 * NO such integration is implemented or claimed in this project. Any real adapter
 * must be built against officially documented endpoints only.
 */
public interface NetworkOptimizer {

    /** Human-readable algorithm id stored with jobs. */
    String algorithmId();

    /** True if this is a local simulation rather than a true solver/API. */
    boolean isSimulation();

    /**
     * Eligible resource row: entityId, capacityMbps, loadMbps (capacity >= 0, load >= 0, load <= capacity).
     * @param rows eligible base-station-like resources
     * @param targetUtilizationPct desired maximum utilization after rebalancing
     * @return per-entity before/after load plan plus recommendations
     */
    List<PlanLine> optimize(List<Map<String, Double>> rows, List<String> ids, double targetUtilizationPct);

    /** Immutable-ish result line of a plan. */
    final class PlanLine {
        public final String entityId;
        public final String metric;
        public final double before;
        public final double after;
        public final String recommendation;
        public PlanLine(String entityId, String metric, double before, double after, String recommendation) {
            this.entityId = entityId; this.metric = metric; this.before = before; this.after = after;
            this.recommendation = recommendation;
        }
    }
}
