package org.mokey.acupple.dashcam.services.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.hadoop.hbase.util.Base64;
import org.mokey.acupple.dashcam.common.models.TraceResult;
import org.mokey.acupple.dashcam.common.models.thrift.Span;
import org.mokey.acupple.dashcam.services.TraceService;
import org.mokey.acupple.dashcam.services.hbase.RawLogDao;
import org.mokey.acupple.dashcam.services.hbase.TraceDao;
import org.mokey.acupple.dashcam.services.models.ExtendSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TraceServiceImpl implements TraceService {

	private static final Logger logger = LoggerFactory
			.getLogger(TraceServiceImpl.class);

	private RawLogDao rawLogDao;

	private TraceDao traceDao;

	@Override
	public int persist(int appId, String envGroup, String env, String hostName,
			String hostIp, Span span) {
		return traceDao.addSpan(appId, envGroup, env, hostName, hostIp, span);
	}

	@Override
	public int persist(int appId, String envGroup, String env, String hostName,
			String hostIp, List<Span> spans) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TraceResult search(long traceId) throws InvalidObjectException {
		List<ExtendSpan> extendSpans = traceDao.search(traceId);

		Map<Long, List<ExtendSpan>> maps = Maps.newHashMap();
		Map<Long, ExtendSpan> spanMaps = Maps.newHashMap();
		Map<Long, TraceResult> results = Maps.newHashMap();
		TraceResult root = new TraceResult();

		for (ExtendSpan span : extendSpans) {
			if (!span.isRoot()) {
				if (maps.containsKey(span.getParentId())) {
					maps.get(span.getParentId()).add(span);
				} else {
					List<ExtendSpan> secondSpan = Lists.newArrayList();
					secondSpan.add(span);
					maps.put(span.getParentId(), secondSpan);
				}
			} else {
				fill(root, span);
				results.put(span.getSpanId(), root);
			}
			spanMaps.put(span.getSpanId(), span);
		}

		for (Map.Entry<Long, List<ExtendSpan>> entry : maps.entrySet()) {
			if (!results.containsKey(entry.getKey())) {
				TraceResult parentResult = new TraceResult();
				if (!spanMaps.containsKey(entry.getKey())) {
					logger.error("Missing span id: " + entry.getKey());
					throw new InvalidObjectException("Missing span id: "
							+ entry.getKey());
				}
				fill(parentResult, spanMaps.get(entry.getKey()));
				results.put(entry.getKey(), parentResult);
			}
			for (ExtendSpan span : entry.getValue()) {
				TraceResult innerResult;
				if (!results.containsKey(span.getSpanId())) {
					innerResult = new TraceResult();
					fill(innerResult, span);
					results.put(span.getSpanId(), innerResult);
				}
				innerResult = results.get(span.getSpanId());
				results.get(entry.getKey()).getChildren().add(innerResult);
			}
		}

		return root;
	}

	/**
	 * 该方法希望用于替代 search方法里面的代码，考虑到更改有风险，因此仅保留该代码，请勿删除！！！！！！！！！！！
	 * @param extendSpans
	 * @return
	 */
	private TraceResult build(List<ExtendSpan> extendSpans) {

		Map<Long, ExtendSpan> spanMaps = Maps.newHashMap();
		Map<Long, TraceResult> results = Maps.newHashMap();
		TraceResult root = new TraceResult();

		for (ExtendSpan span : extendSpans) {
			if (span.isRoot()) {
				fill(root, span);
				results.put(span.getSpanId(), root);
			}
			spanMaps.put(span.getSpanId(), span);
		}

		for (ExtendSpan span : extendSpans) {
			if (!results.containsKey(span.getSpanId())) {
				TraceResult result = new TraceResult();
				fill(result, span);
				results.put(span.getSpanId(), result);
			}
			if (!results.containsKey(span.getParentId())) {
				TraceResult result = new TraceResult();
				fill(result, spanMaps.get(span.getParentId()));
				results.put(span.getParentId(), result);
			}
			results.get(span.getParentId()).getChildren()
					.add(results.get(span.getSpan()));
		}

		return root;
	}

	private void fill(TraceResult result, ExtendSpan span) {
		result.setAppId(Integer.parseInt(span.getSpan().getAppId()));
		result.setSpanType(span.getSpan().getSpanType().name());
		result.setHostIp(span.getSpan().getHostIp());
		result.setServiceName(span.getSpan().getServiceName());
		result.setStartTime(span.getSpan().getStartTime());
		result.setHostName(span.getSpan().getHostName());
		result.setSpanName(span.getSpan().getName());
		result.setEndTime(span.getSpan().getStopTime());
		result.setRowkeys(base64(span.getRowkeys()));
		if (result.getChildren() == null) {
			result.setChildren(new ArrayList<TraceResult>());
		}
	}

	private List<String> base64(List<byte[]> rowkeys) {
		List<String> rowkeyStrs = new ArrayList<>();
		if (rowkeys == null || rowkeys.isEmpty()) {
			return rowkeyStrs;
		}
		for (byte[] bytes : rowkeys) {
			rowkeyStrs.add(Base64.encodeBytes(bytes));
		}
		return rowkeyStrs;
	}
}
