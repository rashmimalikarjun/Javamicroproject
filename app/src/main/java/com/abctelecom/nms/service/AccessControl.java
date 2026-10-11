package com.abctelecom.nms.service;

import com.abctelecom.nms.model.Role;
import com.abctelecom.nms.model.User;
import com.abctelecom.nms.exception.UnauthorizedActionException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Role-based access control (least privilege). Each capability maps to the set
 * of roles allowed to use it. Views call {@code can()} to show/hide actions.
 */
public final class AccessControl {

    public enum Capability {
        MANAGE_USERS,               // user management screen (NETWORK_ADMIN only)
        MANAGE_BASE_STATIONS,       // CRUD base stations
        MANAGE_SUBSCRIBERS_DEVICES, // CRUD subscribers/devices
        MANAGE_SLICES,              // CRUD network slices
        MANAGE_EDGE_SERVERS,        // CRUD edge servers
        MONITOR_TRAFFIC,            // view/add traffic flows
        ALLOCATE_RESOURCES,         // create resource allocations
        RUN_OPTIMIZATION,           // submit optimization jobs
        VIEW_RESULTS,               // view optimization results/KPIs
        MANAGE_ALERTS,              // acknowledge/close alerts
        GENERATE_REPORTS,
        VIEW_AUDIT_LOG
    }

    private static final Map<Capability, Set<Role>> MATRIX = new EnumMap<>(Capability.class);
    static {
        MATRIX.put(Capability.MANAGE_USERS,               Set.of(Role.NETWORK_ADMIN));
        MATRIX.put(Capability.MANAGE_BASE_STATIONS,       Set.of(Role.NETWORK_ADMIN, Role.NOC_ENGINEER));
        MATRIX.put(Capability.MANAGE_SUBSCRIBERS_DEVICES, Set.of(Role.NETWORK_ADMIN, Role.NOC_ENGINEER));
        MATRIX.put(Capability.MANAGE_SLICES,              Set.of(Role.NETWORK_ADMIN, Role.CLOUD_ADMIN));
        MATRIX.put(Capability.MANAGE_EDGE_SERVERS,        Set.of(Role.NETWORK_ADMIN, Role.EDGE_ADMIN, Role.CLOUD_ADMIN));
        MATRIX.put(Capability.MONITOR_TRAFFIC,            Set.of(Role.NETWORK_ADMIN, Role.NOC_ENGINEER, Role.CLOUD_ADMIN));
        MATRIX.put(Capability.ALLOCATE_RESOURCES,         Set.of(Role.NETWORK_ADMIN, Role.CLOUD_ADMIN, Role.EDGE_ADMIN, Role.OPTIMIZATION_ENGINEER));
        MATRIX.put(Capability.RUN_OPTIMIZATION,           Set.of(Role.NETWORK_ADMIN, Role.OPTIMIZATION_ENGINEER));
        MATRIX.put(Capability.VIEW_RESULTS,               Set.of(Role.values()));
        MATRIX.put(Capability.MANAGE_ALERTS,              Set.of(Role.NETWORK_ADMIN, Role.NOC_ENGINEER));
        MATRIX.put(Capability.GENERATE_REPORTS,           Set.of(Role.NETWORK_ADMIN, Role.NOC_ENGINEER, Role.OPTIMIZATION_ENGINEER));
        MATRIX.put(Capability.VIEW_AUDIT_LOG,             Set.of(Role.NETWORK_ADMIN));
    }

    private AccessControl() {}

    public static boolean can(User user, Capability cap) {
        return user != null && MATRIX.getOrDefault(cap, Set.of()).contains(user.getRole());
    }

    public static void require(User user, Capability cap) {
        if (!can(user, cap))
            throw new UnauthorizedActionException(
                "Role " + (user == null ? "?" : user.getRole()) + " may not perform: " + cap);
    }
}
