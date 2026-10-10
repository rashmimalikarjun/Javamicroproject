package com.abctelecom.nms.view;

import com.abctelecom.nms.util.LogConfig;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * Application entry point. Shows Splash -> Login -> MainFrame, all created
 * on the EDT via SwingUtilities.invokeLater. No DB access before login.
 */
public final class Launcher {
    private static JFrame authFrame;
    private static final CardLayout AUTH_CARDS = new CardLayout();

    public static void main(String[] args) {
        LogConfig.init();
        SwingUtilities.invokeLater(Launcher::showSplashThenLogin);
    }

    private static void showSplashThenLogin() {
        authFrame = new JFrame("QITROM - Sign in");
        authFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel p = new JPanel(AUTH_CARDS);
        p.add(new SplashPanel(), "splash");
        p.add(new LoginPanel(Launcher::openMain), "login");
        authFrame.setContentPane(p);
        authFrame.pack();
        center(authFrame);
        authFrame.setVisible(true);
        AUTH_CARDS.show(p, "splash");
        // brief splash pause, then hand over to login (Timer keeps EDT responsive)
        new javax.swing.Timer(1500, e -> {
            ((javax.swing.Timer) e.getSource()).stop();
            AUTH_CARDS.show(p, "login");
        }).start();
    }

    private static void openMain() {
        MainFrame mf = new MainFrame();
        mf.setLoggedIn(AppContext.get().currentUser());
        if (authFrame != null) authFrame.dispose();
        center(mf);
        mf.setVisible(true);
    }

    /** Called from MainFrame logout to bring up a fresh login window. */
    public static void showLogin() {
        authFrame = new JFrame("QITROM - Sign in");
        authFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        authFrame.setContentPane(new LoginPanel(Launcher::openMain));
        authFrame.pack();
        center(authFrame);
        authFrame.setVisible(true);
    }

    private static void center(JFrame f) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        f.setLocation((screen.width - f.getWidth()) / 2, (screen.height - f.getHeight()) / 2);
    }
}
