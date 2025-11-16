package edu.fiu.cop6727.webserver;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Minimal logger for console output with timestamps and thread names.
 */
public final class ServerLogger {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS z");

    public void info(String message) {
        log("INFO", message, null);
    }

    public void warn(String message) {
        log("WARN", message, null);
    }

    public void error(String message, Throwable error) {
        log("ERROR", message, error);
    }

    private void log(String level, String message, Throwable error) {
        String timestamp = FORMATTER.format(ZonedDateTime.now());
        String threadName = Thread.currentThread().getName();
        String line = String.format("[%s] [%s] [%s] %s", timestamp, level, threadName, message);
        if ("ERROR".equals(level)) {
            System.err.println(line);
        } else {
            System.out.println(line);
        }
        if (error != null) {
            error.printStackTrace("ERROR".equals(level) ? System.err : System.out);
        }
    }
}
