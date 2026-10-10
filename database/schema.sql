-- =====================================================================
-- QITROM - Quantum-Inspired Telecom Network Resource Optimization and
-- Management System (SIMULATED; no real quantum computing, no QpiAI Opt API)
-- MySQL 8.x schema for ABC Telecom Pvt. Ltd.
--
-- Setup:
--   mysql -u root -p < database/schema.sql
-- Configuration: see app/config.properties.example (copy to config.properties)
-- =====================================================================

DROP DATABASE IF EXISTS qitrom_db;
CREATE DATABASE qitrom_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE qitrom_db;

-- ---------------------------------------------------------------------
-- 1. users  (authentication + RBAC)
-- ---------------------------------------------------------------------
CREATE TABLE users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL COMMENT 'PBKDF2WithHmacSHA256 iterations$salt$hash; never plaintext',
    full_name      VARCHAR(100) NOT NULL,
    role           VARCHAR(40)  NOT NULL COMMENT 'NETWORK_ADMIN | NOC_ENGINEER | OPTIMIZATION_ENGINEER | CLOUD_ADMIN | EDGE_ADMIN',
    enabled        TINYINT(1)   NOT NULL DEFAULT 1,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_role CHECK (role IN
      ('NETWORK_ADMIN','NOC_ENGINEER','OPTIMIZATION_ENGINEER','CLOUD_ADMIN','EDGE_ADMIN'))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2. base_stations
-- ---------------------------------------------------------------------
CREATE TABLE base_stations (
    station_id     VARCHAR(20)  PRIMARY KEY COMMENT 'e.g. BS-001',
    cell_name      VARCHAR(100) NOT NULL UNIQUE,
    location       VARCHAR(200) NOT NULL,
    frequency_band VARCHAR(30)  NOT NULL COMMENT 'e.g. 1800 MHz, 3.5 GHz, 28 GHz',
    technology     VARCHAR(10)  NOT NULL COMMENT '4G | 5G | 6G',
    capacity_mbps  INT          NOT NULL COMMENT 'max throughput capacity in Mbps',
    current_load_mbps INT       NOT NULL DEFAULT 0 COMMENT 'current offered load in Mbps',
    power_status   VARCHAR(10)  NOT NULL DEFAULT 'ON' COMMENT 'ON | OFF | SLEEP',
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_bs_tech CHECK (technology IN ('4G','5G','6G')),
    CONSTRAINT chk_bs_power CHECK (power_status IN ('ON','OFF','SLEEP')),
    CONSTRAINT chk_bs_capacity CHECK (capacity_mbps > 0),
    CONSTRAINT chk_bs_load CHECK (current_load_mbps >= 0)
) ENGINE=InnoDB;
CREATE INDEX idx_bs_tech ON base_stations(technology);

-- ---------------------------------------------------------------------
-- 3. subscribers
-- ---------------------------------------------------------------------
CREATE TABLE subscribers (
    subscriber_id  VARCHAR(20)  PRIMARY KEY COMMENT 'e.g. SUB-00001',
    name           VARCHAR(100) NOT NULL,
    service_plan   VARCHAR(50)  NOT NULL COMMENT 'BASIC | STANDARD | PREMIUM | ENTERPRISE | IOT',
    qos_level      TINYINT      NOT NULL DEFAULT 3 COMMENT '1=highest ... 5=lowest',
    connected_cell VARCHAR(20)  NULL COMMENT 'FK base_stations.station_id (NULL = not attached)',
    data_usage_mb  INT          NOT NULL DEFAULT 0,
    joined_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_sub_qos CHECK (qos_level BETWEEN 1 AND 5),
    CONSTRAINT chk_sub_usage CHECK (data_usage_mb >= 0),
    CONSTRAINT fk_sub_cell FOREIGN KEY (connected_cell) REFERENCES base_stations(station_id) ON DELETE SET NULL
) ENGINE=InnoDB;
CREATE INDEX idx_sub_cell ON subscribers(connected_cell);

-- ---------------------------------------------------------------------
-- 4. devices
-- ---------------------------------------------------------------------
CREATE TABLE devices (
    device_id       VARCHAR(20) PRIMARY KEY COMMENT 'e.g. DEV-00001',
    subscriber_id   VARCHAR(20) NOT NULL,
    device_type     VARCHAR(30) NOT NULL COMMENT 'SMARTPHONE | CPE | IOT_SENSOR | ROUTER | TABLET | WEARABLE',
    signal_strength_dbm INT     NOT NULL DEFAULT -80 COMMENT 'RSRP in dBm, typically -140..-44',
    connected_cell  VARCHAR(20) NULL,
    status          VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE | IDLE | OFFLINE',
    registered_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_dev_signal CHECK (signal_strength_dbm BETWEEN -160 AND 0),
    CONSTRAINT chk_dev_status CHECK (status IN ('ACTIVE','IDLE','OFFLINE')),
    CONSTRAINT fk_dev_sub  FOREIGN KEY (subscriber_id)  REFERENCES subscribers(subscriber_id) ON DELETE CASCADE,
    CONSTRAINT fk_dev_cell FOREIGN KEY (connected_cell) REFERENCES base_stations(station_id)   ON DELETE SET NULL
) ENGINE=InnoDB;
CREATE INDEX idx_dev_sub ON devices(subscriber_id);

-- ---------------------------------------------------------------------
-- 5. network_slices
-- ---------------------------------------------------------------------
CREATE TABLE network_slices (
    slice_id            VARCHAR(20)   PRIMARY KEY COMMENT 'e.g. SLICE-eMBB-01',
    slice_type          VARCHAR(30)   NOT NULL COMMENT 'eMBB | URLLC | mMTC',
    bandwidth_allocation_mbps INT     NOT NULL,
    priority            TINYINT       NOT NULL DEFAULT 2 COMMENT '1=highest ... 5=lowest',
    latency_requirement_ms DECIMAL(6,2) NOT NULL,
    resource_allocation_pct DECIMAL(5,2) NOT NULL COMMENT 'percent of total pool allocated',
    status              VARCHAR(10)   NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_slice_type CHECK (slice_type IN ('eMBB','URLLC','mMTC')),
    CONSTRAINT chk_slice_bw CHECK (bandwidth_allocation_mbps > 0),
    CONSTRAINT chk_slice_prio CHECK (priority BETWEEN 1 AND 5),
    CONSTRAINT chk_slice_lat CHECK (latency_requirement_ms > 0),
    CONSTRAINT chk_slice_alloc CHECK (resource_allocation_pct BETWEEN 0 AND 100)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 6. edge_servers
-- ---------------------------------------------------------------------
CREATE TABLE edge_servers (
    server_id        VARCHAR(20)  PRIMARY KEY COMMENT 'e.g. EDGE-01',
    site_location    VARCHAR(200) NOT NULL,
    cpu_utilization_pct  DECIMAL(5,2) NOT NULL DEFAULT 0,
    memory_used_gb   DECIMAL(6,2) NOT NULL DEFAULT 0,
    memory_total_gb  DECIMAL(6,2) NOT NULL,
    storage_total_tb DECIMAL(6,2) NOT NULL,
    storage_used_tb  DECIMAL(6,2) NOT NULL DEFAULT 0,
    running_apps     VARCHAR(500) NULL COMMENT 'comma separated application names',
    edge_load_pct    DECIMAL(5,2) NOT NULL DEFAULT 0,
    status           VARCHAR(10)  NOT NULL DEFAULT 'ONLINE' COMMENT 'ONLINE | OFFLINE | MAINTENANCE',
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_edge_cpu CHECK (cpu_utilization_pct BETWEEN 0 AND 100),
    CONSTRAINT chk_edge_mem CHECK (memory_used_gb >= 0 AND memory_used_gb <= memory_total_gb AND memory_total_gb > 0),
    CONSTRAINT chk_edge_sto CHECK (storage_used_tb >= 0 AND storage_used_tb <= storage_total_tb AND storage_total_tb > 0),
    CONSTRAINT chk_edge_load CHECK (edge_load_pct BETWEEN 0 AND 100),
    CONSTRAINT chk_edge_status CHECK (status IN ('ONLINE','OFFLINE','MAINTENANCE'))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 7. traffic_flows
-- ---------------------------------------------------------------------
CREATE TABLE traffic_flows (
    flow_id        VARCHAR(20)  PRIMARY KEY COMMENT 'e.g. FLOW-0001',
    source_node    VARCHAR(30)  NOT NULL COMMENT 'station/slice/edge id or region name',
    dest_node      VARCHAR(30)  NOT NULL,
    bandwidth_mbps DECIMAL(8,2) NOT NULL,
    latency_ms     DECIMAL(8,2) NOT NULL,
    packet_loss_pct DECIMAL(5,2) NOT NULL DEFAULT 0,
    throughput_mbps DECIMAL(8,2) NOT NULL DEFAULT 0,
    congestion_status VARCHAR(10) NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL | HIGH | CRITICAL',
    recorded_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_tf_bw CHECK (bandwidth_mbps > 0),
    CONSTRAINT chk_tf_lat CHECK (latency_ms >= 0),
    CONSTRAINT chk_tf_loss CHECK (packet_loss_pct BETWEEN 0 AND 100),
    CONSTRAINT chk_tf_cong CHECK (congestion_status IN ('NORMAL','HIGH','CRITICAL'))
) ENGINE=InnoDB;
CREATE INDEX idx_tf_src ON traffic_flows(source_node);

-- ---------------------------------------------------------------------
-- 8. resource_allocations
-- ---------------------------------------------------------------------
CREATE TABLE resource_allocations (
    allocation_id   INT AUTO_INCREMENT PRIMARY KEY,
    resource_type   VARCHAR(20) NOT NULL COMMENT 'CPU | MEMORY | SPECTRUM | POWER | USER',
    target_entity   VARCHAR(30) NOT NULL COMMENT 'station/slice/server id',
    allocated_value DECIMAL(10,2) NOT NULL COMMENT 'unit depends on type: %, GB, MHz, kW, count',
    unit            VARCHAR(10) NOT NULL,
    max_value       DECIMAL(10,2) NOT NULL COMMENT 'capacity ceiling for this target',
    allocated_by    INT NOT NULL,
    allocated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_ra_type CHECK (resource_type IN ('CPU','MEMORY','SPECTRUM','POWER','USER')),
    CONSTRAINT chk_ra_value CHECK (allocated_value >= 0 AND allocated_value <= max_value AND max_value > 0),
    CONSTRAINT fk_ra_user FOREIGN KEY (allocated_by) REFERENCES users(user_id)
) ENGINE=InnoDB;
CREATE INDEX idx_ra_target ON resource_allocations(target_entity, resource_type);

-- ---------------------------------------------------------------------
-- 9. optimization_jobs
-- ---------------------------------------------------------------------
CREATE TABLE optimization_jobs (
    job_id        INT AUTO_INCREMENT PRIMARY KEY,
    job_name      VARCHAR(100) NOT NULL,
    algorithm     VARCHAR(60)  NOT NULL DEFAULT 'LOCAL_GREEDY_SIMULATION'
                  COMMENT 'Deterministic local heuristic SIMULATION. NOT a quantum algorithm, NOT QpiAI Opt.',
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING | RUNNING | COMPLETED | FAILED',
    input_csv_path VARCHAR(255) NULL,
    scope         VARCHAR(60)  NULL COMMENT 'e.g. BASE_STATION_LOAD_BALANCING',
    submitted_by  INT          NOT NULL,
    started_at    TIMESTAMP    NULL,
    finished_at   TIMESTAMP    NULL,
    error_message VARCHAR(500) NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_job_status CHECK (status IN ('PENDING','RUNNING','COMPLETED','FAILED')),
    CONSTRAINT fk_job_user FOREIGN KEY (submitted_by) REFERENCES users(user_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 10. optimization_results
-- ---------------------------------------------------------------------
CREATE TABLE optimization_results (
    result_id      INT AUTO_INCREMENT PRIMARY KEY,
    job_id         INT NOT NULL,
    entity_id      VARCHAR(30) NOT NULL COMMENT 'affected station/slice/server id',
    metric         VARCHAR(40) NOT NULL COMMENT 'e.g. LOAD_PCT, CPU_PCT',
    before_value   DECIMAL(10,2) NOT NULL,
    after_value    DECIMAL(10,2) NOT NULL,
    recommendation VARCHAR(500) NULL,
    generated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_res_job FOREIGN KEY (job_id) REFERENCES optimization_jobs(job_id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_res_job ON optimization_results(job_id);

-- ---------------------------------------------------------------------
-- 11. network_kpis
-- ---------------------------------------------------------------------
CREATE TABLE network_kpis (
    kpi_id             INT AUTO_INCREMENT PRIMARY KEY,
    kpi_name           VARCHAR(60) NOT NULL COMMENT 'UTILIZATION | LATENCY | THROUGHPUT | PACKET_LOSS | ENERGY | ACTIVE_USERS | ACTIVE_BS',
    kpi_value          DECIMAL(12,2) NOT NULL,
    unit               VARCHAR(20) NOT NULL,
    is_simulated       TINYINT(1) NOT NULL DEFAULT 0 COMMENT '1 = value partly simulated, 0 = computed from stored data',
    snapshot_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_kpi_sim CHECK (is_simulated IN (0,1))
) ENGINE=InnoDB;
CREATE INDEX idx_kpi_name ON network_kpis(kpi_name, snapshot_at);

-- ---------------------------------------------------------------------
-- 12. alerts
-- ---------------------------------------------------------------------
CREATE TABLE alerts (
    alert_id     INT AUTO_INCREMENT PRIMARY KEY,
    alert_type   VARCHAR(40) NOT NULL COMMENT 'CELL_LOAD | LATENCY | EDGE_CPU | SLICE_LATENCY | DEVICE_OFFLINE',
    severity     VARCHAR(10) NOT NULL DEFAULT 'WARNING' COMMENT 'INFO | WARNING | CRITICAL',
    entity_id    VARCHAR(30) NOT NULL,
    message      VARCHAR(300) NOT NULL,
    threshold    DECIMAL(10,2) NULL,
    actual_value DECIMAL(10,2) NULL,
    status       VARCHAR(10) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN | ACKNOWLEDGED | CLOSED',
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_by INT NULL,
    CONSTRAINT chk_alert_sev CHECK (severity IN ('INFO','WARNING','CRITICAL')),
    CONSTRAINT chk_alert_status CHECK (status IN ('OPEN','ACKNOWLEDGED','CLOSED')),
    CONSTRAINT fk_alert_user FOREIGN KEY (acknowledged_by) REFERENCES users(user_id),
    -- one OPEN alert per (type, entity): prevents uncontrolled duplicates
    UNIQUE KEY uq_alert_open (alert_type, entity_id, status)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 13. reports
-- ---------------------------------------------------------------------
CREATE TABLE reports (
    report_id    INT AUTO_INCREMENT PRIMARY KEY,
    report_type  VARCHAR(50) NOT NULL COMMENT 'NETWORK_UTILIZATION | TRAFFIC | CELL_LOAD | EDGE_RESOURCES | QOS | ENERGY | OPTIMIZATION_RECOMMENDATIONS | DAILY_PERFORMANCE',
    title        VARCHAR(150) NOT NULL,
    csv_path     VARCHAR(255) NULL COMMENT 'exported CSV file location',
    summary      VARCHAR(1000) NULL,
    generated_by INT NOT NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rep_user FOREIGN KEY (generated_by) REFERENCES users(user_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 14. login_log  (never stores passwords)
-- ---------------------------------------------------------------------
CREATE TABLE login_log (
    log_id     INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50) NOT NULL,
    event      VARCHAR(20) NOT NULL COMMENT 'LOGIN_SUCCESS | LOGIN_FAILURE | LOGOUT | PASSWORD_CHANGE',
    source_ip  VARCHAR(45) NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_login_event CHECK (event IN ('LOGIN_SUCCESS','LOGIN_FAILURE','LOGOUT','PASSWORD_CHANGE'))
) ENGINE=InnoDB;
CREATE INDEX idx_login_user ON login_log(username, occurred_at);

-- ---------------------------------------------------------------------
-- 15. audit_log  (user actions; never secrets)
-- ---------------------------------------------------------------------
CREATE TABLE audit_log (
    audit_id   INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NULL,
    action     VARCHAR(60) NOT NULL COMMENT 'e.g. CREATE_BASE_STATION, EXPORT_CSV',
    entity_type VARCHAR(40) NOT NULL,
    entity_id  VARCHAR(40) NULL,
    details    VARCHAR(500) NULL COMMENT 'sanitized description; no passwords/tokens',
    performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;
CREATE INDEX idx_audit_time ON audit_log(performed_at);

-- ---------------------------------------------------------------------
-- 16. alert_thresholds (configuration table used by the alert engine)
-- ---------------------------------------------------------------------
CREATE TABLE alert_thresholds (
    threshold_id INT AUTO_INCREMENT PRIMARY KEY,
    alert_type   VARCHAR(40) NOT NULL UNIQUE,
    warn_value   DECIMAL(10,2) NOT NULL,
    crit_value   DECIMAL(10,2) NOT NULL,
    enabled      TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT chk_thr CHECK (warn_value <= crit_value)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 17. slice_station_map (junction: which stations serve which slice)
-- ---------------------------------------------------------------------
CREATE TABLE slice_station_map (
    slice_id   VARCHAR(20) NOT NULL,
    station_id VARCHAR(20) NOT NULL,
    PRIMARY KEY (slice_id, station_id),
    CONSTRAINT fk_ssm_slice FOREIGN KEY (slice_id)   REFERENCES network_slices(slice_id) ON DELETE CASCADE,
    CONSTRAINT fk_ssm_station FOREIGN KEY (station_id) REFERENCES base_stations(station_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- SAMPLE DATA
-- =====================================================================

-- Bootstrap: NO password/hash is stored in this file (no credentials committed).
-- After loading the schema, run the one-time setup tool which interactively
-- creates the local NETWORK_ADMIN account with a PBKDF2-hashed password:
--   java -cp "app/target/qitrom-app.jar:app/target/lib/*" com.abctelecom.nms.SetupBootstrapAdmin
-- See README "Bootstrap login setup".

INSERT INTO base_stations (station_id, cell_name, location, frequency_band, technology, capacity_mbps, current_load_mbps, power_status) VALUES
 ('BS-001','MG-Road-1','Bangalore MG Road','3500 MHz','5G',1000,720,'ON'),
 ('BS-002','Indiranagar-2','Bangalore Indiranagar','1800 MHz','4G',600,540,'ON'),
 ('BS-003','Whitefield-3','Bangalore Whitefield','28 GHz','5G',2000,900,'ON'),
 ('BS-004','Electronic-City-4','Bangalore Electronic City','700 MHz','4G',400,380,'ON'),
 ('BS-005','Koramangala-5','Bangalore Koramangala','3500 MHz','5G',1200,300,'OFF'),
 ('BS-006','Hebbal-6','Bangalore Hebbal','6 GHz (sub-6)','6G',3000,1100,'ON');

INSERT INTO subscribers (subscriber_id, name, service_plan, qos_level, connected_cell, data_usage_mb) VALUES
 ('SUB-00001','Ravi Kumar','PREMIUM',2,'BS-001',15400),
 ('SUB-00002','Anita Shah','STANDARD',3,'BS-001',6200),
 ('SUB-00003','Mohammed Irfan','ENTERPRISE',1,'BS-003',48000),
 ('SUB-00004','Priya Nair','BASIC',4,'BS-002',2100),
 ('SUB-00005','Karthik Reddy','IOT',5,'BS-004',180),
 ('SUB-00006','Sneha Patil','PREMIUM',2,'BS-006',22000),
 ('SUB-00007','Arjun Mehta','STANDARD',3,NULL,900);

INSERT INTO devices (device_id, subscriber_id, device_type, signal_strength_dbm, connected_cell, status) VALUES
 ('DEV-00001','SUB-00001','SMARTPHONE',-72,'BS-001','ACTIVE'),
 ('DEV-00002','SUB-00002','CPE',-85,'BS-001','ACTIVE'),
 ('DEV-00003','SUB-00003','ROUTER',-68,'BS-003','ACTIVE'),
 ('DEV-00004','SUB-00004','TABLET',-95,'BS-002','IDLE'),
 ('DEV-00005','SUB-00005','IOT_SENSOR',-110,'BS-004','ACTIVE'),
 ('DEV-00006','SUB-00006','WEARABLE',-88,'BS-006','OFFLINE');

INSERT INTO network_slices (slice_id, slice_type, bandwidth_allocation_mbps, priority, latency_requirement_ms, resource_allocation_pct) VALUES
 ('SLICE-eMBB-01','eMBB',4000,2,30.00,35.00),
 ('SLICE-URLLC-01','URLLC',800,1,1.00,20.00),
 ('SLICE-mMTC-01','mMTC',1500,3,100.00,25.00);

INSERT INTO edge_servers (server_id, site_location, cpu_utilization_pct, memory_used_gb, memory_total_gb, storage_total_tb, storage_used_tb, running_apps, edge_load_pct, status) VALUES
 ('EDGE-01','Bangalore East DC',78.50,42.0,64.0,10.0,6.2,'video-cache;ran-scheduler',74.00,'ONLINE'),
 ('EDGE-02','Bangalore West DC',45.20,20.5,64.0,10.0,3.1,'iot-aggregator',41.00,'ONLINE'),
 ('EDGE-03','Mysuru Edge PoP',91.30,58.0,64.0,8.0,5.9,'ar-render;cdn',88.00,'MAINTENANCE');

INSERT INTO traffic_flows (flow_id, source_node, dest_node, bandwidth_mbps, latency_ms, packet_loss_pct, throughput_mbps, congestion_status) VALUES
 ('FLOW-0001','BS-001','EDGE-01',450.00,12.50,0.40,430.00,'NORMAL'),
 ('FLOW-0002','BS-002','EDGE-01',380.00,25.00,1.20,350.00,'HIGH'),
 ('FLOW-0003','BS-003','EDGE-02',900.00,8.00,0.10,890.00,'NORMAL'),
 ('FLOW-0004','BS-004','EDGE-03',300.00,60.00,3.50,250.00,'CRITICAL'),
 ('FLOW-0005','BS-006','EDGE-02',1200.00,5.00,0.05,1190.00,'NORMAL');

INSERT INTO resource_allocations (resource_type, target_entity, allocated_value, unit, max_value, allocated_by) VALUES
 ('SPECTRUM','BS-001',80.00,'MHz',100.00,1),
 ('CPU','EDGE-01',60.00,'pct',100.00,1),
 ('MEMORY','EDGE-01',48.00,'GB',64.00,1),
 ('POWER','BS-004',30.00,'kW',35.00,1),
 ('USER','BS-002',500.00,'users',520.00,1);

INSERT INTO alert_thresholds (alert_type, warn_value, crit_value) VALUES
 ('CELL_LOAD',80.00,95.00),
 ('LATENCY',50.00,100.00),
 ('EDGE_CPU',75.00,90.00),
 ('SLICE_LATENCY',30.00,60.00);


-- ---------------------------------------------------------------------
-- Post-bootstrap sample data (requires a users row; run AFTER SetupBootstrapAdmin,
-- or let the setup tool insert it automatically):
-- ---------------------------------------------------------------------
-- INSERT INTO optimization_jobs (job_name, algorithm, status, scope, submitted_by, started_at, finished_at)
-- VALUES ('Sample completed job','LOCAL_GREEDY_SIMULATION','COMPLETED','BASE_STATION_LOAD_BALANCING',1,NOW(),NOW());
-- INSERT INTO optimization_results (job_id, entity_id, metric, before_value, after_value, recommendation) VALUES
--  (1,'BS-002','LOAD_PCT',90.00,72.50,'Migrate 105 Mbps of best-effort traffic to BS-003 (load 45%)'),
--  (1,'BS-004','LOAD_PCT',95.00,80.00,'Enable carrier aggregation; shed non-GBR bearers during peak');
