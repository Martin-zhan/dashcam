package org.mokey.acupple.dashcam.services.impl;


import org.mokey.acupple.dashcam.services.LogHostService;
import org.mokey.acupple.dashcam.services.hbase.LogHostDao;
import org.mokey.acupple.dashcam.services.hbase.models.LogHost;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class LogHostServiceImpl implements LogHostService {

	private LogHostDao logHostDao;

	@Override
	public boolean addHost(LogHost host) {
		return logHostDao.addHost(host);
	}

	@Override
	public List<LogHost> getHosts(int appId, String envGroup) {
		List<LogHost> hosts = logHostDao.getHosts(appId, envGroup);
		
		//给HostName去重
		HashSet<String> sets = new HashSet<String>();
		
		List<LogHost> result = new ArrayList<>();
		
		for(LogHost host : hosts){
			if(sets.add(host.getHostname())){
				result.add(host);
			}
		}
		return result;
	}

}
