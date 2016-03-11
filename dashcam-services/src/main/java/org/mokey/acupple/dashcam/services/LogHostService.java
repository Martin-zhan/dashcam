package org.mokey.acupple.dashcam.services;

import org.mokey.acupple.dashcam.services.hbase.models.LogHost;

import java.util.List;

public interface LogHostService {
	boolean addHost(LogHost host);

	List<LogHost> getHosts(int appId, String envGroup);
}
