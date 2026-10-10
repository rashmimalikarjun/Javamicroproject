package com.abctelecom.nms.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Loads database/app configuration from (in priority order):
 * 1. System properties (-Dqitrom.db.url=...)
 * 2. Environment variables (QITROM_DB_URL, QITROM_DB_USER, QITROM_DB_PASSWORD)
 * 3. ./config.properties (git-ignored; copy from config.properties.example)
 * Credentials are NEVER committed to the repository.
 */
public final class AppConfig {
    private static final Logger LOG = Logger.getLogger(AppConfig.class.getName());
    private static final Properties props = new Properties();
    private static boolean loaded = false;

    private AppConfig() {}

    public static synchronized void load() {
        if (loaded) return;
        Path p = Path.of("config.properties");
        if (Files.isReadable(p)) {
            try (InputStream in = Files.newInputStream(p)) {
                props.load(in);
                LOG.info("Loaded configuration from " + p.toAbsolutePath());
            } catch (IOException e) {
                LOG.warning("Could not read config.properties: " + e.getMessage());
            }
        }
        // environment overrides
        overlayEnv("qitrom.db.url", "QITROM_DB_URL");
        overlayEnv("qitrom.db.user", "QITROM_DB_USER");
        overlayEnv("qitrom.db.password", "QITROM_DB_PASSWORD");
        loaded = true;
    }

    private static void overlayEnv(String key, String env) {
        String v = System.getenv(env);
        if (v != null && !v.isBlank()) props.setProperty(key, v);
    }

    /** Resolve a property: system property > env overlay > file value > default. */
    public static String get(String key, String def) {
        load();
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys;
        return props.getProperty(key, def);
    }

    public static String getDbUrl()      { return get("qitrom.db.url", "jdbc:mysql://localhost:3306/qitrom_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"); }
    public static String getDbUser()     { return get("qitrom.db.user", "qitrom_user"); }
    public static String getDbPassword() { return get("qitrom.db.password", ""); }
    public static String getDataDir()    { return get("qitrom.data.dir", "data"); }
}
