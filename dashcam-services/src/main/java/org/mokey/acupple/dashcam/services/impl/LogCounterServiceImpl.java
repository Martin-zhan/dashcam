package org.mokey.acupple.dashcam.services.impl;

import org.mokey.acupple.dashcam.services.LogCounterService;
import org.mokey.acupple.dashcam.services.hbase.LogCounterDao;
import org.mokey.acupple.dashcam.services.models.AggregationType;
import org.mokey.acupple.dashcam.services.models.LogCounter;
import org.mokey.acupple.dashcam.services.models.PrimitiveLogCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class LogCounterServiceImpl implements LogCounterService {
	
	private static final Logger logger = LoggerFactory
			.getLogger(LogCounterServiceImpl.class);

	private LogCounterDao logCounterDao;

	public boolean incrementCounter(Map<Long, LogCounter> appMCounters) {
		
		return logCounterDao.incrementCounter(appMCounters);
	}

	@Override
	public List<PrimitiveLogCounter> getCount(String envGroup, long from,
			long to, AggregationType aggreType) {
		logger.info("dashcam_counter read from hbase instead of cache");
		return logCounterDao.getCount(envGroup, from, to, aggreType);
	}

	@Override
	public List<PrimitiveLogCounter> getCount(int appId, String envGroup,
			long from, long to, AggregationType aggreType) {
		logger.info("dashcam_app_counter read from hbase instead of cache");
		return logCounterDao.getCount(appId, envGroup, from, to, aggreType);
	}

	@Override
	/*@Cacheable(value="dashcam_hcounter", key="#root.args[0]")*/
	public List<PrimitiveLogCounter> getCount(String envGroup, long from,
			long to) {
		return logCounterDao.getCount(envGroup, from, to);
	}

	@Override
	public Map<Integer, List<PrimitiveLogCounter>> getCount(
			Collection<Integer> appIds, String envGroup, long from, long to,
			AggregationType aggreType) {
		return logCounterDao.getCount(appIds, envGroup, from, to, aggreType);
	}

}
