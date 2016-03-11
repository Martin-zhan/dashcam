package org.mokey.acupple.dashcam.services.hbase;

import org.apache.hadoop.hbase.util.Bytes;
import org.mokey.acupple.dashcam.common.utils.CamUtil;
import org.mokey.acupple.dashcam.hbase.HFxClient;
import org.mokey.acupple.dashcam.services.hbase.models.LogHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LogHostDao {
	private static final Logger logger = LoggerFactory.getLogger(LogHostDao.class);

	private HFxClient client;
	public LogHostDao(HFxClient client){
		this.client = client;
	}
	public boolean addHost(LogHost host) {
		try {
			this.client.insert(host);
		} catch (Exception ex) {
			logger.error("Failed to put into hbase", ex);
		}
		return false;
	}

	public List<LogHost> getHosts(int appId, String envGroup) {

		byte[] appIdHash = Bytes.toBytes(CamUtil.getHashCode(String.valueOf(appId)));
		byte[] appIdBytes = Bytes.toBytes(appId);
		byte[] envGroupHash = Bytes.toBytes(CamUtil.getHashCode(envGroup));

		byte[] startRowkey = CamUtil.concat(appIdHash, appIdBytes, envGroupHash, new byte[]{0});

		byte[] endRowkey = CamUtil.concat(appIdHash, appIdBytes, envGroupHash, new byte[]{-1});

		List<LogHost> logHosts = null;
		try {
			logHosts = client.scan(startRowkey, endRowkey, LogHost.class);
		} catch (Exception ex) {
			logger.error("Failed to put into hbase", ex);
		}

		return logHosts;
	}

}
