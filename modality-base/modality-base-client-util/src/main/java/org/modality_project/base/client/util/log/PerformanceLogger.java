package org.modality_project.base.client.util.log;

import dev.webfx.platform.console.Console;

/**
 * @author Bruno Salmon
 */
public final class PerformanceLogger {

    private long t0 = System.currentTimeMillis();

    public void log(String message) {
        long t1 = System.currentTimeMillis();
        Console.log(message + ": " + (t1 - t0) + "ms");
        t0 = t1;
    }
}
