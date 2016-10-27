package com.projectbronze.pbbot.log;

import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.projectbronze.pbbot.Core;

public class LogStream extends PrintStream {
	public LogStream(PrintStream out) throws IOException {
		super(out);
		Logger.init();
	}

	private static final String INFO = "[INFO] ", DEBUG = "[DEBUG] ", WARNING = "[WARNING] ", ERROR = "[ERROR] ",
			FATAL = "[FATAL] ";
	private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	private static String getTime() {
		return "[" + timeFormat.format(Calendar.getInstance().getTime()) + "] ";
	}

	private static String getSender(boolean full) {
		StackTraceElement e = Thread.currentThread().getStackTrace()[3];
		if (e.getClassName().equals(LogStream.class.getName())) {
			e = Thread.currentThread().getStackTrace()[4];
		}
		return "[" + (full ? e : e.getClassName().substring(e.getClassName().lastIndexOf('.') + 1) + "#" + e.getLineNumber()) + "] ";
	}

	/**
	 * Should be used only internally, or by exceptions
	 */
	@Override
	@Deprecated
	public void print(String s) {
		Logger.log(s);
		super.print(s);
	}

	public void info(Object s) {
		println(getTime() + getSender(false) + INFO + s);
	}

	public void debug(Object s) {
		if (Core.debug)
			println(getTime() + getSender(true) + DEBUG + s);
	}

	public void warning(Object s) {
		println(getTime() + getSender(false) + WARNING + s);
	}

	public void error(Object s) {
		println(getTime() + getSender(false) + ERROR + s);
	}

	public void fatal(Object s, boolean exit, int exitcode, Throwable cause) {
		println(getTime() + getSender(false) + FATAL + s);
		if (exit)
			Core.exit(exitcode, cause);
	}

	public void info(String format, Object... s) {
		info(String.format(format, s));
	}

	public void debug(String format, Object... s) {
		debug(String.format(format, s));
	}

	public void warning(String format, Object... s) {
		warning(String.format(format, s));
	}

	public void error(String format, Object... s) {
		error(String.format(format, s));
	}

	public void fatal(String format, boolean exit, int exitcode, Throwable cause, Object... s) {
		fatal(String.format(format, s), exit, exitcode, cause);
	}

	public boolean canLog() {
		return Logger.canLog();
	}
}
