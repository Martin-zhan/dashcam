package org.mokey.acupple.dashcam.services.hbase.models;

import org.apache.hadoop.hbase.util.Bytes;
import org.mokey.acupple.dashcam.common.utils.CamUtil;
import org.mokey.acupple.dashcam.hbase.annotations.Table;
import org.mokey.acupple.dashcam.services.utils.AggregateUtil;

/**
 * the count of logs per minute of a specified appId
 * Created by enousei on 3/10/16.
 */
@Table(name = "dashcam_app_counter")
public class AppLogCounter extends CounterInfo {

    public AppLogCounter(){}

    public AppLogCounter(int appId, String envGroup, long time){
        this.appId = appId;
        this.envGroup = envGroup;
        this.time = time;
    }

    @Override
    public byte[] getRowKey() {
        byte[] appIdHash = Bytes.toBytes(CamUtil.getHashCode(String.valueOf(appId)));
        byte[] appIdBytes = Bytes.toBytes(appId);
        byte[] envGroupHash = Bytes.toBytes(CamUtil.getHashCode(envGroup));

        return CamUtil.concat(appIdHash, appIdBytes, envGroupHash,
                Bytes.toBytes(AggregateUtil.getMinutePart(time)));
    }
}
