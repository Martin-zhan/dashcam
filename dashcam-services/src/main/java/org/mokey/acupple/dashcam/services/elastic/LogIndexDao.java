package org.mokey.acupple.dashcam.services.elastic;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.Base64;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.mokey.acupple.dashcam.services.models.LogSearchParam;
import org.mokey.acupple.dashcam.common.utils.NetAddress;
import org.mokey.acupple.dashcam.common.utils.NetAddressList;
import org.mokey.acupple.dashcam.common.utils.Strings;
import org.mokey.acupple.dashcam.services.models.LogIndex;
import org.mokey.acupple.dashcam.services.models.LogIndexResult;
import org.mokey.acupple.dashcam.services.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.mokey.acupple.dashcam.common.utils.DateUtil;

import java.util.*;

/**
 * Created by Yuan on 2015/7/2.
 */
public class LogIndexDao {

	private static final Logger logger = LoggerFactory.getLogger(LogIndexDao.class);
	private final int maxRetry = 3;
	private final String clusterName = "dashcam-es";

	private TransportClient client;

	public LogIndexDao(String hosts) {
		NetAddressList addresses = new NetAddressList(hosts);
		Settings settings =  ImmutableSettings.settingsBuilder().put("cluster.name", clusterName).build();
		client = new TransportClient(settings);
		for (NetAddress address : addresses.getAddresses()) {
			client.addTransportAddress(new InetSocketTransportAddress(address
					.getHost(), address.getPort()));
		}
	}

	public LogIndexResult search(LogSearchParam param, int lastResultIndex, int limit) {
		LogIndexResult result = new LogIndexResult();
		try {
			Set<String> indexNames = getIndexNames(param.getFromDate(), param.getToDate());
			String[] idxes = new String[indexNames.size()];
			indexNames.toArray(idxes);
			FilterBuilder filterBuilder = new SearchBuilder().buildFilter(param);
			SearchResponse response = client.prepareSearch(idxes)
					.setTypes(String.valueOf(param.getAppId()))
					.setPostFilter(filterBuilder).setFrom(lastResultIndex)
					.addSort("timestamp", SortOrder.DESC).setSize(limit)
					.execute().actionGet();
			result.setTimeout(response.isTimedOut());
			if (!result.isTimeout()) {
				result.setTotalCount(response.getHits().getTotalHits());
				result.setHits(response.getHits().getHits());
			}
		} catch (Exception e) {
			logger.error("RawLog search from ES failed", e);
		}
		return result;
	}

	public int insert(List<LogIndex> indexes) {
		if (indexes == null || indexes.isEmpty()) {
			return 0;
		}
		int count = 0;
		BulkRequestBuilder bulk = client.prepareBulk();
		Map<String, String[]> docs = Maps.newHashMap();
		for (LogIndex index : indexes) {
			String json = JsonUtil.toJson(index);
			String id = Base64.encodeBytes(index.getRowkey());
			if (!Strings.isNullOrEmpty(json)) {
				String[] backups = new String[3];
				String indexName = getIndexName(new Date(index.getTimestamp()));
				backups[0] = indexName;
				backups[1] =  index.getAppId() + "";
				backups[2] = json;
				bulk.add(client.prepareIndex(indexName, index.getAppId() + "", id).setSource(json));
				docs.put(id, backups);
				count++;
			} else {
				logger.error("This one proves to be error: "
						+ Base64.encodeBytes(index.getRowkey()));
			}
		}
		if (count > 0) {
			boolean needToDo = true;
			int currentRetry = 0;
			while (needToDo) {
				List<String> failedIds = doBulk(bulk);
				if (failedIds.size() == 0) {
					needToDo = false;
					if (currentRetry > 0) {
						logger.warn(String.format(
								"We finally insert success with %d retries",
								currentRetry));
					}
				} else {
					// 如果前几次尝试都没成功，我们再试一次
					bulk = client.prepareBulk();
					for (String id : failedIds) {
						bulk.add(client.prepareIndex(docs.get(id)[0], docs.get(id)[1], docs.get(id)[2]));
					}
				}
				currentRetry++;
				if (currentRetry >= maxRetry) {
					StringBuilder sb = new StringBuilder();
					for (String id : failedIds) {
						sb.append("\"");
						sb.append(id);
						sb.append("\"");
						sb.append(",");
					}
					logger.error(String.format(
							"Now we retry %d times, failed, ids are: %s",
							currentRetry, sb.toString()));
					count -= failedIds.size();
					needToDo = false;
				}
			}
		}
		return count;
	}

	private List<String> doBulk(BulkRequestBuilder bulk) {
		BulkResponse response = bulk.execute().actionGet();
		List<String> failedIds = Lists.newArrayList();
		for (BulkItemResponse itemResponse : response.getItems()) {
			if (itemResponse.isFailed()) {
				String failMsg = itemResponse.getFailureMessage();
				failedIds.add(itemResponse.getId());
				if (!Strings.isNullOrEmpty(failMsg)) {
					logger.error("Elastic Search insert error: " + failMsg);
				} else {
					logger.error("Unknow Elastic Search error");
				}
			}
		}
		return failedIds;
	}

	private Set<String> getIndexNames(Date from, Date to){
		Set<String> indexNames = new HashSet<>();
		Date beforeDay = to;
		do {
			indexNames.add(getIndexName(beforeDay));
			beforeDay = DateUtil.getNextDay(beforeDay, -1);
		}while (beforeDay.compareTo(from) >= 0);
		return indexNames;
	}

	private String getIndexName(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.WEEK_OF_YEAR);
	}
}
