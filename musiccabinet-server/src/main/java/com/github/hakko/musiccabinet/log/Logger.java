package com.github.hakko.musiccabinet.log;

import static java.io.File.separator;

import org.apache.commons.lang.exception.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Logger implementation which logs to ${JAVA_TMP_DIR}/musiccabinet.log.
 * 
 * This is a modified version of the Logger class used in Subsonic, written by
 * Sindre Mehus.
 * 
 * During static initialization, previous log file is removed and a writer to a
 * new log file is set up. If the log file can't be written, it falls back to
 * writing to standard error.
 * 
 * Therefore, this Logger can't be used safely during static initialization of
 * other classes, unless class loading order is well defined.
 */
public class Logger {

	private String category;

	private static PrintWriter printWriter;
	private static String logFileLocation;
	
	static {
		String tmpDir = System.getProperty("java.io.tmpdir");
		if (!tmpDir.endsWith(separator)) {
			tmpDir = tmpDir + separator;
		}
		File logFile = new File(tmpDir, "musiccabinet.log");
		try {
			printWriter = new PrintWriter(logFile);
			logFileLocation = tmpDir + "musiccabinet.log";
		} catch (IOException e) {
			System.err.println("Could not write to musiccabinet.log!");
			e.printStackTrace(System.err);
			printWriter = new PrintWriter(System.err);
			logFileLocation = "stderr (" + tmpDir + " not accessible)";
		}
	}
	
	public static String getLogFileLocation() {
		return logFileLocation;
	}
	
	/**
	 * Creates a logger for the given class.
	 * 
	 * @param clazz
	 *            The class.
	 * @return A logger for the class.
	 */
	@SuppressWarnings("rawtypes")
	public static Logger getLogger(Class clazz) {
		return new Logger(clazz.getName());
	}

	/**
	 * Creates a logger for the given name.
	 * 
	 * @param name
	 *            The name.
	 * @return A logger for the name.
	 */
	public static Logger getLogger(String name) {
		return new Logger(name);
	}

	private Logger(String name) {
		int lastDot = name.lastIndexOf('.');
		if (lastDot == -1) {
			category = name;
		} else {
			category = name.substring(lastDot + 1);
		}
	}

	/**
	 * Logs a debug message.
	 * 
	 * @param message
	 *            The log message.
	 */
	public void debug(Object message) {
		debug(message, null);
	}

	/**
	 * Logs a debug message.
	 * 
	 * @param message
	 *            The message.
	 * @param error
	 *            The optional exception.
	 */
	public void debug(Object message, Throwable error) {
		add(Level.DEBUG, message, error);
	}

	/**
	 * Logs an info message.
	 * 
	 * @param message
	 *            The message.
	 */
	public void info(Object message) {
		info(message, null);
	}

	/**
	 * Logs an info message.
	 * 
	 * @param message
	 *            The message.
	 * @param error
	 *            The optional exception.
	 */
	public void info(Object message, Throwable error) {
		add(Level.INFO, message, error);
	}

	/**
	 * Logs a warning message.
	 * 
	 * @param message
	 *            The message.
	 */
	public void warn(Object message) {
		warn(message, null);
	}

	/**
	 * Logs a warning message.
	 * 
	 * @param message
	 *            The message.
	 * @param error
	 *            The optional exception.
	 */
	public void warn(Object message, Throwable error) {
		add(Level.WARN, message, error);
	}

	/**
	 * Logs an error message.
	 * 
	 * @param message
	 *            The message.
	 */
	public void error(Object message) {
		error(message, null);
	}

	/**
	 * Logs an error message.
	 * 
	 * @param message
	 *            The message.
	 * @param error
	 *            The optional exception.
	 */
	public void error(Object message, Throwable error) {
		add(Level.ERROR, message, error);
	}

	private void add(Level level, Object message, Throwable error) {
		Entry entry = new Entry(category, level, message, error);
		printWriter.println(entry);
		printWriter.flush();
	}

	/**
	 * Log level.
	 */
	public enum Level {
		DEBUG, INFO, WARN, ERROR
	}

	/**
	 * Log entry.
	 */
	public static class Entry {
		private String category;
		private Date date;
		private Level level;
		private Object message;
		private Throwable error;
		private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		public Entry(String category, Level level, Object message,
				Throwable error) {
			this.date = new Date();
			this.category = category;
			this.level = level;
			this.message = message;
			this.error = error;
		}

		public String getCategory() {
			return category;
		}

		public Date getDate() {
			return date;
		}

		public Level getLevel() {
			return level;
		}

		public Object getMessage() {
			return message;
		}

		public Throwable getError() {
			return error;
		}

		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append('[').append(DATE_FORMAT.format(date)).append("] ");
			buf.append(level).append(' ');
			buf.append(category).append(" - ");
			buf.append(message);

			if (error != null) {
				buf.append('\n')
						.append(ExceptionUtils.getFullStackTrace(error));
			}
			return buf.toString();
		}
	}

}