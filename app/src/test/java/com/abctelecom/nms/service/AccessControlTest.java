package com.abctelecom.nms.service;

import com.abctelecom.nms.exception.UnauthorizedActionException;
import com.abctelecom.nms.model.Role;
import com.abctelecom.nms.model.User;
import com.abctelecom.nms.service.AccessControl.Capability;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccessControlTest {

    private static User user(Role r) {
        User u = new User();
        u.setUserId(1);
        u.setUsername("u-" + r.name());
        u.setRole(r);
        return u;
    }

    @Test
    void nullUserHasNoCapabilities() {
        assertFalse(AccessControl.can(null, Capability.VIEW_RESULTS));
    }

    @Test
    void runOptimizationOnlyAdminAndOptEngineer() {
        assertTrue(AccessControl.can(user(Role.NETWORK_ADMIN), Capability.RUN_OPTIMIZATION));
        assertTrue(AccessControl.can(user(Role.OPTIMIZATION_ENGINEER), Capability.RUN_OPTIMIZATION));
        assertFalse(AccessControl.can(user(Role.NOC_ENGINEER), Capability.RUN_OPTIMIZATION));
        assertFalse(AccessControl.can(user(Role.EDGE_ADMIN), Capability.RUN_OPTIMIZATION));
    }

    @Test
    void manageUsersAndAuditAreAdminOnly() {
        assertTrue(AccessControl.can(user(Role.NETWORK_ADMIN), Capability.MANAGE_USERS));
        assertFalse(AccessControl.can(user(Role.CLOUD_ADMIN), Capability.MANAGE_USERS));
        assertFalse(AccessControl.can(user(Role.NOC_ENGINEER), Capability.VIEW_AUDIT_LOG));
    }

    @Test
    void everyoneCanViewResults() {
        for (Role r : Role.values())
            assertTrue(AccessControl.can(user(r), Capability.VIEW_RESULTS), r.name());
    }

    @Test
    void requireThrowsForForbiddenCapability() {
        User noc = user(Role.NOC_ENGINEER);
        assertThrows(UnauthorizedActionException.class,
                () -> AccessControl.require(noc, Capability.MANAGE_USERS));
        assertDoesNotThrow(() -> AccessControl.require(noc, Capability.MANAGE_BASE_STATIONS));
    }
}
