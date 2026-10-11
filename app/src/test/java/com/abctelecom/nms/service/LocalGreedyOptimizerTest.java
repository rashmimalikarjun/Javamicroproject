package com.abctelecom.nms.service;

import com.abctelecom.nms.service.NetworkOptimizer.PlanLine;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for the deterministic local greedy simulation (no DB required). */
class LocalGreedyOptimizerTest {

    private static Map<String, Double> row(double cap, double load) {
        Map<String, Double> m = new HashMap<>();
        m.put("capacityMbps", cap);
        m.put("loadMbps", load);
        return m;
    }

    @Test
    void metadataIdentifiesItAsSimulation() {
        NetworkOptimizer opt = new LocalGreedyOptimizer();
        assertEquals(LocalGreedyOptimizer.ALGORITHM_ID, opt.algorithmId());
        assertTrue(opt.isSimulation(), "must be flagged as a simulation, not a quantum algorithm");
    }

    @Test
    void emptyInputProducesNoPlanLines() {
        NetworkOptimizer opt = new LocalGreedyOptimizer();
        assertTrue(opt.optimize(List.of(), List.of(), 70).isEmpty());
    }

    @Test
    void balancedWithinTargetNeedsNoMoves() {
        NetworkOptimizer opt = new LocalGreedyOptimizer();
        List<PlanLine> plan = opt.optimize(
                List.of(row(100, 50), row(100, 60)), List.of("BS-A", "BS-B"), 70);
        assertEquals(2, plan.size(), "one line per resource");
        for (PlanLine p : plan)
            assertEquals(p.before, p.after, 0.01, "within-target cells must not move");
        // total load conserved
        double totalAfter = plan.stream().mapToDouble(p -> p.after).sum();
        assertEquals(110.0, totalAfter, 0.01);
    }

    @Test
    void offloadsHotCellTowardsCoolCellAndConservesLoad() {
        NetworkOptimizer opt = new LocalGreedyOptimizer();
        List<PlanLine> plan = opt.optimize(
                List.of(row(100, 95), row(100, 10)), List.of("HOT", "COOL"), 70);
        assertEquals(2, plan.size());
        double totalAfter = plan.stream().mapToDouble(p -> p.after).sum();
        assertEquals(105.0, totalAfter, 0.5, "greedy moves conserve offered load");
        PlanLine hot = plan.stream().filter(p -> p.entityId.equals("HOT")).findFirst().orElseThrow();
        assertTrue(hot.after < hot.before, "the overloaded cell must shed load");
        PlanLine cool = plan.stream().filter(p -> p.entityId.equals("COOL")).findFirst().orElseThrow();
        assertTrue(cool.after > cool.before, "the cool cell must accept migrated load");
        for (PlanLine p : plan) assertTrue(p.after >= 0 && p.after <= 100 + 0.001,
                "capacity respected / no negative load");
    }

    @Test
    void isDeterministicForSameInput() {
        NetworkOptimizer opt = new LocalGreedyOptimizer();
        List<Map<String, Double>> rows = List.of(row(100, 90), row(100, 20), row(100, 75));
        List<String> ids = List.of("A", "B", "C");
        List<PlanLine> first = opt.optimize(rows, ids, 70);
        List<PlanLine> second = opt.optimize(rows, ids, 70);
        assertEquals(first.size(), second.size());
        for (int i = 0; i < first.size(); i++) {
            PlanLine a = first.get(i), b = second.get(i);
            assertEquals(a.entityId, b.entityId);
            assertEquals(a.before, b.before, 0.0, "before load must be identical run-to-run");
            assertEquals(a.after, b.after, 0.0, "after load must be identical run-to-run");
            assertEquals(a.recommendation, b.recommendation);
        }
    }
}
