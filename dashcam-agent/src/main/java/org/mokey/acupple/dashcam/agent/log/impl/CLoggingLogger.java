package org.mokey.acupple.dashcam.agent.log.impl;

import org.mokey.acupple.dashcam.agent.log.ILog;
import org.mokey.acupple.dashcam.agent.log.ILogSender;
import org.mokey.acupple.dashcam.common.models.thrift.LogEvent;
import org.mokey.acupple.dashcam.common.models.thrift.LogLevel;
import org.mokey.acupple.dashcam.common.models.thrift.LogType;
import org.mokey.acupple.dashcam.common.utils.IdentityUtil;
import org.mokey.acupple.dashcam.common.utils.Strings;

import java.util.Map;

/**
 * Created by Yuan on 2015/6/17.
 */
public class CLoggingLogger implements ILog {

	private String _logName = "";
	private ILogSender _logSender;

	public CLoggingLogger(String logName, ILogSender sender) {
		if (Strings.isNullOrEmpty(logName)) {
			this._logName = "defaultLogName";
		} else {
			this._logName = logName;
		}

		this._logSender = sender;
	}

	private void writeLog(LogLevel logLevel, String title, String message,
			Throwable throwable, Map<String, String> attrs) {
		LogEvent logEvent = new LogEvent();
		logEvent.setId(IdentityUtil.getUniqueID());
		logEvent.setLogLevel(logLevel);
		logEvent.setLogType(LogType.APP);
		logEvent.setCreatedTime(System.currentTimeMillis());
		logEvent.setSource(this._logName);
		logEvent.setThreadId(Thread.currentThread().getId());
		logEvent.setTitle(title);
		if (Strings.isNullOrEmpty(title)) {
			logEvent.setTitle("NA");
		}
		if (message != null) {
			logEvent.setMessage(message);
		}
		if (throwable != null) {
			if (logEvent.getTitle().equals("NA")) {
				logEvent.setTitle(throwable.getMessage());
			}
			logEvent.setMessage(Strings.toString(throwable));
		}
		if (logEvent.getMessage() == null) {
			logEvent.setMessage("");
		}
		logEvent.setAttributes(attrs);
		if (this._logSender != null) {
			_logSender.send(logEvent);
		}
	}

	@Override
	public void debug(String title, String message) {
		this.writeLog(LogLevel.DEBUG, title, message, null, null);
	}

	@Override
	public void debug(String title, Throwable throwable) {
		this.writeLog(LogLevel.DEBUG, title, null, throwable, null);
	}

	@Override
	public void debug(String title, String message, Map<String, String> attrs) {
		this.writeLog(LogLevel.DEBUG, title, message, null, attrs);
	}

	@Override
	public void debug(String title, Throwable throwable,
			Map<String, String> attrs) {
		this.writeLog(LogLevel.DEBUG, title, null, throwable, attrs);
	}

	@Override
	public void debug(String message) {
		this.writeLog(LogLevel.DEBUG, null, message, null, null);
	}

	@Override
	public void debug(Throwable throwable) {
		this.writeLog(LogLevel.DEBUG, null, null, throwable, null);
	}

	@Override
	public void debug(String message, Map<String, String> attrs) {
		this.writeLog(LogLevel.DEBUG, null, message, null, attrs);
	}

	@Override
	public void debug(Throwable throwable, Map<String, String> attrs) {
		this.writeLog(LogLevel.DEBUG, null, null, throwable, attrs);
	}

	@Override
	public void error(String title, String message) {
		this.writeLog(LogLevel.ERROR, title, message, null, null);
	}

	@Override
	public void error(String title, Throwable throwable) {
		this.writeLog(LogLevel.ERROR, title, null, throwable, null);
	}

	@Override
	public void error(String title, String message, Map<String, String> attrs) {
		this.writeLog(LogLevel.ERROR, title, message, null, attrs);
	}

	@Override
	public void error(String title, Throwable throwable,
			Map<String, String> attrs) {
		this.writeLog(LogLevel.ERROR, title, null, throwable, attrs);
	}

	@Override
	public void error(String message) {
		this.writeLog(LogLevel.ERROR, null, message, null, null);
	}

	@Override
	public void error(Throwable throwable) {
		this.writeLog(LogLevel.ERROR, null, null, throwable, null);
	}

	@Override
	public void error(String message, Map<String, String> attrs) {
		this.writeLog(LogLevel.ERROR, null, message, null, attrs);
	}

	@Override
	public void error(Throwable throwable, Map<String, String> attrs) {
		this.writeLog(LogLevel.ERROR, null, null, throwable, attrs);
	}

	@Override
	public void fatal(String title, String message) {
		this.writeLog(LogLevel.FATAL, title, message, null, null);
	}

	@Override
	public void fatal(String title, Throwable throwable) {
		this.writeLog(LogLevel.FATAL, title, null, throwable, null);
	}

	@Override
	public void fatal(String title, String message, Map<String, String> attrs) {
		this.writeLog(LogLevel.FATAL, title, message, null, attrs);
	}

	@Override
	public void fatal(String title, Throwable throwable,
			Map<String, String> attrs) {
		this.writeLog(LogLevel.FATAL, null, null, throwable, attrs);
	}

	@Override
	public void fatal(String message) {
		this.writeLog(LogLevel.FATAL, null, message, null, null);
	}

	@Override
	public void fatal(Throwable throwable) {
		this.writeLog(LogLevel.FATAL, null, null, throwable, null);
	}

	@Override
	public void fatal(String message, Map<String, String> attrs) {
		this.writeLog(LogLevel.FATAL, null, message, null, attrs);
	}

	@Override
	public void fatal(Throwable throwable, Map<String, String> attrs) {
		this.writeLog(LogLevel.FATAL, null, null, throwable, attrs);
	}

	@Override
	public void info(String title, String message) {
		this.writeLog(LogLevel.INFO, title, message, null, null);
	}

	@Override
	public void info(String title, Throwable throwable) {
		this.writeLog(LogLevel.INFO, title, null, throwable, null);
	}

	@Override
	public void info(String title, String message, Map<String, String> attrs) {
		this.writeLog(LogLevel.INFO, title, message, null, attrs);
	}

	@Override
	public void info(String title, Throwable throwable,
			Map<String, String> attrs) {
		this.writeLog(LogLevel.INFO, null, null, throwable, attrs);
	}

	@Override
	public void info(String message) {
		this.writeLog(LogLevel.INFO, null, message, null, null);
	}

	@Override
	public void info(Throwable throwable) {
		this.writeLog(LogLevel.INFO, null, null, throwable, null);
	}

	@Override
	public void info(String message, Map<String, String> attrs) {
		this.writeLog(LogLevel.INFO, null, message, null, attrs);
	}

	@Override
	public void info(Throwable throwable, Map<String, String> attrs) {
		this.writeLog(LogLevel.INFO, null, null, throwable, attrs);
	}

	@Override
	public void warn(String title, String message) {
		this.writeLog(LogLevel.WARN, title, message, null, null);
	}

	@Override
	public void warn(String title, Throwable throwable) {
		this.writeLog(LogLevel.WARN, title, null, throwable, null);
	}

	@Override
	public void warn(String title, String message, Map<String, String> attrs) {
		this.writeLog(LogLevel.WARN, title, message, null, attrs);
	}

	@Override
	public void warn(String title, Throwable throwable,
			Map<String, String> attrs) {
		this.writeLog(LogLevel.WARN, title, null, throwable, attrs);
	}

	@Override
	public void warn(String message) {
		this.writeLog(LogLevel.WARN, null, message, null, null);
	}

	@Override
	public void warn(Throwable throwable) {
		this.writeLog(LogLevel.WARN, null, null, throwable, null);
	}

	@Override
	public void warn(String message, Map<String, String> attrs) {
		this.writeLog(LogLevel.WARN, null, message, null, attrs);
	}

	@Override
	public void warn(Throwable throwable, Map<String, String> attrs) {
		this.writeLog(LogLevel.WARN, null, null, throwable, attrs);
	}
}
