package sumogame.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DebugLogger {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    private static boolean enabled = true;
    private static LogLevel minLevel = LogLevel.INFO;

    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    public static void log(String message) {
        log(message, LogLevel.INFO);
    }

    public static void debug(String message) {
        log(message, LogLevel.DEBUG);
    }

    public static void warn(String message) {
        log(message, LogLevel.WARN);
    }

    public static void error(String message) {
        log(message, LogLevel.ERROR);
    }

    private static void log(String message, LogLevel level) {
        if (!enabled || level.ordinal() < minLevel.ordinal()) {
            return;
        }

        String timestamp = sdf.format(new Date());
        String levelStr = "[" + level + "]";

        switch (level) {
            case ERROR:
                System.err.println(timestamp + " " + levelStr + " " + message);
                break;
            default:
                System.out.println(timestamp + " " + levelStr + " " + message);
                break;
        }
    }

    public static void enable() { enabled = true; }
    public static void disable() { enabled = false; }

    public static void setLogLevel(LogLevel level) {
        minLevel = level;
    }

    public static boolean isEnabled() {
        return enabled;
    }
}