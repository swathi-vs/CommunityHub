package utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class Log {

	private static final Logger LOGGER = Logger.getLogger("CommunityHub");

	private Log() {
	}

	public static void error(Throwable t) {
		LOGGER.log(Level.SEVERE, t.getMessage(), t);
	}

	public static void error(String message, Throwable t) {
		LOGGER.log(Level.SEVERE, message, t);
	}

	public static void warn(String message) {
		LOGGER.warning(message);
	}
}
