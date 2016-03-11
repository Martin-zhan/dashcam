package org.mokey.acupple.dashcam.services.impl;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.hadoop.hbase.util.Base64;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.search.SearchHit;
import org.mokey.acupple.dashcam.common.models.thrift.LogEvent;
import org.mokey.acupple.dashcam.common.utils.Strings;
import org.mokey.acupple.dashcam.services.LogEventService;
import org.mokey.acupple.dashcam.services.constants.CommonConstants;
import org.mokey.acupple.dashcam.services.elastic.LogIndexDao;
import org.mokey.acupple.dashcam.services.hbase.RawLogDao;
import org.mokey.acupple.dashcam.services.hbase.models.RawLog;
import org.mokey.acupple.dashcam.services.models.LogSearchParam;
import org.mokey.acupple.dashcam.services.models.LogIndex;
import org.mokey.acupple.dashcam.services.models.LogIndexResult;
import org.mokey.acupple.dashcam.services.models.LogSearchResult;
import org.mokey.acupple.dashcam.services.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Yuan on 2015/7/2.
 */
public class LogEventServiceImpl implements LogEventService {
	private static final Logger logger = LoggerFactory
			.getLogger(LogEventServiceImpl.class);

	/**
	 * ES Index storage dao
	 */
	private LogIndexDao indexDao;

	/**
	 * HBase raw log storage dao
	 */
	private RawLogDao rawLogDao;

	@Override
	public int insert(int appId, String envGroup, String env, String hostName,
			String hostIp, List<LogEvent> logEvents) {
		int count = 0;
		if (!Strings.isNullOrEmpty(hostIp) && logEvents != null) {

			List<LogIndex> indexes = rawLogDao.insert(appId, envGroup, env,
					hostName, hostIp, logEvents);
			count = indexDao.insert(indexes);
		}
		return count;
	}

	@Override
	public LogSearchResult search(LogSearchParam param, int lastResultIndex,
			int limit) {
		if (param == null) {
			return null;
		}
		if (lastResultIndex <= 0) {
			lastResultIndex = 0;
		}
		LogSearchResult result = new LogSearchResult();
		result.setLogs(new ArrayList<RawLog>());

		// Now we reach the shit code
		boolean enough = false;
		long begin = System.currentTimeMillis();
		int N = 0;
		while (!enough) {
			LogIndexResult searchResult = indexDao.search(param, lastResultIndex, limit);
			if (searchResult == null || searchResult.getHits() == null) {
				enough = true;
				break;
			}

			result.setTotalCount(searchResult.getTotalCount());

			// 将rowkey字符串化后，存入Map，后续聚合的时候方便查询
			List<byte[]> rowkeys = Lists.newArrayList();
			Map<String, SearchHit> hits = Maps.newHashMap();
			for (SearchHit hit : searchResult.getHits()) {
				String rowkey = hit.getSource().get("rowkey").toString();
				rowkeys.add(Base64.decode(rowkey));
				hits.put(rowkey, hit);
			}

			List<RawLog> logs = rawLogDao.search(rowkeys, param.getMessage());

			// 场景分析：设某一个查询条件在ES中匹配的总数有S个，limit设置为L, 本次ES查询到了X个, 之前的ES的index为I <= K*X，
			// 如果 X < L怎么办？
			// 如果hbase的前K次查到的数量为N，第K+1次查询是M
			// a. 如果 N + M >= L, 其中 N < L, M <= L
			// 则从M中取出 L - N个填充进去，剩余的 M - (L - N) 个丢弃，且记录下来当前ES的index，为 I + L - N
			// b. 如果 N + M < L，且 I + L >= S， 则查询停止，且符合条件的数量为N+M,
			// hasMore为false, ES的index记录为 I + L

			if (null != logs) {
				int take = 0;
				if ((N + logs.size()) >= limit) {
					take = limit - N;
					enough = true;
					lastResultIndex += take;
				}
				else {
					take = logs.size();
					lastResultIndex += searchResult.getHits().length;
				}
				for (int i = 0; i < take; i++) {
					RawLog log = logs.get(i);
					LogIndex index = JsonUtil.toLog(hits.get(
							Base64.encodeBytes(log.getRowKey()))
							.getSourceAsString());
					try {
						PropertyUtils.copyProperties(log, index);
					} catch (IllegalAccessException | InvocationTargetException
							| NoSuchMethodException e) {
						logger.error("Failed to copy LogIndex to RawLog", e);
					}
					result.getLogs().add(log);
				}
				N += take;
			}else{
				lastResultIndex += searchResult.getHits().length;
			}
			
			result.setLastResultIndex(lastResultIndex);
			result.setHasMoreResult(result.getLastResultIndex() < result
					.getTotalCount());
			result.setTimeOut(!enough
					&& (System.currentTimeMillis() - begin) >= CommonConstants.SEARCH_TIMEOUT);
			if(result.isTimeOut() || !result.isHasMoreResult()){
				enough = true;
			}
		}

		return result;
	}

	@Override
	public LogSearchResult search(List<byte[]> rowkeys) {
		LogSearchResult result = new LogSearchResult();
		result.setLogs(new ArrayList<RawLog>());
		if (rowkeys != null && !rowkeys.isEmpty()) {
			for (byte[] rowkey : rowkeys) {
				RawLog log = rawLogDao.get(rowkey);
				if (log != null) {
					result.getLogs().add(log);
				}
			}
		}
		result.setTotalCount(result.getLogs().size());
		return result;
	}
}
