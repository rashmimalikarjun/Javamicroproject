package com.abctelecom.nms.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.*;

/** Central JUL logging configuration: console + rotating file under logs/qitrom.log. */
public final class LogConfig {
    private static boolean configured = false;
    private LogConfig() {}

    public static synchronized void init() {
        if (configured) return;
        Logger root = Logger.getLogger("");
        for (Handler h : root.getHandlers()) root.removeHandler(h);

        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.INFO);
        ch.setFormatter(new SimpleFormatter());
        root.addHandler(ch);
        try {
            Path dir = Path.of("logs");
            Files.createDirectories(dir);
            FileHandler fh = new FileHandler("logs/qitrom.log", 2_000_000, 3, true);
            fh.setLevel(Level.FINE);
            fh.setFormatter(new SimpleFormatter());
            root.addHandler(fh);
        } catch (IOException | SecurityException e) {
            System.err.println("File logging disabled: " + e.getMessage());
        }
        root.setLevel(Level.INFO);
        configured = true;
    }

    public static Logger get(Class<?> c) {
        init();
        return Logger.getLogger(c.getName());
    }
}
