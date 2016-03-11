package org.mokey.acupple.dashcam.services.hbase;

import com.google.common.collect.Lists;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;
import org.elasticsearch.common.base.Strings;
import org.mokey.acupple.dashcam.common.models.RawLog;
import org.mokey.acupple.dashcam.common.models.thrift.LogEvent;
import org.mokey.acupple.dashcam.common.models.thrift.LogLevel;
import org.mokey.acupple.dashcam.common.models.thrift.LogType;
import org.mokey.acupple.dashcam.common.utils.CamUtil;
import org.mokey.acupple.dashcam.services.constants.RawLogConstants;
import org.mokey.acupple.dashcam.services.models.LogIndex;
import org.mokey.acupple.dashcam.services.utils.LogId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;

public class RawLogDao {

    private static final Logger logger = LoggerFactory
            .getLogger(RawLogDao.class);

    public RawLog get(byte[] rowkey) {
        Get get = new Get(rowkey);

        try (Table table = HTableWrapper
                .getInstance(RawLogConstants.RAWLOG_TABLE)) {
            Result result = table.get(get);

            RawLog log = new RawLog();

            if (null == result || null == result.getRow()) {
                return null;
            }

            log.setEnvGroup(Bytes.toString(result.getValue(
                    RawLogConstants.contentFamily, RawLogConstants.envgroup)));
            log.setEnv(Bytes.toString(result.getValue(
                    RawLogConstants.contentFamily, RawLogConstants.env)));
            log.setAppId(Bytes.toInt(result.getRow(), 4, 4));
            log.setHostIp(Bytes.toString(result.getValue(
                    RawLogConstants.contentFamily, RawLogConstants.hostip)));
            log.setHostName(Bytes.toString(result.getValue(
                    RawLogConstants.contentFamily, RawLogConstants.hostname)));
            log.setLogLevel(LogLevel.findByValue(Bytes.toInt(result.getValue(
                    RawLogConstants.contentFamily, RawLogConstants.loglevel))));
            log.setLogTime(Bytes.toLong(result.getValue(
                    RawLogConstants.contentFamily, RawLogConstants.logtime)));
            log.setLogType(LogType.findByValue(Bytes.toInt(result.getValue(
                    RawLogConstants.contentFamily, RawLogConstants.logtype))));
            log.setMessage(Bytes.toString(result.getValue(
                    RawLogConstants.contentFamily, RawLogConstants.message)));
            log.setRowKey(result.getRow());
            log.setSource(Bytes.toString(result.getValue(
                    RawLogConstants.contentFamily, RawLogConstants.source)));
            log.setSpanId(Bytes.toString(result.getValue(
                    RawLogConstants.contentFamily, RawLogConstants.spanid)));

            log.setTags(new HashMap<String,String>());

            NavigableMap<byte[], byte[]> tags = result.getFamilyMap(RawLogConstants.tagFamily);
            for(Entry<byte[], byte[]> entry : tags.entrySet()){
                log.getTags().put(Bytes.toString(entry.getKey()), Bytes.toString(entry.getValue()));
            }

            log.setTitle(Bytes.toString(result.getValue(
                    RawLogConstants.contentFamily, RawLogConstants.title)));
            log.setTraceId(Bytes.toString(result.getValue(
                    RawLogConstants.contentFamily, RawLogConstants.traceid)));

            return log;
        } catch (IOException ex) {
            logger.error("Failed to get from hbase", ex);
        }

        return null;
    }

    public List<RawLog> search(List<byte[]> rowkeys, String message) {

        List<RawLog> rawlogs = Lists.newArrayList();

        List<Get> gets = Lists.newArrayList();

        SingleColumnValueFilter filter = null;
        if (!Strings.isNullOrEmpty(message)) {
            filter = new SingleColumnValueFilter(RawLogConstants.contentFamily,
                    RawLogConstants.message, CompareOp.EQUAL,
                    new SubstringComparator(message));
            filter.setFilterIfMissing(true);
        }

        for (byte[] rowkey : rowkeys) {
            Get get = new Get(rowkey);
            if (null != filter) {
                get.setFilter(filter);
            }
            gets.add(get);
        }

        try (Table table = HTableWrapper
                .getInstance(RawLogConstants.RAWLOG_TABLE)) {
            Result[] results = table.get(gets);
            for (Result result : results) {
                if (null == result || null == result.getRow()) {
                    continue;
                }
                RawLog log = new RawLog();

                log.setMessage(Bytes.toString(result.getValue(
                        RawLogConstants.contentFamily, RawLogConstants.message)));
                log.setRowKey(result.getRow());
                log.setLogTime(Bytes.toLong(result.getValue(
                        RawLogConstants.contentFamily, RawLogConstants.logtime)));

                rawlogs.add(log);
            }
        } catch (IOException ex) {
            logger.error("Failed to get from hbase", ex);
        }

        return rawlogs;
    }

    public List<LogIndex> insert(int appId, String envGroup, String env,
                                 String hostName, String hostIp, List<LogEvent> logEvents) {

        List<LogIndex> results = Lists.newArrayList();

        byte[] appIdHash = Bytes.toBytes(CamUtil.getHashCode(String
                .valueOf(appId)));
        byte[] appIdBytes = Bytes.toBytes(appId);

        List<Put> puts = Lists.newArrayList();

        for (LogEvent event : logEvents) {
            byte[] dayBytes = Bytes.toBytes(CamUtil.getRelativeDay(event
                    .getCreatedTime()));
            byte[] timeBytes = Bytes.toBytes(CamUtil
                    .getRelativeMillSeconds(event.getCreatedTime()));
            byte[] logIdBytes = Bytes.toBytes(LogId.getLogId().nextId());

            byte[] rowkey = CamUtil.concat(appIdHash, appIdBytes, dayBytes,
                    timeBytes, logIdBytes);

            LogIndex index = new LogIndex(appId, envGroup, env, rowkey);

            index.setHostIp(hostIp);
            index.setHostName(hostName);
            index.setLogLevel(event.getLogLevel());
            index.setLogType(event.getLogType());
            index.setSpanId(event.getSpanId()+"");
            index.setTags(event.getAttributes());
            index.setTimestamp(event.getCreatedTime());
            index.setSource(event.getSource());
            index.setTitle(event.getTitle());
            index.setTraceId(event.getTraceId()+"");

            Put put = new Put(rowkey);
            put.addColumn(RawLogConstants.contentFamily,
                    RawLogConstants.envgroup, Bytes.toBytes(envGroup));
            put.addColumn(RawLogConstants.contentFamily, RawLogConstants.env,
                    Bytes.toBytes(env));
            put.addColumn(RawLogConstants.contentFamily,
                    RawLogConstants.logtime,
                    Bytes.toBytes(event.getCreatedTime()));
            put.addColumn(RawLogConstants.contentFamily,
                    RawLogConstants.traceid, Bytes.toBytes(event.getTraceId()));
            put.addColumn(RawLogConstants.contentFamily,
                    RawLogConstants.spanid, Bytes.toBytes(event.getSpanId()));

            if (null == event.getLogType()) {
                put.addColumn(RawLogConstants.contentFamily,
                        RawLogConstants.logtype,
                        Bytes.toBytes(LogType.APP.getValue()));
            } else {
                put.addColumn(RawLogConstants.contentFamily,
                        RawLogConstants.logtype,
                        Bytes.toBytes(event.getLogType().getValue()));
            }

            if (null == event.getLogLevel()) {
                put.addColumn(RawLogConstants.contentFamily,
                        RawLogConstants.loglevel,
                        Bytes.toBytes(LogLevel.INFO.getValue()));
            } else {
                put.addColumn(RawLogConstants.contentFamily,
                        RawLogConstants.loglevel,
                        Bytes.toBytes(event.getLogLevel().getValue()));
            }

            put.addColumn(RawLogConstants.contentFamily,
                    RawLogConstants.source, Bytes.toBytes(event.getSource()));
            put.addColumn(RawLogConstants.contentFamily, RawLogConstants.title,
                    Bytes.toBytes(event.getTitle()));
            put.addColumn(RawLogConstants.contentFamily,
                    RawLogConstants.message, Bytes.toBytes(event.getMessage()));
            put.addColumn(RawLogConstants.contentFamily,
                    RawLogConstants.hostip, Bytes.toBytes(hostIp));
            put.addColumn(RawLogConstants.contentFamily,
                    RawLogConstants.hostname, Bytes.toBytes(hostName));

            if (null != event.getAttributes()) {
                for (Entry<String, String> entry : event.getAttributes()
                        .entrySet()) {
                    put.addColumn(RawLogConstants.tagFamily,
                            Bytes.toBytes(entry.getKey()),
                            Bytes.toBytes(entry.getValue()));
                }
            }
            puts.add(put);

            results.add(index);
        }

        // TODO: make it more transaction, now maybe we lose some data
        try (Table table = HTableWrapper
                .getInstance(RawLogConstants.RAWLOG_TABLE)) {
            table.put(puts);
            return results;
        } catch (IOException ex) {
            logger.error("Failed to put into hbase", ex);
        }

        return Lists.newArrayList();
    }
}
