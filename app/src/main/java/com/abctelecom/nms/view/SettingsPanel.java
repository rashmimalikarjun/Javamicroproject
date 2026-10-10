package com.abctelecom.nms.view;

import com.abctelecom.nms.config.AppConfig;
import com.abctelecom.nms.config.DbConnection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.BorderLayout;

/** Screen 16: Settings - environment info, DB connectivity test, change own password. */
public class SettingsPanel extends JPanel {
    public SettingsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        add(new JLabel("<html><b style='font-size:15px'>Settings</b></html>"), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel env = new JPanel(new BorderLayout(6, 4));
        env.setBorder(BorderFactory.createTitledBorder("Environment (read-only; edit config.properties)"));
        JLabel info = new JLabel("<html>DB URL: <tt>" + esc(AppConfig.getDbUrl()) + "</tt><br>" +
                "DB user: <tt>" + esc(AppConfig.getDbUser()) + "</tt> (password never displayed)<br>" +
                "Data directory: <tt>" + esc(AppConfig.getDataDir()) + "</tt><br>" +
                "Config file: <tt>config.properties</tt> (see config.properties.example)</html>");
        env.add(info, BorderLayout.CENTER);
        JButton test = new JButton("Test DB Connection");
        test.addActionListener(e -> AppContext.get().async(DbConnection::testConnection,
                ok -> JOptionPane.showMessageDialog(this, ok
                        ? "Database connection OK."
                        : "Database NOT reachable. Start MySQL and check config.properties / QITROM_DB_* env vars."),
                err -> JOptionPane.showMessageDialog(this, "Test failed: " + err.getMessage())));
        JPanel testBar = new JPanel();
        testBar.add(test);
        env.add(testBar, BorderLayout.SOUTH);
        env.setAlignmentX(LEFT_ALIGNMENT);
        body.add(env);
        body.add(Box.createVerticalStrut(10));

        JPanel pwd = new JPanel(new BorderLayout(6, 4));
        pwd.setBorder(BorderFactory.createTitledBorder("Change my password"));
        JPasswordField oldP = new JPasswordField(14);
        JPasswordField newP = new JPasswordField(14);
        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.add(rowL("Current:", oldP));
        fields.add(rowL("New (8+, letter+digit):", newP));
        JButton change = new JButton("Change Password");
        change.addActionListener(e -> AppContext.get().async(() -> {
                    AppContext.get().auth().changePassword(new String(oldP.getPassword()),
                                                            new String(newP.getPassword()));
                    return true;
                }, ok -> {
                    oldP.setText(""); newP.setText("");
                    JOptionPane.showMessageDialog(this, "Password updated (stored as PBKDF2 hash).");
                }, err -> JOptionPane.showMessageDialog(this,
                        com.abctelecom.nms.util.ErrorHelper.message(err))));
        pwd.add(fields, BorderLayout.CENTER);
        JPanel cb = new JPanel();
        cb.add(change);
        pwd.add(cb, BorderLayout.SOUTH);
        pwd.setAlignmentX(LEFT_ALIGNMENT);
        body.add(pwd);
        body.add(Box.createVerticalGlue());
        add(body, BorderLayout.CENTER);
    }

    private static String esc(String s) { return s.replace("&", "&amp;").replace("<", "&lt;"); }

    private JPanel rowL(String label, javax.swing.JComponent c) {
        JPanel p = new JPanel(new BorderLayout(4, 2));
        p.setMaximumSize(new java.awt.Dimension(360, 32));
        p.add(new JLabel(label), BorderLayout.WEST);
        p.add(c, BorderLayout.CENTER);
        return p;
    }
}
