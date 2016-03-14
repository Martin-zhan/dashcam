package org.mokey.acupple.dashcam.services.impl;

import org.mokey.acupple.dashcam.services.LogCounterService;
import org.mokey.acupple.dashcam.services.hbase.LogCounterDao;
import org.mokey.acupple.dashcam.services.hbase.models.AppLogCounter;
import org.mokey.acupple.dashcam.services.hbase.models.CounterInfo;
import org.mokey.acupple.dashcam.services.models.AggregationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class LogCounterServiceImpl implements LogCounterService {
	
	private static final Logger logger = LoggerFactory
			.getLogger(LogCounterServiceImpl.class);

	private LogCounterDao logCounterDao;

	public void incrementCounter(Map<Long, AppLogCounter> appMCounters) {
		logCounterDao.increment(appMCounters);
	}

	@Override
	public List<CounterInfo> getCount(String envGroup, long from,
			long to, AggregationType aggreType) {
		return logCounterDao.getCount(envGroup, from, to, aggreType);
	}

	@Override
	public List<CounterInfo> getCount(int appId, String envGroup,
			long from, long to, AggregationType aggreType) {
		return logCounterDao.getCount(appId, envGroup, from, to, aggreType);
	}

	@Override
	public List<CounterInfo> getCount(String envGroup, long from,
			long to) {
		return logCounterDao.getCount(envGroup, from, to);
	}

	@Override
	public Map<Integer, List<CounterInfo>> getCount(
			Set<Integer> appIds, String envGroup, long from, long to,
			AggregationType aggreType) {
		return logCounterDao.getCount(appIds, envGroup, from, to, aggreType);
	}

}
