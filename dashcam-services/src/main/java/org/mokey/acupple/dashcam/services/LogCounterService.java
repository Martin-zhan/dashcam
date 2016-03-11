package org.mokey.acupple.dashcam.services;

import org.mokey.acupple.dashcam.services.models.AggregationType;
import org.mokey.acupple.dashcam.services.models.LogCounter;
import org.mokey.acupple.dashcam.services.models.PrimitiveLogCounter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface LogCounterService {

	boolean incrementCounter(Map<Long, LogCounter> appMCounters);

	List<PrimitiveLogCounter> getCount(String envGroup, long from, long to,
									   AggregationType aggreType);

	List<PrimitiveLogCounter> getCount(int appId, String envGroup, long from,
									   long to, AggregationType aggreType);

	Map<Integer, List<PrimitiveLogCounter>> getCount(Collection<Integer> appIds,
													 String envGroup, long from, long to, AggregationType aggreType);

	List<PrimitiveLogCounter> getCount(String envGroup, long from, long to);

}
