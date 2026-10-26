package ganymedes01.etfuturum.core.utils;

import org.apache.logging.log4j.LogManager;

public final class Logger {
    private Logger() { }
    public static void info(String message) { LogManager.getLogger("UAB").info(message); }
    public static void warn(String message) { LogManager.getLogger("UAB").warn(message); }
    public static void error(String message) { LogManager.getLogger("UAB").error(message); }
}
