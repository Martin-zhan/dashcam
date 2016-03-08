package org.mokey.acupple.dashcam.agent.trace;

import org.mokey.acupple.dashcam.agent.trace.impl.CLoggingTracer;
import org.mokey.acupple.dashcam.common.utils.Strings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于创建ITrace<see cref="ITrace" />实例。
 * Created by Yuan on 2015/6/17.
 */
public class TraceManager {
    private static Map<String, ITrace> _tracers = new ConcurrentHashMap<String, ITrace>();

    /**
     * Initializes a new instance of the <see cref="TraceManager" /> class.
     * <p/>
     * Uses a private access modifier to prevent instantiation of this class.
     */
    private TraceManager() {
    }

    /**
     * 通过类型名获取ITrace实例
     *
     * @param type The type
     * @return ITrace instance
     */
    public static ITrace getTracer(Class<?> type) {
        if (type == null) {
            return getTracer("NoName");
        }
        return getTracer(type.getName());
    }

    /**
     * 通过字符串名获取ITrace实例。
     *
     * @param name tracer name
     * @return ITrace instance
     */
    public static ITrace getTracer(String name) {
        String defaultName = name;
        if (Strings.isNullOrEmpty(name)) {
            defaultName = "defaultName";
        }

        ITrace trace = _tracers.get(defaultName);
        if (trace == null) {
            trace = new CLoggingTracer(defaultName);
            _tracers.put(defaultName, trace);
        }
        return trace;
    }
}
