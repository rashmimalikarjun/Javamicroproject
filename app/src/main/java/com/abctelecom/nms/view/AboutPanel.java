package com.abctelecom.nms.view;

import com.abctelecom.nms.config.AppConfig;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/** Screen 18: About - project identity, honest scope statement and environment. */
public class AboutPanel extends JPanel {
    public AboutPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        add(new JLabel("<html><b style='font-size:15px'>About QITROM</b></html>"), BorderLayout.NORTH);
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(label("<html><br><b>QITROM</b> v1.0 &mdash; Quantum-Inspired Telecom Network Resource<br>" +
                "Optimization and Management System (ABC Telecom Pvt. Ltd.)<br><br>" +
                "<b>Honest scope statement:</b> the optimizer is a <i>deterministic local heuristic<br>" +
                "SIMULATION</i> (LocalGreedyOptimizer v1). It is NOT real quantum computing and it does NOT<br>" +
                "call any external QpiAI Opt endpoint. A pluggable {@code NetworkOptimizer} interface is<br>" +
                "provided as the future integration slot.<br><br>" +
                "Modules: core (domain logic), app (this Swing desktop client), web (Servlet/JSP preview),<br>" +
                "database (MySQL schema + seed data: 17 tables).<br><br>" +
                "Stack: Java 17, Maven multi-module, MySQL 8 (JDBC), JUnit 5, SLF4J-simple logging,<br>" +
                "PBKDF2-HMAC-SHA256 password hashing, role-based access control (3 roles).<br><br>" +
                "Data directory: <tt>" + esc(AppConfig.getDataDir()) + "</tt><br>" +
                "Logs: <tt>logs/qitrom.log</tt><br>" +
                "&copy; 2026 ABC Telecom Pvt. Ltd. &mdash; academic/demo build.</html>"));
        body.add(Box.createVerticalGlue());
        add(body, BorderLayout.CENTER);
    }
    private static JLabel label(String html) {
        JLabel l = new JLabel(html);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }
    private static String esc(String s) { return s.replace("&", "&amp;").replace("<", "&lt;"); }
}
