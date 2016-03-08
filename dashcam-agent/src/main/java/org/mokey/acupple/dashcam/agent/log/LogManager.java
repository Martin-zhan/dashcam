package org.mokey.acupple.dashcam.agent.log;

import org.mokey.acupple.dashcam.agent.log.impl.CLoggingLogger;
import org.mokey.acupple.dashcam.agent.log.impl.FreewayLogSender;
import org.mokey.acupple.dashcam.agent.trace.TraceManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Create <see cref="ILog"/> instance which is used to record freeway log
 * Created by Yuan on 2015/6/17.
 */
public class LogManager {
    private static Map<String, ILog> _loggers = new ConcurrentHashMap<String, ILog>();

    /**
     * Initializes a new instance of the <see cref="LogManager" /> class.
     * <p/>
     * Uses a private access modifier to prevent instantiation of this class.
     */
    private LogManager() {
    }

    /**
     * 通过类型名获取ILog实例。
     *
     * @param type logger type
     * @return ILog instance
     */
    public static ILog getLogger(Class<?> type) {
        if (type == null) {
            return getLogger("defaultLogger");
        } else {
            return getLogger(type.getName());
        }
    }

    /**
     * 通过字符串名获取ILog实例。
     *
     * @param name logger name
     * @return ILog instance
     */
    public static ILog getLogger(String name) {
        String loggerName = name;
        if(name == null || name.isEmpty()) {
            loggerName = "defaultLogger";
        }
        ILog logger = _loggers.get(loggerName);
        if (logger == null) {
            logger = new CLoggingLogger(loggerName, new FreewayLogSender(TraceManager.getTracer(loggerName)));
            _loggers.put(loggerName, logger);
        }
        return logger;
    }
}
