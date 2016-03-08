package org.mokey.acupple.dashcam.agent.conf;

import org.mokey.acupple.dashcam.common.models.thrift.LogLevel;
import org.mokey.acupple.dashcam.common.utils.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by enousei on 3/8/16.
 */
public class LocalDashcamProperties extends DashcamProperties{

    private static Logger logger = Logger.getLogger(LocalDashcamProperties.class.getName());

    private static final String APP_ID_KEY = "dashcam.agent.appId";
    private static final String BROKER_LIST_KEY = "dashcam.agent.kafka.brokerList";
    private static final String LEVEL_KEY = "dashcam.agent.log.level";
    private static final String APP_LOG_ENABLE_KEY = "dashcam.agent.log.enable";
    private static final String APP_TRACE_ENABLE_KEY = "dashcam.agent.trace.enable";
    private static final String LOGGING_MAX_MESSAGE_SIZE = "dashcam.agent.max.message.size";
    private static final String APP_CHUNK_SIZE = "dashcam.agent.chunk.size";

    private static final String CONF_FILE = "/dashcam-commons.properties";
    private Properties properties = new Properties();

    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Properties properties = new Properties();
        InputStream inputStream = null;
        try{
            inputStream = classLoader.getResourceAsStream(CONF_FILE);
            properties.load(inputStream);
            logger.info("Begin to initialize dashcam agent setting");

            this.setAppId();
            this.setBrokerList();
            this.setAppLogEnabled();
            this.setTraceEnabled();
            this.setMaxMessagesSize();
            this.setChunkSize();

        }catch (IOException ex){
            ex.printStackTrace();
        }finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setAppId(){
        this.appId = Integer.parseInt(properties.getProperty(APP_ID_KEY));
    }

    private void setBrokerList(){
        this.brokerList = properties.getProperty(BROKER_LIST_KEY);
    }

    private void setLevel(){
        String val = properties.getProperty(LEVEL_KEY);
        if(Strings.isNullOrEmpty(val)){
            logger.info(LEVEL_KEY + " update failed, config value is empty");
            return;
        }
        if(val.equalsIgnoreCase(LogLevel.INFO.toString())) {
            this.level = LogLevel.INFO;
            logger.info(LEVEL_KEY + " has been updated to: INFO");
            return;
        }
        if(val.equalsIgnoreCase(LogLevel.DEBUG.toString())){
            this.level = LogLevel.DEBUG;
            logger.info(LEVEL_KEY + " has been updated to: DEBUG");
            return;
        }
        if(val.equalsIgnoreCase(LogLevel.ERROR.toString())){
            this.level = LogLevel.ERROR;
            logger.info(LEVEL_KEY + " has been updated to: ERROR");
            return;
        }
        if(val.equalsIgnoreCase(LogLevel.WARN.toString())){
            this.level = LogLevel.WARN;
            logger.info(LEVEL_KEY + " has been updated to: WARN");
            return;
        }

        logger.log(Level.WARNING, LEVEL_KEY + " update failed, invalid config, pls check.");
    }

    private void setAppLogEnabled(){
        String val = properties.getProperty(APP_LOG_ENABLE_KEY);
        if(Strings.isNullOrEmpty(val) || (!val.equalsIgnoreCase("true") && val.equalsIgnoreCase("false"))){
            logger.log(Level.WARNING, APP_LOG_ENABLE_KEY + " update failed. invalid value: " + val);
            return;
        }
        this.appLogEnabled = Boolean.parseBoolean(val);
        logger.info(APP_LOG_ENABLE_KEY + " has been updated to: " + val);
    }

    private void setTraceEnabled(){
        String val = properties.getProperty(APP_TRACE_ENABLE_KEY);
        if(Strings.isNullOrEmpty(val) || (!val.equalsIgnoreCase("true") && val.equalsIgnoreCase("false"))){
            logger.log(Level.WARNING, APP_TRACE_ENABLE_KEY + " update failed. invalid value: " + val);
            return;
        }
        this.traceEnabled = Boolean.parseBoolean(val);
        logger.info(APP_TRACE_ENABLE_KEY + " has been updated to: " + val);
    }

    private void setMaxMessagesSize(){
        String val = properties.getProperty(LOGGING_MAX_MESSAGE_SIZE);
        if(Strings.isNullOrEmpty(val)){
            logger.log(Level.WARNING, LOGGING_MAX_MESSAGE_SIZE + " update failed. empty value");
            return;
        }
        short shortVal = 0;
        try{
            shortVal =  Short.parseShort(val);
        }catch (Exception e){
            logger.log(Level.WARNING, LOGGING_MAX_MESSAGE_SIZE + "update failed, parse short error.", e);
        }
        if(shortVal > 0){
            this.maxMessageSize = shortVal;
            logger.info(LOGGING_MAX_MESSAGE_SIZE + " has been updated to: " + shortVal);
        }else {
            logger.log(Level.WARNING, LOGGING_MAX_MESSAGE_SIZE + " update failed. invalid value: " + shortVal);
        }
    }

    private void setChunkSize(){
        String val = properties.getProperty(APP_CHUNK_SIZE);
        if(Strings.isNullOrEmpty(val)){
            logger.log(Level.WARNING, APP_CHUNK_SIZE + " update failed. empty value");
            return;
        }

        int intVal = 0;
        try{
            intVal = Integer.parseInt(val);
        }catch (Exception e){
            logger.log(Level.WARNING, APP_CHUNK_SIZE + " update failed, parse int error.", e);
        }
        if(intVal > 0){
            this.chunkSize = intVal;
            logger.info(APP_CHUNK_SIZE + " has been updated to: " + intVal);
        }else {
            logger.log(Level.WARNING, APP_CHUNK_SIZE + " update failed, invalid value: " + intVal);
        }
    }
}
