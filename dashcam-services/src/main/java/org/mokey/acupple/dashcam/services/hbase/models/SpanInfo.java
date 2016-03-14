package org.mokey.acupple.dashcam.services.hbase.models;

import org.apache.hadoop.hbase.util.Bytes;
import org.mokey.acupple.dashcam.common.utils.CamUtil;
import org.mokey.acupple.dashcam.hbase.HBase;
import org.mokey.acupple.dashcam.hbase.annotations.Entity;
import org.mokey.acupple.dashcam.hbase.annotations.Table;

/**
 * Created by enousei on 3/11/16.
 */
@Table(name = "dashcam_span")
public class SpanInfo implements HBase{

    public SpanInfo(){}
    public SpanInfo(long spanId){
        this.spanId = spanId;
    }

    @Entity(family = "span")
    private long spanId;

    @Entity(family = "span")
    private long parentId;

    @Entity(family = "span")
    private byte[] rawKey;

    public long getSpanId() {
        return spanId;
    }

    public void setSpanId(long spanId) {
        this.spanId = spanId;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public byte[] getRawKey() {
        return rawKey;
    }

    public void setRawKey(byte[] rawKey) {
        this.rawKey = rawKey;
    }

    @Override
    public byte[] getRowKey() {
        byte[] traceHash = Bytes.toBytes(CamUtil.getHashCode(String.valueOf(spanId)));
        return CamUtil.concat(traceHash, Bytes.toBytes(spanId));
    }
}
