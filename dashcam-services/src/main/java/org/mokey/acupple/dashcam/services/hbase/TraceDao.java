package org.mokey.acupple.dashcam.services.hbase;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.mokey.acupple.dashcam.common.models.thrift.LogEvent;
import org.mokey.acupple.dashcam.common.models.thrift.Span;
import org.mokey.acupple.dashcam.common.utils.CamUtil;
import org.mokey.acupple.dashcam.services.constants.TraceConstants;
import org.mokey.acupple.dashcam.services.elastic.LogIndexDao;
import org.mokey.acupple.dashcam.services.models.ExtendSpan;
import org.mokey.acupple.dashcam.services.models.LogIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

public class TraceDao {

	private static final Logger logger = LoggerFactory
			.getLogger(TraceDao.class);

	private static final ThreadLocal<TDeserializer> deserializer = new ThreadLocal<TDeserializer>() {
		@Override
		protected TDeserializer initialValue() {
			return new TDeserializer();
		}
	};

	private static final ThreadLocal<TSerializer> serializer = new ThreadLocal<TSerializer>() {
		@Override
		protected TSerializer initialValue() {
			return new TSerializer();
		}
	};

	private RawLogDao rawLogDao;

	private LogIndexDao indexDao;

	public int addSpan(int appId, String envGroup, String env,
			String hostName, String hostIp, Span span) {

		List<LogIndex> logIndexs = rawLogDao.insert(appId, envGroup, env,
				hostName, hostIp, span.getLogEvents());

		int count = indexDao.insert(logIndexs);
		
		//must clear it before save to hbase
		span.setLogEvents(new ArrayList<LogEvent>());

		byte[] traceHash = Bytes.toBytes(CamUtil.getHashCode(String
				.valueOf(span.getTraceId())));

		byte[] traceRowkey = CamUtil.concat(traceHash,
				Bytes.toBytes(span.getTraceId()));

		byte[] traceSpanHash = Bytes
				.toBytes(CamUtil.getHashCode(String.valueOf(span.getTraceId())
						+ String.valueOf(span.getSpanId())));

		byte[] spanRowkey = CamUtil.concat(traceSpanHash,
				Bytes.toBytes(span.getTraceId()),
				Bytes.toBytes(span.getSpanId()));

		byte[] qualifier = span.getParentId() > 0 ? Bytes.toBytes(span
				.getSpanId()) : TraceConstants.rootSpan;

		try {
			Put put = new Put(traceRowkey);
			put.addColumn(TraceConstants.traceColumnFamily, qualifier,
					serializer.get().serialize(span));
			try (Table table = HTableWrapper
					.getInstance(TraceConstants.TRACE_TABLE_NAME)) {
				table.checkAndPut(traceRowkey,
						TraceConstants.traceColumnFamily, qualifier, null, put);
			} catch (IOException ex) {
				logger.error("Failed to put into hbase", ex);
			}
		} catch (TException e) {
			logger.error("Failed to serialize span", e);
		}
		
		Put spanPut = new Put(spanRowkey);
		int logid = 0;
		for (LogIndex index : logIndexs) {
			spanPut.addColumn(TraceConstants.spanColumnFamily,
					Bytes.toBytes(logid++), index.getRowkey());
		}

		try (Table table = HTableWrapper
				.getInstance(TraceConstants.SPAN_TABLE_NAME)) {
			table.put(spanPut);
		} catch (IOException ex) {
			logger.error("Failed to put into hbase", ex);
		}

		return count;
	}

	public void putTrace(List<LogIndex> logIndexs, Span span){
		byte[] traceHash = Bytes.toBytes(CamUtil.getHashCode(String
				.valueOf(span.getTraceId())));

		byte[] traceRowkey = CamUtil.concat(traceHash,
				Bytes.toBytes(span.getTraceId()));

		byte[] traceSpanHash = Bytes
				.toBytes(CamUtil.getHashCode(String.valueOf(span.getTraceId())
						+ String.valueOf(span.getSpanId())));

		byte[] spanRowkey = CamUtil.concat(traceSpanHash,
				Bytes.toBytes(span.getTraceId()),
				Bytes.toBytes(span.getSpanId()));

		byte[] qualifier = span.getParentId() > 0 ? Bytes.toBytes(span
				.getSpanId()) : TraceConstants.rootSpan;

		try {
			Put put = new Put(traceRowkey);
			put.addColumn(TraceConstants.traceColumnFamily, qualifier,
					serializer.get().serialize(span));
			try (Table table = HTableWrapper
					.getInstance(TraceConstants.TRACE_TABLE_NAME)) {
				table.checkAndPut(traceRowkey,
						TraceConstants.traceColumnFamily, qualifier, null, put);
			} catch (IOException ex) {
				logger.error("Failed to put into hbase", ex);
			}
		} catch (TException e) {
			logger.error("Failed to serialize span", e);
		}

		Put spanPut = new Put(spanRowkey);
		int logid = 0;
		for (LogIndex index : logIndexs) {
			spanPut.addColumn(TraceConstants.spanColumnFamily,
					Bytes.toBytes(logid++), index.getRowkey());
		}

		try (Table table = HTableWrapper
				.getInstance(TraceConstants.SPAN_TABLE_NAME)) {
			table.put(spanPut);
		} catch (IOException ex) {
			logger.error("Failed to put into hbase", ex);
		}
	}

	public List<ExtendSpan> search(long traceId) {

		List<ExtendSpan> results = Lists.newArrayList();

		byte[] traceHash = Bytes.toBytes(CamUtil.getHashCode(String
				.valueOf(traceId)));

		byte[] traceRowkey = CamUtil
				.concat(traceHash, Bytes.toBytes(traceId));

		Get get = new Get(traceRowkey);
		try (Table table = HTableWrapper
				.getInstance(TraceConstants.TRACE_TABLE_NAME)) {
			Result result = table.get(get);

			NavigableMap<byte[], byte[]> columns = result
					.getFamilyMap(TraceConstants.traceColumnFamily);

			Map<String, ExtendSpan> spans = Maps.newHashMap();
			List<Long> spanIds = Lists.newArrayList();

			for (Entry<byte[], byte[]> entry : columns.entrySet()) {
				try {
					ExtendSpan extendSpan = new ExtendSpan();
					extendSpan.setRoot(Bytes.equals(entry.getKey(),
							TraceConstants.rootSpan));
					Span span = new Span();
					deserializer.get().deserialize(span, entry.getValue());
					extendSpan.setSpanId(span.getSpanId());
					extendSpan.setParentId(span.getParentId());
					extendSpan.setSpan(span);
					spanIds.add(span.getSpanId());
					spans.put(
							String.valueOf(span.getTraceId())
									+ String.valueOf(span.getSpanId()),
							extendSpan);
				} catch (TException e) {
					logger.error("Failed to deserialize from hbase", e);
				}
			}

			Map<String, List<byte[]>> rowkeys = getLogRowKeys(traceId, spanIds);

			for (Entry<String, List<byte[]>> entry : rowkeys.entrySet()) {
				spans.get(entry.getKey()).setRowkeys(entry.getValue());
			}

			results = Lists.newArrayList(spans.values());

		} catch (IOException ex) {
			logger.error("Failed to put into hbase", ex);
		}

		return results;
	}

	private Map<String, List<byte[]>> getLogRowKeys(long traceId,
			List<Long> spanIds) {

		Map<String, List<byte[]>> rowkeys = Maps.newHashMap();

		List<Get> gets = Lists.newArrayList();

		for (Long spanId : spanIds) {
			byte[] traceSpanHash = Bytes.toBytes(CamUtil.getHashCode(String
					.valueOf(traceId) + String.valueOf(spanId)));

			byte[] spanRowkey = CamUtil.concat(traceSpanHash,
					Bytes.toBytes(traceId), Bytes.toBytes(spanId));
			gets.add(new Get(spanRowkey));
		}

		try (Table table = HTableWrapper
				.getInstance(TraceConstants.SPAN_TABLE_NAME)) {
			Result[] results = table.get(gets);

			for (Result result : results) {
				NavigableMap<byte[], byte[]> columns = result
						.getFamilyMap(TraceConstants.spanColumnFamily);

				String key = String.valueOf(traceId)
						+ String.valueOf(Bytes.toLong(result.getRow(), 12, 8));
				rowkeys.put(key, Lists.newArrayList(columns.values()));
			}

		} catch (IOException ex) {
			logger.error("Failed to put into hbase", ex);
		}
		return rowkeys;
	}

}
