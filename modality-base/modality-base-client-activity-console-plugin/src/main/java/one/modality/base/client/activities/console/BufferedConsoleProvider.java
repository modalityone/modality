package one.modality.base.client.activities.console;

import dev.webfx.platform.console.spi.ConsoleProvider;
import dev.webfx.platform.useragent.UserAgent;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Bruno Salmon
 */
public final class BufferedConsoleProvider implements ConsoleProvider {

    private static final StringBuilder CONSOLE_BUFFER = new StringBuilder();
    private static Runnable LISTENER;

    public BufferedConsoleProvider() {
        if (!UserAgent.isBrowser()) {
            PrintStream standardOut = System.out;
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                    CONSOLE_BUFFER.append((char) b);
                    standardOut.write(b);
                    if (LISTENER != null)
                        LISTENER.run();
                }
            }));
            PrintStream standardErr = System.err;
            System.setErr(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                    CONSOLE_BUFFER.append((char) b);
                    standardErr.write(b);
                    if (LISTENER != null)
                        LISTENER.run();
                }
            }));
        }
    }

    public static void setListener(Runnable listener) {
        LISTENER = listener;
    }

    public static String getBufferContent() {
        return CONSOLE_BUFFER.toString();
    }

    @Override
    public void log(String message, Throwable error) {
        if (!UserAgent.isBrowser()) {
            ConsoleProvider.super.log(message, error);
        } else {
            if (message != null)
                CONSOLE_BUFFER.append(message).append("\n");
/*
            if (error != null)
                error.printStackTrace(new PrintWriter(new ));
*/
            if (LISTENER != null)
                LISTENER.run();
        }
    }

    @Override
    public void logNative(Object nativeObject) {
        log(nativeObject);
    }
}
