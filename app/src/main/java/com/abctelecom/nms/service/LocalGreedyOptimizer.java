package com.abctelecom.nms.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic local greedy load-balancing SIMULATION.
 *
 * NOT a quantum algorithm and NOT QpiAI Opt. It repeatedly moves Mbps from the
 * most-loaded entity to the least-loaded entity that still has headroom, until
 * every entity is at or below the target utilization or no beneficial move exists.
 * Total offered load is conserved and capacities are never exceeded.
 * Same input always produces the same output (reproducible).
 */
public class LocalGreedyOptimizer implements NetworkOptimizer {

    public static final String ALGORITHM_ID = "LOCAL_GREEDY_SIMULATION";

    @Override public String algorithmId() { return ALGORITHM_ID; }
    @Override public boolean isSimulation() { return true; }

    @Override
    public List<PlanLine> optimize(List<Map<String, Double>> rows, List<String> ids, double targetUtilizationPct) {
        if (rows == null || rows.isEmpty()) return List.of();
        int n = rows.size();
        double[] cap = new double[n];
        double[] load = new double[n];
        for (int i = 0; i < n; i++) {
            cap[i] = Math.max(0.0001, rows.get(i).getOrDefault("capacityMbps", 0.0));
            load[i] = Math.max(0.0, rows.get(i).getOrDefault("loadMbps", 0.0));
            if (load[i] > cap[i]) load[i] = cap[i]; // defensive: respect capacity
        }
        double target = Math.min(100.0, Math.max(1.0, targetUtilizationPct)) / 100.0;

        // Greedy rebalance loop (deterministic): move from hottest to coolest.
        int guard = 0;
        while (guard++ < 10_000) {
            int hot = -1, cool = -1;
            double maxU = -1, minU = 2;
            for (int i = 0; i < n; i++) {
                double u = load[i] / cap[i];
                if (u > maxU) { maxU = u; hot = i; }
            }
            for (int i = 0; i < n; i++) {
                double u = load[i] / cap[i];
                if (u < minU) { minU = u; cool = i; }
            }
            if (hot == cool || maxU <= target) break;                    // balanced enough
            double room = cap[cool] * target - load[cool];               // headroom at target
            double excess = load[hot] - cap[hot] * target;               // above-target amount
            double movable = cap[hot] - load[hot];                       // physical spare at hot
            double move = Math.min(Math.max(excess, 0) + 0.0001, room);
            if (excess <= 0) {                                           // try equalizing anyway
                move = Math.min((maxU - minU) / 2.0 * cap[hot], room);
            }
            move = Math.min(move, Math.max(movable, 0));                 // never exceed source capacity
            if (move <= 0.5) break;                                      // negligible improvement
            load[hot] -= move;
            load[cool] += move;
        }

        List<PlanLine> out = new ArrayList<>();
        Map<String, Double> beforeMap = new HashMap<>();
        for (int i = 0; i < n; i++) beforeMap.put(ids.get(i), rows.get(i).getOrDefault("loadMbps", 0.0));
        for (int i = 0; i < n; i++) {
            double before = beforeMap.get(ids.get(i));
            double after = load[i];
            double beforePct = cap[i] <= 0 ? 0 : before * 100.0 / cap[i];
            double afterPct = cap[i] <= 0 ? 0 : after * 100.0 / cap[i];
            String rec;
            if (Math.abs(after - before) < 0.5 && afterPct <= target * 100 + 0.5)
                rec = "No action needed (within target utilization).";
            else if (after < before - 0.5)
                rec = String.format("Offload %.0f Mbps toward lower-load cells; keep monitoring (was %.1f%%, now %.1f%%).",
                                    before - after, beforePct, afterPct);
            else
                rec = String.format("Accept %.0f Mbps migrated from overloaded cells (now %.1f%%; capacity respected).",
                                    after - before, afterPct);
            out.add(new PlanLine(ids.get(i), "LOAD_PCT", round2(beforePct), round2(afterPct), rec));
        }
        return out;
    }

    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
