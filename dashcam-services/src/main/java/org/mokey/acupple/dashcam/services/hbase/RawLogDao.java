package org.mokey.acupple.dashcam.services.hbase;

import com.google.common.collect.Lists;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;
import org.elasticsearch.common.base.Strings;
import org.mokey.acupple.dashcam.common.models.thrift.LogEvent;
import org.mokey.acupple.dashcam.common.models.thrift.LogLevel;
import org.mokey.acupple.dashcam.common.models.thrift.LogType;
import org.mokey.acupple.dashcam.hbase.HFxClient;
import org.mokey.acupple.dashcam.services.hbase.models.RawLog;
import org.mokey.acupple.dashcam.services.models.LogIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RawLogDao {

    private static final Logger logger = LoggerFactory.getLogger(RawLogDao.class);

    private HFxClient client;
    public RawLogDao(HFxClient client){
        this.client = client;
    }

    public RawLog get(byte[] rowkey) {
        try{
            return client.get(rowkey, RawLog.class);
        }catch (Exception ex){
            logger.error("Failed to get from hbase", ex);
        }
        return null;
    }

    public List<RawLog> search(List<byte[]> rowkeys, String message) {
        try{
            SingleColumnValueFilter filter = null;
            if (!Strings.isNullOrEmpty(message)) {
                filter = new SingleColumnValueFilter(Bytes.toBytes("content"),
                        Bytes.toBytes("message"), CompareOp.EQUAL,
                        new SubstringComparator(message));
                filter.setFilterIfMissing(true);
            }

            return client.search(rowkeys, filter, RawLog.class);
        }catch (Exception ex) {
            logger.error("Failed to get from hbase", ex);
        }

        return null;
    }

    public List<LogIndex> insert(int appId, String envGroup, String env,
                                 String hostName, String hostIp, List<LogEvent> logEvents) {

        List<RawLog> rawLogs = Lists.newArrayList();
        List<LogIndex> results = Lists.newArrayList();
        for (LogEvent event: logEvents){
            RawLog rawLog = new RawLog();
            rawLog.setAppId(appId);
            rawLog.setEnvGroup(envGroup);
            rawLog.setEnv(env);
            rawLog.setHostip(hostIp);
            rawLog.setHostname(hostName);

            rawLog.setLogtype(null == event.getLogType() ? LogType.APP.getValue() :
                    event.getLogType().getValue());

            rawLog.setLoglevel(null == event.getLogLevel() ? LogLevel.INFO.getValue() :
                    event.getLogLevel().getValue());

            rawLog.setMessage(event.getMessage());
            rawLog.setLogtime(event.getCreatedTime());
            rawLog.setSource(event.getSource());
            rawLog.setTitle(event.getTitle());
            rawLog.setTraceid(event.getTraceId());
            rawLog.setSpanid(event.getSpanId());

            rawLogs.add(rawLog); // Insert into HBase

            LogIndex index = new LogIndex(appId, envGroup, env, rawLog.getRowKey());

            index.setHostIp(hostIp);
            index.setHostName(hostName);
            index.setLogLevel(event.getLogLevel());
            index.setLogType(event.getLogType());
            index.setSpanId(event.getSpanId() + "");
            index.setTags(event.getAttributes());
            index.setTimestamp(event.getCreatedTime());
            index.setSource(event.getSource());
            index.setTitle(event.getTitle());
            index.setTraceId(event.getTraceId() + "");

            results.add(index);
        }

        try{
            client.insert(rawLogs);
            return results;
        }catch (Exception ex){
            logger.error("Failed to put into hbase", ex);
        }

        return Lists.newArrayList();
    }
}
