package com.abctelecom.nms.view;

import com.abctelecom.nms.model.User;
import com.abctelecom.nms.service.AccessControl.Capability;
import com.abctelecom.nms.service.AuthService;

import javax.swing.JFrame;
import javax.swing.SwingWorker;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Shared application state for the Swing UI (session principal, main frame,
 * background executor). Views never touch the database directly on the EDT;
 * they use {@link #bg} via {@link #async}.
 */
public final class AppContext {
    private static final AppContext INSTANCE = new AppContext();
    public static AppContext get() { return INSTANCE; }

    private final AuthService auth = new AuthService();
    private final Executor bgExecutor = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "qitrom-bg");
        t.setDaemon(true);
        return t;
    });
    private JFrame mainFrame;

    private AppContext() {}

    public AuthService auth() { return auth; }
    public User currentUser() { return auth.getCurrentUser(); }
    public boolean can(Capability cap) { return AccessControlCheck.can(currentUser(), cap); }

    public JFrame mainFrame() { return mainFrame; }
    public void setMainFrame(JFrame f) { this.mainFrame = f; }

    /** Run blocking work off the EDT, deliver result or error back on the EDT. */
    public <T> void async(java.util.concurrent.Callable<T> task,
                          Consumer<T> onSuccess, Consumer<Throwable> onError) {
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return task.call(); }
            @Override protected void done() {
                try { onSuccess.accept(get()); }
                catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    onError.accept(cause);
                }
            }
        }.execute(); // default executor is a shared Swing pool: EDT stays free
    }

    /** Small indirection so views don't import AccessControl statically everywhere. */
    static final class AccessControlCheck {
        static boolean can(User u, Capability c) {
            return com.abctelecom.nms.service.AccessControl.can(u, c);
        }
    }
}
