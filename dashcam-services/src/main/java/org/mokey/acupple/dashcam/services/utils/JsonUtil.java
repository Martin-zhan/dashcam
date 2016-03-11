package org.mokey.acupple.dashcam.services.utils;

import org.mokey.acupple.dashcam.services.models.LogIndex;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Yuan on 2015/6/26.
 */
public class JsonUtil {
	private static final Logger logger = LoggerFactory
			.getLogger(JsonUtil.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    }

    public static String toJson(LogIndex instance){
        try {
            return objectMapper.writeValueAsString(instance);
        } catch (Exception e) {
        	logger.error("To json error", e);
        }
        return "";
    }


    public static LogIndex toLog(String json){
        try {
        return objectMapper.readValue(json, LogIndex.class);
        } catch (Exception e) {
        	logger.error("To Log error", e);
        }
        return null;
    }
}