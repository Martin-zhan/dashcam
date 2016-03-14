package org.mokey.acupple.dashcam.services.hbase;

import com.google.common.collect.Lists;
import org.mokey.acupple.dashcam.common.models.thrift.Span;
import org.mokey.acupple.dashcam.hbase.HFxClient;
import org.mokey.acupple.dashcam.services.elastic.LogIndexDao;
import org.mokey.acupple.dashcam.services.hbase.models.SpanInfo;
import org.mokey.acupple.dashcam.services.hbase.models.TraceInfo;
import org.mokey.acupple.dashcam.services.models.ExtendSpan;
import org.mokey.acupple.dashcam.services.models.LogIndex;

import java.util.HashMap;
import java.util.List;

/**
 * Created by enousei on 3/13/16.
 */
public class TraceDao1 {
    private HFxClient client;
    private RawLogDao rawLogDao;

    private LogIndexDao indexDao;
    public TraceDao1(HFxClient client){
        this.client = client;
        this.rawLogDao = new RawLogDao(client);
        this.indexDao = new LogIndexDao("127.0.0.1:9300");
    }

    public void addSpan(int appId, String envGroup, String env,
                       String hostName, String hostIp, Span span){
        List<LogIndex> logIndexs = rawLogDao.insert(appId, envGroup, env,
                hostName, hostIp, span.getLogEvents());
        indexDao.insert(logIndexs);
        TraceInfo traceInfo = new TraceInfo(span.getTraceId());
        SpanInfo spanInfo = new SpanInfo(span.getSpanId());
        spanInfo.setParentId(span.getParentId());

        try {
            traceInfo = client.get(traceInfo.getRowKey(), TraceInfo.class);
            if(traceInfo == null){
                traceInfo = new TraceInfo(span.getTraceId());
                traceInfo.setSpanIds(new HashMap<Long, Long>());
            }
            traceInfo.getSpanIds().put(spanInfo.getSpanId(), null);
            client.insert(spanInfo);
            client.insert(traceInfo);

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public List<ExtendSpan> search(long traceId){
        TraceInfo traceInfo = new TraceInfo(traceId);
        try {
            traceInfo = client.get(traceInfo.getRowKey(), TraceInfo.class);
            if(traceInfo.getSpanIds().size() == 0){
                return Lists.newArrayList();
            }

            List<byte[]> rowkeys = Lists.newArrayList();
            for (Long spanId: traceInfo.getSpanIds().keySet()){
                SpanInfo info = new SpanInfo();
                info.setSpanId(spanId);
                rowkeys.add(info.getRowKey());
            }

            List<SpanInfo> spanInfos = client.search(rowkeys, null, SpanInfo.class);
            if(spanInfos.size() != rowkeys.size()){
                return null;
            }

            //TODO: fill ExtendSpan

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return Lists.newArrayList();
    }
}
