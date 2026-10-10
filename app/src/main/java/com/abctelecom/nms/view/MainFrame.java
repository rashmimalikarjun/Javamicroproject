package com.abctelecom.nms.view;

import com.abctelecom.nms.model.User;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;

/**
 * Shared navigation shell (MVC "controller" side of the view layer).
 * Uses CardLayout for the 18 screens; menu buttons drive navigation.
 * All DB work happens inside panels via AppContext.async (never on the EDT).
 */
public class MainFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel userLabel = new JLabel(" ");
    private static final String[] NAV = {
        "Dashboard", "Base Stations", "Subscribers", "Devices", "Slices", "Edge Servers",
        "Allocation", "Traffic", "KPIs", "Optimize", "Results", "Reports",
        "Users", "Backup", "Settings", "About"
    };
    private static final String[] CARDS = {
        "dashboard", "stations", "subscribers", "devices", "slices", "edge",
        "allocation", "traffic", "kpi", "optreq", "optres", "reports",
        "users", "backup", "settings", "about"
    };

    public MainFrame() {
        super("QITROM - Quantum-Inspired Telecom NMS (simulated optimizer)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1240, 760);
        setLocationByPlatform(true);
        AppContext.get().setMainFrame(this);

        JPanel top = new JPanel(new BorderLayout());
        JPanel navBar = new JPanel(new GridLayout(2, 8, 2, 2));
        for (int i = 0; i < NAV.length; i++) {
            JButton b = new JButton(NAV[i]);
            final int idx = i;
            b.addActionListener(e -> show(CARDS[idx]));
            navBar.add(b);
        }
        JPanel userBar = new JPanel(new BorderLayout());
        userBar.add(userLabel, BorderLayout.WEST);
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> doLogout());
        userBar.add(logout, BorderLayout.EAST);
        top.add(navBar, BorderLayout.CENTER);
        top.add(userBar, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        content.add(new DashboardPanel(), "dashboard");
        content.add(new BaseStationPanel(), "stations");
        content.add(new SubscriberPanel(), "subscribers");
        content.add(new DevicePanel(), "devices");
        content.add(new SlicePanel(), "slices");
        content.add(new EdgeServerPanel(), "edge");
        content.add(new AllocationPanel(), "allocation");
        content.add(new TrafficPanel(), "traffic");
        content.add(new KpiPanel(), "kpi");
        content.add(new OptimizationRequestPanel(), "optreq");
        content.add(new OptimizationResultsPanel(), "optres");
        content.add(new ReportsPanel(), "reports");
        content.add(new UserPanel(), "users");
        content.add(new BackupPanel(), "backup");
        content.add(new SettingsPanel(), "settings");
        content.add(new AboutPanel(), "about");
        add(content, BorderLayout.CENTER);
    }

    /** Navigate to a card and trigger its data load (onShow). */
    public void show(String card) {
        cards.show(content, card);
        for (java.awt.Component c : content.getComponents())
            if (c instanceof BasePanel bp) {
                // only fire onShow for the now-visible panel: find via layout constraint is overkill;
                // BasePanel.onShow is idempotent and cheap, so fire for visible card by name match.
            }
        java.awt.Component visible = visibleCard(card);
        if (visible instanceof BasePanel bp) bp.onShow();
    }

    private java.awt.Component visibleCard(String card) {
        for (java.awt.Component c : content.getComponents())
            if (nameOf(c).equals(card)) return c;
        return null;
    }

    private static String nameOf(java.awt.Component c) {
        // CardLayout stores the name as the constraints key; we registered names in order.
        for (int i = 0; i < CARDS.length; i++)
            if (content.getComponent(i) == c) return CARDS[i];
        return "?";
    }

    public void setLoggedIn(User u) {
        userLabel.setText("Signed in: " + u.getUsername() + " [" + u.getRole().name() + "]");
        show("dashboard");
    }

    private void doLogout() {
        AppContext.get().auth().logout();
        SwingUtilities.invokeLater(() -> {
            dispose();
            Launcher.showLogin();
        });
    }
}
