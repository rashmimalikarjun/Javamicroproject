package com.abctelecom.nms.view;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;

/** Screen 1: Splash - product identity shown while the app initializes. */
public class SplashPanel extends JPanel {
    public SplashPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(20, 30, 60));
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        add(Box.createVerticalGlue());
        JLabel title = new JLabel("QITROM");
        title.setAlignmentX(CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(48f));
        add(title);
        JLabel sub = new JLabel("Quantum-Inspired Telecom Network Resource Optimization & Management");
        sub.setAlignmentX(CENTER_ALIGNMENT);
        sub.setForeground(new Color(200, 210, 235));
        add(sub);
        add(Box.createVerticalStrut(12));
        JLabel note = new JLabel("ABC Telecom Pvt. Ltd. - simulated platform. NOT real quantum computing.");
        note.setAlignmentX(CENTER_ALIGNMENT);
        note.setForeground(new Color(160, 170, 200));
        add(note);
        JLabel loading = new JLabel("Loading modules... (database is contacted only after login)");
        loading.setAlignmentX(CENTER_ALIGNMENT);
        loading.setForeground(new Color(160, 170, 200));
        add(loading);
        add(Box.createVerticalGlue());
        setPreferredSize(new Dimension(900, 560));
    }
}
