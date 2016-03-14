package org.mokey.acupple.dashcam.services;

import org.mokey.acupple.dashcam.services.hbase.models.AppLogCounter;
import org.mokey.acupple.dashcam.services.hbase.models.CounterInfo;
import org.mokey.acupple.dashcam.services.models.AggregationType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LogCounterService {

	void incrementCounter(Map<Long, AppLogCounter> appMCounters);

	List<CounterInfo> getCount(String envGroup, long from, long to,
									   AggregationType aggreType);

	List<CounterInfo> getCount(int appId, String envGroup, long from,
									   long to, AggregationType aggreType);

	Map<Integer, List<CounterInfo>> getCount(Set<Integer> appIds,
													 String envGroup, long from, long to, AggregationType aggreType);

	List<CounterInfo> getCount(String envGroup, long from, long to);

}
