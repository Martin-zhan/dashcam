package org.mokey.acupple.dashcam.services.hbase;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.mokey.acupple.dashcam.common.utils.CamUtil;
import org.mokey.acupple.dashcam.services.constants.LogCounterConstants;
import org.mokey.acupple.dashcam.services.models.AggregationType;
import org.mokey.acupple.dashcam.services.models.LogCounter;
import org.mokey.acupple.dashcam.services.models.PrimitiveLogCounter;
import org.mokey.acupple.dashcam.services.utils.AggregateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class LogCounterDao {
	private static final Logger logger = LoggerFactory
			.getLogger(LogCounterDao.class);

	private boolean increment(String tableName, byte[] rowkey,
			byte[] columnFamily, LogCounter counter) {
		Increment inc = new Increment(rowkey);

		inc.addColumn(columnFamily, LogCounterConstants.total, counter
				.getTotal().get());
		inc.addColumn(columnFamily, LogCounterConstants.debug, counter
				.getDebug().get());
		inc.addColumn(columnFamily, LogCounterConstants.info, counter.getInfo()
				.get());
		inc.addColumn(columnFamily, LogCounterConstants.warn, counter.getWarn()
				.get());
		inc.addColumn(columnFamily, LogCounterConstants.error, counter
				.getError().get());
		inc.addColumn(columnFamily, LogCounterConstants.fatal, counter
				.getFatal().get());

		try (Table table = HTableWrapper.getInstance(tableName)) {
			table.increment(inc);
			return true;
		} catch (IOException ex) {
			logger.error("Failed to increment the hbase value", ex);
		}

		return false;
	}

	/**
	 * @param appMCounters
	 * @return
	 */
	private boolean incrementAppMCounter(Map<Long, LogCounter> appMCounters) {
		boolean result = false;
		for (Entry<Long, LogCounter> entry : appMCounters.entrySet()) {
			long minute = entry.getKey();
			LogCounter counter = entry.getValue();

			byte[] appIdHash = Bytes.toBytes(CamUtil.getHashCode(String
					.valueOf(counter.getAppId())));
			byte[] appIdBytes = Bytes.toBytes(counter.getAppId());
			byte[] envGroupHash = Bytes.toBytes(CamUtil.getHashCode(counter
					.getEnvGroup()));

			byte[] rowkey = CamUtil.concat(appIdHash, appIdBytes, envGroupHash,
					Bytes.toBytes(minute));

			result = increment(LogCounterConstants.APP_COUNTER_TABLE, rowkey,
					LogCounterConstants.appMCountFamily, counter) && result;
		}
		return result;
	}

	/**
	 * Increment the clog_app_hcounter, together with the clog_hcounter_rate
	 *
	 * @param appHCounter
	 * @return
	 */
	private boolean incrementAppHCounter(Map<Long, LogCounter> appHCounter) {
		boolean result = false;
		for (Entry<Long, LogCounter> entry : appHCounter.entrySet()) {
			LogCounter counter = entry.getValue();

			byte[] appIdHash = Bytes.toBytes(CamUtil.getHashCode(String
					.valueOf(counter.getAppId())));
			byte[] appIdBytes = Bytes.toBytes(counter.getAppId());
			byte[] envGroupHash = Bytes.toBytes(CamUtil.getHashCode(counter
					.getEnvGroup()));
			byte[] hourBytes = Bytes.toBytes(entry.getKey());

			byte[] rowkey = CamUtil.concat(appIdHash, appIdBytes, envGroupHash,
					hourBytes);

			result = increment(LogCounterConstants.APP_HOUR_COUNTER_TABLE,
					rowkey, LogCounterConstants.appHCountFamily, counter);

			byte[] rateRowKey = CamUtil.concat(envGroupHash, hourBytes,
					appIdBytes);

			result = increment(LogCounterConstants.HOUR_COUNTER_RATE_TABLE,
					rateRowKey, LogCounterConstants.hCountRateFamily, counter);
		}
		return result;
	}

	/**
	 * @param mCounter
	 * @return
	 */
	private boolean incrementMCounter(
			Map<String, Map<Long, LogCounter>> mCounter) {
		boolean result = false;
		for (Entry<String, Map<Long, LogCounter>> entry : mCounter.entrySet()) {
			for (Entry<Long, LogCounter> innerEntry : entry.getValue()
					.entrySet()) {
				byte[] envGroupHash = Bytes.toBytes(CamUtil.getHashCode(entry
						.getKey()));

				byte[] rowkey = CamUtil.concat(envGroupHash,
						Bytes.toBytes(innerEntry.getKey()));

				LogCounter counter = innerEntry.getValue();

				// 将 && result放置在后面，防止短路
				result = increment(LogCounterConstants.COUNTER_TABLE, rowkey,
						LogCounterConstants.mCountFamily, counter) && result;
			}
		}
		return result;
	}

	/**
	 * @param hCounter
	 * @return
	 */
	private boolean incrementHCounter(
			Map<String, Map<Long, LogCounter>> hCounter) {
		boolean result = false;
		for (Entry<String, Map<Long, LogCounter>> entry : hCounter.entrySet()) {
			for (Entry<Long, LogCounter> innerEntry : entry.getValue()
					.entrySet()) {
				byte[] envGroupHash = Bytes.toBytes(CamUtil.getHashCode(entry
						.getKey()));

				byte[] rowkey = CamUtil.concat(envGroupHash,
						Bytes.toBytes(innerEntry.getKey()));

				LogCounter counter = innerEntry.getValue();

				// 将 && result放置在后面，防止短路
				result = increment(LogCounterConstants.HOUR_COUNTER_TABLE,
						rowkey, LogCounterConstants.hCountFamily, counter)
						&& result;
			}
		}
		return result;
	}

	/**
	 * increment the counters
	 *
	 * @param appMCounters
	 *            key is the minute, System.currentMilliseconds() / (60*1000) *
	 *            (60*1000) ,value is the total count of the past minute
	 * @return
	 */
	@SuppressWarnings("unused")
	public boolean incrementCounter(Map<Long, LogCounter> appMCounters) {
		boolean result = false;

		Map<String, Map<Long, LogCounter>> mCounters = Maps.newHashMap();
		Map<String, Map<Long, LogCounter>> hCounters = Maps.newHashMap();
		Map<Long, LogCounter> appHCounters = Maps.newHashMap();

		for (Entry<Long, LogCounter> entry : appMCounters.entrySet()) {
			long minute = entry.getKey();
			LogCounter counter = entry.getValue();
			long hour = AggregateUtil.getHourPart(minute);

			LogCounter hourCounter = appHCounters.containsKey(hour) ? appHCounters
					.get(hour).add(counter) : appHCounters.put(hour, counter);

			if (!mCounters.containsKey(counter.getEnvGroup())) {
				mCounters.put(counter.getEnvGroup(),
						new HashMap<Long, LogCounter>());
				hCounters.put(counter.getEnvGroup(),
						new HashMap<Long, LogCounter>());
			}

			Map<Long, LogCounter> envCounter = mCounters.get(counter
					.getEnvGroup());
			LogCounter withoutAppCounter = envCounter.containsKey(minute) ? envCounter
					.get(minute).add(counter) : envCounter.put(minute, counter);

			Map<Long, LogCounter> hourEnvCounter = hCounters.get(counter
					.getEnvGroup());
			LogCounter hourWithoutAppCounter = hourEnvCounter.containsKey(hour) ? hourEnvCounter
					.get(hour).add(counter) : hourEnvCounter.put(hour, counter);
		}

		result = incrementAppMCounter(appMCounters) && result;
		result = incrementAppHCounter(appHCounters) && result;
		result = incrementMCounter(mCounters) && result;
		result = incrementHCounter(hCounters) && result;

		return result;
	}

	private PrimitiveLogCounter fillInLogCounter(Result result,
			byte[] columnFamily, PrimitiveLogCounter counter) {
		byte[] total = result.getValue(columnFamily, LogCounterConstants.total);
		byte[] debug = result.getValue(columnFamily, LogCounterConstants.debug);
		byte[] info = result.getValue(columnFamily, LogCounterConstants.info);
		byte[] warn = result.getValue(columnFamily, LogCounterConstants.warn);
		byte[] error = result.getValue(columnFamily, LogCounterConstants.error);
		byte[] fatal = result.getValue(columnFamily, LogCounterConstants.fatal);

		counter.setTotal(null == total ? 0 : Bytes.toLong(total));
		counter.setDebug(null == debug ? 0 : Bytes.toLong(debug));
		counter.setInfo(null == info ? 0 : Bytes.toLong(info));
		counter.setWarn(null == warn ? 0 : Bytes.toLong(warn));
		counter.setError(null == error ? 0 : Bytes.toLong(error));
		counter.setFatal(null == fatal ? 0 : Bytes.toLong(fatal));

		return counter;
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
	public List<PrimitiveLogCounter> getCount(String envGroup, long from,
			long to, AggregationType aggreType) {
		List<PrimitiveLogCounter> results = Lists.newArrayList();

		long fromTimePart = aggreType == AggregationType.MINUTE ? AggregateUtil
				.getMinutePart(from) : AggregateUtil.getHourPart(from);
		long toTimePart = aggreType == AggregationType.MINUTE ? AggregateUtil
				.getMinutePart(to) : AggregateUtil.getNextHourPart(to);
		String tableName = aggreType == AggregationType.MINUTE ? LogCounterConstants.COUNTER_TABLE
				: LogCounterConstants.HOUR_COUNTER_TABLE;
		byte[] columnFamily = aggreType == AggregationType.MINUTE ? LogCounterConstants.mCountFamily
				: LogCounterConstants.hCountFamily;

		byte[] envGroupHash = Bytes.toBytes(CamUtil.getHashCode(envGroup));

		byte[] fromKey = CamUtil.concat(envGroupHash,
				Bytes.toBytes(fromTimePart));
		byte[] toKey = CamUtil.concat(envGroupHash, Bytes.toBytes(toTimePart));

		Scan scan = new Scan();
		scan.setStartRow(fromKey);
		scan.setStopRow(toKey);

		try (Table table = HTableWrapper.getInstance(tableName)) {
			try (ResultScanner scanner = table.getScanner(scan)) {
				Result result;
				while (null != (result = scanner.next())) {
					PrimitiveLogCounter counter = new PrimitiveLogCounter();
					counter.setAppId(0);
					counter.setEnvGroup(envGroup);
					// rowkey的结构：
					// envgroup_hash(4字节）+hour/minute(8字节)
					// 因此，从第4个字节开始，取8位，得到hour/minute
					counter.setTime(Bytes.toLong(result.getRow(), 4, 8));
					results.add(fillInLogCounter(result, columnFamily, counter));
				}
			}
		} catch (IOException ex) {
			logger.error("Failed to increment the hbase value", ex);
		}

		return results;
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
	public List<PrimitiveLogCounter> getCount(int appId, String envGroup,
			long from, long to, AggregationType aggreType) {

		List<PrimitiveLogCounter> results = Lists.newArrayList();

		long fromTimePart = aggreType == AggregationType.MINUTE ? AggregateUtil
				.getMinutePart(from) : AggregateUtil.getHourPart(from);
		long toTimePart = aggreType == AggregationType.MINUTE ? AggregateUtil
				.getMinutePart(to) : AggregateUtil.getNextHourPart(to);
		String tableName = aggreType == AggregationType.MINUTE ? LogCounterConstants.APP_COUNTER_TABLE
				: LogCounterConstants.APP_HOUR_COUNTER_TABLE;
		byte[] columnFamily = aggreType == AggregationType.MINUTE ? LogCounterConstants.appMCountFamily
				: LogCounterConstants.appHCountFamily;

		byte[] appIdHash = Bytes.toBytes(CamUtil.getHashCode(String
				.valueOf(appId)));
		byte[] appIdBytes = Bytes.toBytes(appId);
		byte[] envGroupHash = Bytes.toBytes(CamUtil.getHashCode(envGroup));

		byte[] fromKey = CamUtil.concat(appIdHash, appIdBytes, envGroupHash,
				Bytes.toBytes(fromTimePart));
		byte[] toKey = CamUtil.concat(appIdHash, appIdBytes, envGroupHash,
				Bytes.toBytes(toTimePart));

		Scan scan = new Scan();
		scan.setStartRow(fromKey);
		scan.setStopRow(toKey);

		try (Table table = HTableWrapper.getInstance(tableName)) {
			try (ResultScanner scanner = table.getScanner(scan)) {
				Result result;
				while (null != (result = scanner.next())) {
					PrimitiveLogCounter counter = new PrimitiveLogCounter();
					counter.setAppId(appId);
					counter.setEnvGroup(envGroup);
					// rowkey的结构：
					// appid_hash(4字节) + appid(4字节) +
					// envgroup_hash(4字节）+minute(8字节)
					// 因此，从第12个字节开始，取8位，得到minute
					counter.setTime(Bytes.toLong(result.getRow(), 12, 8));
					results.add(fillInLogCounter(result, columnFamily, counter));
				}
			}
		} catch (IOException ex) {
			logger.error("Failed to increment the hbase value", ex);
		}

		return results;
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
	public Map<Integer, List<PrimitiveLogCounter>> getCount(
			Collection<Integer> appIds, String envGroup, long from, long to,
			AggregationType aggreType) {

		Map<Integer, List<PrimitiveLogCounter>> results = Maps.newHashMap();

		long fromTimePart = aggreType == AggregationType.MINUTE ? AggregateUtil
				.getMinutePart(from) : AggregateUtil.getHourPart(from);
		long toTimePart = aggreType == AggregationType.MINUTE ? AggregateUtil
				.getMinutePart(to) : AggregateUtil.getNextHourPart(to);
		String tableName = aggreType == AggregationType.MINUTE ? LogCounterConstants.APP_COUNTER_TABLE
				: LogCounterConstants.APP_HOUR_COUNTER_TABLE;
		byte[] columnFamily = aggreType == AggregationType.MINUTE ? LogCounterConstants.appMCountFamily
				: LogCounterConstants.appHCountFamily;

		byte[] envGroupHash = Bytes.toBytes(CamUtil.getHashCode(envGroup));
		byte[] fromTimeBytes = Bytes.toBytes(fromTimePart);
		byte[] toTimeBytes = Bytes.toBytes(toTimePart);

		SingleColumnValueFilter filter = new SingleColumnValueFilter(
				columnFamily, LogCounterConstants.fatal, CompareOp.GREATER,
				Bytes.toBytes(0L));

		try (Table table = HTableWrapper.getInstance(tableName)) {
			for (Integer appId : appIds) {
				byte[] appIdHash = Bytes.toBytes(CamUtil.getHashCode(String
						.valueOf(appId)));
				byte[] appIdBytes = Bytes.toBytes(appId);

				byte[] fromKey = CamUtil.concat(appIdHash, appIdBytes,
						envGroupHash, fromTimeBytes);
				byte[] toKey = CamUtil.concat(appIdHash, appIdBytes,
						envGroupHash, toTimeBytes);

				Scan scan = new Scan();
				scan.setStartRow(fromKey);
				scan.setStopRow(toKey);
				scan.setFilter(filter);
				try (ResultScanner scanner = table.getScanner(scan)) {
					Result result = null;
					List<PrimitiveLogCounter> counters = Lists.newArrayList();
					while (null != (result = scanner.next())) {
						PrimitiveLogCounter counter = new PrimitiveLogCounter();
						counter.setAppId(appId);
						counter.setEnvGroup(envGroup);
						// rowkey的结构：
						// appid_hash(4字节) + appid(4字节) +
						// envgroup_hash(4字节）+minute(8字节)
						// 因此，从第12个字节开始，取8位，得到minute
						counter.setTime(Bytes.toLong(result.getRow(), 12, 8));
						counters.add(fillInLogCounter(result, columnFamily,
								counter));
					}
					results.put(appId, counters);
				}
			}

		} catch (IOException ex) {
			logger.error("Failed to increment the hbase value", ex);
		}

		return results;
	}

	/**
	 * 注意，to包含当前小时，如 2015-07-17 11:31:00，转化之后为 2015-07-17 12:00:00
	 *
	 * @param envGroup
	 * @param from
	 * @param to
	 * @return
	 */
	public List<PrimitiveLogCounter> getCount(String envGroup, long from,
			long to) {
		List<PrimitiveLogCounter> results = Lists.newArrayList();

		long fromTimePart = AggregateUtil.getHourPart(from);
		long toTimePart = AggregateUtil.getNextHourPart(to);
		String tableName = LogCounterConstants.HOUR_COUNTER_RATE_TABLE;
		byte[] columnFamily = LogCounterConstants.hCountRateFamily;

		byte[] fromAppId = new byte[] { 0 };
		byte[] toAppId = new byte[] { -1 };

		byte[] envGroupHash = Bytes.toBytes(CamUtil.getHashCode(envGroup));

		byte[] fromKey = CamUtil.concat(envGroupHash,
				Bytes.toBytes(fromTimePart), fromAppId);
		byte[] toKey = CamUtil.concat(envGroupHash, Bytes.toBytes(toTimePart),
				toAppId);

		Scan scan = new Scan();
		scan.setStartRow(fromKey);
		scan.setStopRow(toKey);

		try (Table table = HTableWrapper.getInstance(tableName)) {
			try (ResultScanner scanner = table.getScanner(scan)) {
				Result result;
				while (null != (result = scanner.next())) {
					PrimitiveLogCounter counter = new PrimitiveLogCounter();
					// rowkey的结构： envgroup_hash(4字节）+hour(8字节) + appid(4字节)
					// 因此，从第12个字节开始，取4位，得到appid
					counter.setAppId(Bytes.toInt(result.getRow(), 12, 4));
					counter.setEnvGroup(envGroup);
					// 同理，从第4个字节开始，取8位，作为hour
					counter.setTime(Bytes.toLong(result.getRow(), 4, 8));
					results.add(fillInLogCounter(result, columnFamily, counter));
				}
			}
		} catch (IOException ex) {
			logger.error("Failed to increment the hbase value", ex);
		}

		return results;
	}

	public static void main(String[] args) {
		System.out.println(CamUtil.getHashCode("DEV"));
		System.out.println(CamUtil.getHashCode("UAT"));
	}

}
