package org.mokey.acupple.dashcam.services.hbase;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.hadoop.hbase.util.Bytes;
import org.mokey.acupple.dashcam.common.utils.CamUtil;
import org.mokey.acupple.dashcam.hbase.HFxClient;
import org.mokey.acupple.dashcam.services.hbase.models.*;
import org.mokey.acupple.dashcam.services.models.AggregationType;
import org.mokey.acupple.dashcam.services.utils.AggregateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

public class LogCounterDao {
	private static final Logger logger = LoggerFactory.getLogger(LogCounterDao.class);

	private HFxClient client;

	public LogCounterDao(HFxClient client){
		this.client = client;
	}

	public void increment(Map<Long, AppLogCounter> appMLogs) {
		Map<String, Map<Long, LogCounter>> mLogs = Maps.newHashMap();
		Map<String, Map<Long,LogCounterHourly>> hLogs = Maps.newHashMap();
		Map<Long, AppLogCounterHourly> appHLogs = Maps.newHashMap();
		for (Entry<Long, AppLogCounter> entry: appMLogs.entrySet()){
			long minute = entry.getKey();
			AppLogCounter counter = entry.getValue();
			long hour = AggregateUtil.getHourPart(minute);

			AppLogCounterHourly hourly = appHLogs.get(hour);
			if(hourly == null){
				hourly = new AppLogCounterHourly(counter.getAppId(), counter.getEnvGroup(), hour);
				appHLogs.put(hour, hourly);
			}
			hourly.add(counter);

			if(!mLogs.containsKey(counter.getEnvGroup())){
				mLogs.put(counter.getEnvGroup(), new HashMap<Long, LogCounter>());
				hLogs.put(counter.getEnvGroup(), new HashMap<Long, LogCounterHourly>());
			}

			Map<Long, LogCounter> mmLogs = mLogs.get(counter.getEnvGroup());
			LogCounter mCounter = mmLogs.get(minute);
			if(mCounter == null){
				mCounter = new LogCounter(counter.getAppId(), counter.getEnvGroup(), counter.getTime());
				mmLogs.put(minute, mCounter);
			}
			mCounter.add(counter);

			Map<Long, LogCounterHourly> hhLogs = hLogs.get(counter.getEnvGroup());
			LogCounterHourly hCounter = hhLogs.get(hour);
			if(hCounter == null){
				hCounter = new LogCounterHourly(counter.getAppId(), counter.getEnvGroup(), counter.getTime());
				hhLogs.put(hour, hCounter);
			}
			hCounter.add(counter);
		}

		for (AppLogCounterHourly hLog: appHLogs.values()){ //环境下根据App小时环比数据
			AppLogCounterWithRateHourly rateHour =
					new AppLogCounterWithRateHourly(hLog.getAppId(), hLog.getEnvGroup(), hLog.getTime());
			rateHour.setAppId(hLog.getAppId());
			rateHour.setEnvGroup(hLog.getEnvGroup());
			rateHour.setInfo(hLog.getInfo());
			rateHour.setDebug(hLog.getDebug());
			rateHour.setWarn(hLog.getWarn());
			rateHour.setError(hLog.getError());
			rateHour.setFatal(hLog.getFatal());
			rateHour.setTotal(hLog.getTotal());

			try{
				client.increment(rateHour);
			}catch (Exception ex){
				logger.error("increment app hour log counter failed", ex);
			}
		}

		incrementForeach(appMLogs.values());
		incrementForeach(appHLogs.values());
		for (Map<Long, LogCounter> mmLogs: mLogs.values()){
			incrementForeach(mmLogs.values());
		}
		for (Map<Long, LogCounterHourly> hhLogs : hLogs.values()){
			incrementForeach(hhLogs.values());
		}
	}

	private <T extends CounterInfo> void incrementForeach(Collection<T> bases){
		if(bases != null) {
			for (CounterInfo count : bases) {
				try {
					client.increment(count);
				}catch (Exception ex){
					logger.error("Failed to increment the hbase value[" + count.getClass() + "]", ex);
				}
			}
		}
	}

	/**
	 * 注意，to包含当前小时，如 2015-07-17 11:31:00，转化之后为 2015-07-17 12:00:00
	 *
	 * @param envGroup
	 * @param from
	 * @param to
	 * @param aggreType
	 * @return
	 */
	public List<CounterInfo> getCount(String envGroup, long from,
									  long to, AggregationType aggreType){
		long fromTimePart = aggreType == AggregationType.MINUTE ? AggregateUtil
				.getMinutePart(from) : AggregateUtil.getHourPart(from);
		long toTimePart = aggreType == AggregationType.MINUTE ? AggregateUtil
				.getMinutePart(to) : AggregateUtil.getNextHourPart(to);

		byte[] envGroupHash = Bytes.toBytes(CamUtil.getHashCode(envGroup));

		byte[] fromKey = CamUtil.concat(envGroupHash,
				Bytes.toBytes(fromTimePart));
		byte[] toKey = CamUtil.concat(envGroupHash, Bytes.toBytes(toTimePart));

		Class clazz = aggreType == AggregationType.MINUTE ? LogCounter.class
				: LogCounterHourly.class;

		List<CounterInfo> infos = null;
		try{
			infos = client.scan(fromKey, toKey, clazz);
			for (CounterInfo info: infos){
				info.setEnvGroup(envGroup);
			}
		}catch (Exception ex){
			logger.error("Failed to increment the hbase value", ex);
		}

		return infos;
	}

	/**
	 * 注意，to包含当前小时，如 2015-07-17 11:31:00，转化之后为 2015-07-17 12:00:00
	 *
	 * @param appId
	 * @param envGroup
	 * @param from
	 * @param to
	 * @param aggreType
	 * @return
	 */
	public List<CounterInfo> getCount(int appId, String envGroup,
									  long from, long to, AggregationType aggreType){
		long fromTimePart = aggreType == AggregationType.MINUTE ? AggregateUtil
				.getMinutePart(from) : AggregateUtil.getHourPart(from);
		long toTimePart = aggreType == AggregationType.MINUTE ? AggregateUtil
				.getMinutePart(to) : AggregateUtil.getNextHourPart(to);
		Class clazz = aggreType == AggregationType.MINUTE ? AppLogCounter.class
				: AppLogCounterHourly.class;

		byte[] appIdHash = Bytes.toBytes(CamUtil.getHashCode(String
				.valueOf(appId)));
		byte[] appIdBytes = Bytes.toBytes(appId);
		byte[] envGroupHash = Bytes.toBytes(CamUtil.getHashCode(envGroup));

		byte[] fromKey = CamUtil.concat(appIdHash, appIdBytes, envGroupHash,
				Bytes.toBytes(fromTimePart));
		byte[] toKey = CamUtil.concat(appIdHash, appIdBytes, envGroupHash,
				Bytes.toBytes(toTimePart));

		List<CounterInfo> infos = null;
		try{
			infos =  client.scan(fromKey, toKey, clazz);
			for (CounterInfo info: infos){
				info.setAppId(appId);
				info.setEnvGroup(envGroup);
			}
		}catch (Exception ex){
			logger.error("Failed to increment the hbase value", ex);
		}

		return infos;
	}

	/**
	 * 获取所有appId的一段时间内的信息
	 * 
	 * @param appIds
	 * @param envGroup
	 * @param from
	 * @param to
	 * @param aggreType
	 * @return
	 */
	public Map<Integer, List<CounterInfo>> getCount(
			Set<Integer> appIds, String envGroup, long from, long to,
			AggregationType aggreType) {

		Map<Integer, List<CounterInfo>> countMap = Maps.newHashMap();
		for (Integer appId: appIds){
			countMap.put(appId, getCount(appId, envGroup, from, to, aggreType));
		}
		return countMap;
	}

	/**
	 * 注意，to包含当前小时，如 2015-07-17 11:31:00，转化之后为 2015-07-17 12:00:00
	 * @param envGroup
	 * @param from
	 * @param to
	 * @return
	 */
	public List<CounterInfo> getCount(String envGroup, long from,
			long to) {

		long fromTimePart = AggregateUtil.getHourPart(from);
		long toTimePart = AggregateUtil.getNextHourPart(to);

		byte[] fromAppId = new byte[] { 0 };
		byte[] toAppId = new byte[] { -1 };

		byte[] envGroupHash = Bytes.toBytes(CamUtil.getHashCode(envGroup));

		byte[] fromKey = CamUtil.concat(envGroupHash,
				Bytes.toBytes(fromTimePart), fromAppId);
		byte[] toKey = CamUtil.concat(envGroupHash, Bytes.toBytes(toTimePart),
				toAppId);
		try{
			return client.scan(fromKey, toKey, AppLogCounterWithRateHourly.class);
		}catch (Exception ex){
			logger.error("Failed to increment the hbase value", ex);
		}

		return new ArrayList<>();
	}

	public static void main(String[] args){

	}

}
