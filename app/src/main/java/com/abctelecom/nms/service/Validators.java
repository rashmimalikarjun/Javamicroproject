package com.abctelecom.nms.service;

import com.abctelecom.nms.exception.ValidationException;

import java.util.Set;
import java.util.regex.Pattern;

/** Central input validation helpers (Java course concepts: strings, regex, operators). */
public final class Validators {
    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{2,20}$");
    public static final Set<String> TECHNOLOGIES = Set.of("4G", "5G", "6G");
    public static final Set<String> POWER_STATUS = Set.of("ON", "OFF", "SLEEP");
    public static final Set<String> SERVICE_PLANS = Set.of("BASIC", "STANDARD", "PREMIUM", "ENTERPRISE", "IOT");
    public static final Set<String> DEVICE_TYPES = Set.of("SMARTPHONE", "CPE", "IOT_SENSOR", "ROUTER", "TABLET", "WEARABLE");
    public static final Set<String> DEVICE_STATUS = Set.of("ACTIVE", "IDLE", "OFFLINE");
    public static final Set<String> SLICE_TYPES = Set.of("eMBB", "URLLC", "mMTC");
    public static final Set<String> EDGE_STATUS = Set.of("ONLINE", "OFFLINE", "MAINTENANCE");
    public static final Set<String> CONGESTION = Set.of("NORMAL", "HIGH", "CRITICAL");
    public static final Set<String> RESOURCE_TYPES = Set.of("CPU", "MEMORY", "SPECTRUM", "POWER", "USER");

    private Validators() {}

    public static String required(String value, String field) throws ValidationException {
        if (value == null || value.trim().isEmpty())
            throw new ValidationException(field + " is required");
        return value.trim();
    }

    public static String id(String value, String field) throws ValidationException {
        String v = required(value, field);
        if (!ID_PATTERN.matcher(v).matches())
            throw new ValidationException(field + " must match [A-Za-z0-9._-]{2,20}");
        return v;
    }

    public static int rangeInt(String value, String field, int min, int max) throws ValidationException {
        long n;
        try { n = Long.parseLong(required(value, field).trim()); }
        catch (NumberFormatException e) { throw new ValidationException(field + " must be a whole number"); }
        if (n < min || n > max) throw new ValidationException(field + " must be between " + min + " and " + max);
        return (int) n;
    }

    public static double rangeDouble(String value, String field, double min, double max) throws ValidationException {
        double d;
        try { d = Double.parseDouble(required(value, field).trim()); }
        catch (NumberFormatException e) { throw new ValidationException(field + " must be a number"); }
        if (Double.isNaN(d) || Double.isInfinite(d)) throw new ValidationException(field + " must be finite");
        if (d < min || d > max) throw new ValidationException(field + " must be between " + min + " and " + max);
        return d;
    }

    public static String oneOf(String value, String field, Set<String> allowed) throws ValidationException {
        String v = required(value, field);
        for (String a : allowed) if (a.equalsIgnoreCase(v)) return a; // normalize case
        throw new ValidationException(field + " must be one of " + allowed);
    }

    /** Load can never exceed capacity in the domain model. */
    public static void loadWithinCapacity(int loadMbps, int capacityMbps) throws ValidationException {
        if (loadMbps > capacityMbps)
            throw new ValidationException("Current load (" + loadMbps + " Mbps) exceeds capacity ("
                    + capacityMbps + " Mbps). Increase capacity or reduce load first.");
    }
}
