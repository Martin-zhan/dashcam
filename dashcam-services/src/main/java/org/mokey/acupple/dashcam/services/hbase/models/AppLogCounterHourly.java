package org.mokey.acupple.dashcam.services.hbase.models;

import org.apache.hadoop.hbase.util.Bytes;
import org.mokey.acupple.dashcam.common.utils.CamUtil;
import org.mokey.acupple.dashcam.hbase.annotations.Table;
import org.mokey.acupple.dashcam.services.utils.AggregateUtil;

/**
 * The count of logs per hour of a specified appId
 * Created by enousei on 3/11/16.
 */
@Table(name = "dashcam_app_hcounter")
public class AppLogCounterHourly extends CounterInfo {

    public AppLogCounterHourly(){}

    public AppLogCounterHourly(int appId, String envGroup, long time){
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
                Bytes.toBytes(AggregateUtil.getHourPart(time)));
    }
}
