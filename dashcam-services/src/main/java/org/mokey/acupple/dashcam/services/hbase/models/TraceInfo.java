package org.mokey.acupple.dashcam.services.hbase.models;

import com.google.common.collect.Lists;
import org.apache.hadoop.hbase.util.Bytes;
import org.mokey.acupple.dashcam.common.utils.CamUtil;
import org.mokey.acupple.dashcam.hbase.HBase;
import org.mokey.acupple.dashcam.hbase.HFxClient;
import org.mokey.acupple.dashcam.hbase.annotations.Entity;
import org.mokey.acupple.dashcam.hbase.annotations.Table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by enousei on 3/11/16.
 */
@Table(name = "dashcam_trace")
public class TraceInfo implements HBase{

    public TraceInfo(){}
    public TraceInfo(long traceId){
        this.traceId = traceId;
    }

    @Entity(family = "trace")
    private long traceId;

    @Entity(family = "spans")
    private Map<Long, Long> spanIds;

    public long getTraceId() {
        return traceId;
    }

    public void setTraceId(long traceId) {
        this.traceId = traceId;
    }

    public Map<Long, Long> getSpanIds() {
        return spanIds;
    }

    public void setSpanIds(Map<Long, Long> spanIds) {
        this.spanIds = spanIds;
    }

    @Override
    public byte[] getRowKey() {
        byte[] traceHash = Bytes.toBytes(CamUtil.getHashCode(String
                .valueOf(traceId)));
        return CamUtil.concat(traceHash, Bytes.toBytes(traceId));
    }

    public static void main(String[] args) throws Exception{
        HFxClient client = new HFxClient("127.0.0.1:2181", "/hbase");
        client.createTable(TraceInfo.class);
        client.createTable(SpanInfo.class);

        long mockId = System.currentTimeMillis();
        TraceInfo trace = new TraceInfo();
        trace.setTraceId(mockId);
        trace.setSpanIds(new HashMap<Long, Long>());

        SpanInfo span1 = new SpanInfo(); //Root
        span1.setSpanId(mockId + 1000);
        span1.setRawKey(new byte[]{1});

        SpanInfo span2 = new SpanInfo();
        span2.setSpanId(mockId + 2000);
        span2.setRawKey(new byte[]{2});

        SpanInfo span3 = new SpanInfo();
        span3.setSpanId(mockId + 3000);
        span3.setRawKey(new byte[]{3});

        SpanInfo span4 = new SpanInfo();
        span4.setSpanId(mockId + 4000);
        span4.setRawKey(new byte[]{4});

        SpanInfo span5 = new SpanInfo();
        span5.setSpanId(mockId + 5000);
        span5.setRawKey(new byte[]{5});

        SpanInfo span6 = new SpanInfo();
        span6.setSpanId(mockId + 6000);
        span6.setRawKey(new byte[]{6});

        span2.setParentId(span1.getSpanId());
        span3.setParentId(span2.getSpanId());

        span4.setParentId(span3.getSpanId());

        span5.setParentId(span1.getSpanId());
        span6.setParentId(span5.getSpanId());

        trace.getSpanIds().put(span1.getSpanId(), null);
        trace.getSpanIds().put(span2.getSpanId(), null);
        trace.getSpanIds().put(span3.getSpanId(), null);
        trace.getSpanIds().put(span4.getSpanId(), null);
        trace.getSpanIds().put(span5.getSpanId(), null);
        trace.getSpanIds().put(span6.getSpanId(), null);

        client.insert(trace);
        client.insert(span1);
        client.insert(span2);
        client.insert(span3);
        client.insert(span4);
        client.insert(span5);
        client.insert(span6);

        TraceInfo traceInfo = new TraceInfo(mockId);
        traceInfo.setSpanIds(new HashMap<Long, Long>());
        SpanInfo span7 = new SpanInfo(); //Root
        span7.setSpanId(mockId + 7000);
        span7.setRawKey(new byte[]{7});

        SpanInfo span8 = new SpanInfo();
        span8.setSpanId(mockId + 8000);
        span8.setRawKey(new byte[]{8});
        span8.setParentId(span7.getSpanId());

        SpanInfo span9 = new SpanInfo();
        span9.setSpanId(mockId + 9000);
        span9.setRawKey(new byte[]{9});
        span9.setParentId(span8.getSpanId());

        SpanInfo span10 = new SpanInfo();
        span10.setSpanId(mockId + 10000);
        span10.setRawKey(new byte[]{10});
        span10.setParentId(span9.getSpanId());

        traceInfo.getSpanIds().put(span7.getSpanId(), null);
        traceInfo.getSpanIds().put(span8.getSpanId(), null);
        traceInfo.getSpanIds().put(span9.getSpanId(), null);
        traceInfo.getSpanIds().put(span10.getSpanId(), null);

        client.insert(traceInfo);


        TraceInfo trace1 = client.get(trace.getRowKey(), TraceInfo.class);
        System.out.println(trace1.getSpanIds().size());

        List<byte[]> rowkeys = Lists.newArrayList();
        for (Long spanId: trace1.getSpanIds().keySet()){
            SpanInfo info = new SpanInfo();
            info.setSpanId(spanId);
            rowkeys.add(info.getRowKey());
        }

        List<SpanInfo> spanInfos = client.search(rowkeys, null, SpanInfo.class);
        System.out.println(spanInfos.size());
    }
}
