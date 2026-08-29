package com.hydroplant.util;

import javax.swing.SwingUtilities;

/**
 * A background thread that periodically invokes a callback on a fixed
 * interval, marshalling the actual UI update back onto the Swing Event
 * Dispatch Thread via SwingUtilities.invokeLater. This demonstrates the
 * (optional) multithreading requirement for live dashboard updates
 * without blocking or corrupting Swing components from a non-EDT thread.
 */
public class DashboardUpdater extends Thread {

    /** Functional callback invoked on the EDT each refresh cycle. */
    public interface RefreshCallback {
        void refresh();
    }

    private final RefreshCallback callback;
    private final long intervalMillis;
    private volatile boolean running = true;

    public DashboardUpdater(RefreshCallback callback, long intervalMillis) {
        super("DashboardUpdaterThread");
        this.callback = callback;
        this.intervalMillis = intervalMillis;
        setDaemon(true); // don't prevent JVM shutdown
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            if (!running) break;
            SwingUtilities.invokeLater(callback::refresh);
        }
    }

    /** Signals the loop to stop after its current sleep completes. */
    public void stopUpdating() {
        running = false;
        this.interrupt();
    }
}
