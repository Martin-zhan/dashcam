package org.mokey.acupple.dashcam.agent.utils;


import org.mokey.acupple.dashcam.common.models.thrift.LogEvent;
import org.mokey.acupple.dashcam.common.utils.Strings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yuan on 2015/6/17.
 */
public class LogEventUtil {
    private static final int MAX_TITLE_SIZE = 200;
    private static final int MAX_KEY_SIZE = 32;
    private static final int MAX_VALUE_SIZE = 2 * 1024; // 2K
    private static final int MAX_ADDINFO_SIZE = 10; // 10个k/v pair

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    /**
     * Truncate the value according to max length limit
     *
     * @param value     a string
     * @param maxLength length limit
     * @return truncated string
     */
    private static String truncate(String value, int maxLength) {
        if (Strings.isNullOrEmpty(value)) return value;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    /**
     * Truncate log event to limit its size
     *
     * @param logEvent lot event to be truncated
     */
    public static void truncateLogSize(LogEvent logEvent, int size) {
        int maxMessageSize = size * 1024;
        logEvent.setTitle(truncate(logEvent.getTitle(), MAX_TITLE_SIZE));
        logEvent.setMessage(truncate(logEvent.getMessage(), maxMessageSize));

        if (logEvent.getAttributes() != null && logEvent.getAttributes().size() > 0) {
            Map<String, String> attrs = new HashMap<String, String>();
            int i = 0;
            for (String key : logEvent.getAttributes().keySet()) {
                i++;
                if (i > MAX_ADDINFO_SIZE) {
                    break;
                }
                String k = truncate(key, MAX_KEY_SIZE);
                String v = logEvent.getAttributes().get(key);
                v = truncate(v, MAX_VALUE_SIZE);
                attrs.put(k, v);
            }
            logEvent.setAttributes(attrs);
        }
    }

    public static String toLogString(String loggerName, LogEvent logEvent){
        StringBuffer data = new StringBuffer();
        if(logEvent.getCreatedTime() > 0){
            Date date = new Date(logEvent.getCreatedTime());
            data.append(df.format(date) + "|");
        }
        data.append(logEvent.getLogLevel().name());
        data.append("|" + Thread.currentThread().getName());
        data.append("|" + loggerName);
        if(logEvent.getAttributes() != null && logEvent.getAttributes().size() > 0){
            String tag = "[";
            for (String key: logEvent.getAttributes().keySet()){
                tag += (key + "=" + logEvent.getAttributes().get(key) + ",");
            }
            tag = tag.substring(0, tag.length() - 1) + "]";
            data.append("|" + tag);
        }
        if(!Strings.isNullOrEmpty(logEvent.getTitle())){
            data.append("|" + logEvent.getTitle());
        }
        if(!Strings.isNullOrEmpty(logEvent.getMessage())){
            data.append("|" + logEvent.getMessage());
        }
        return data.toString();
    }

}
