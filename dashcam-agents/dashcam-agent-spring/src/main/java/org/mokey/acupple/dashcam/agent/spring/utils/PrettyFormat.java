package org.mokey.acupple.dashcam.agent.spring.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mokey.acupple.dashcam.agent.spring.configuration.SimpleTraceConfiguration;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Forest on 2015/12/23.
 */
public class PrettyFormat {
    private static ObjectMapper mapper = new ObjectMapper();
    private static Set<Class<?>> wrapperTypes = new HashSet<>();

    static {
        wrapperTypes.add(Boolean.class);
        wrapperTypes.add(Character.class);
        wrapperTypes.add(Byte.class);
        wrapperTypes.add(Short.class);
        wrapperTypes.add(Integer.class);
        wrapperTypes.add(Long.class);
        wrapperTypes.add(Float.class);
        wrapperTypes.add(Double.class);
    }

    public static String toString(Object value) {
        if (value == null) {
            return "null";
        }
        if(SimpleTraceConfiguration.isPretty()){
            String returnStr = value.toString();
            try {
                if(SimpleTraceConfiguration.isPretty()) {
                    returnStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
                }
            } catch (JsonProcessingException e) {}
            return returnStr;
        }
        if (Collection.class.isAssignableFrom(value.getClass())) {
            return "size=" + ((Collection<?>) value).size();
        } else if (Map.class.isAssignableFrom(value.getClass())) {
            return "size=" + ((Map<?,?>) value).size();
        } else if (value.getClass().isArray()) {
            return "size=" + Array.getLength(value);
        } else if (wrapperTypes.contains(value.getClass())) {
            return value.toString();
        } else if (String.class.isAssignableFrom(value.getClass())) {
            return (String) value;
        } else {
            return "<"+value.getClass().getSimpleName().toString()+">";
        }
    }

    public static void main(String[] args){
        System.out.println(PrettyFormat.class.getCanonicalName());
    }
}
