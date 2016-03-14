package org.mokey.acupple.dashcam.services.hbase.models;

import org.apache.hadoop.hbase.util.Bytes;
import org.mokey.acupple.dashcam.common.utils.CamUtil;
import org.mokey.acupple.dashcam.hbase.annotations.Table;
import org.mokey.acupple.dashcam.services.utils.AggregateUtil;

/**
 * /**
 * similar to APP_HOUR_COUNTER_TABLE, but the rowkey is not the same
 * need group by appId
 * Created by enousei on 3/11/16.
 */
@Table(name = "dashcam_hcounter_rate")
public class AppLogCounterWithRateHourly extends CounterInfo{

    public AppLogCounterWithRateHourly(){}

    public AppLogCounterWithRateHourly(int appId, String envGroup, long time){
        this.appId = appId;
        this.envGroup = envGroup;
        this.time = time;
    }

    @Override
    public byte[] getRowKey() {
        byte[] envGroupHash = Bytes.toBytes(CamUtil.getHashCode(envGroup));
        byte[] hourBytes = Bytes.toBytes(AggregateUtil.getHourPart(time));
        byte[] appIdBytes = Bytes.toBytes(appId);

        return CamUtil.concat(envGroupHash, hourBytes, appIdBytes);
    }
}
