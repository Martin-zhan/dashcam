package org.mokey.acupple.dashcam.services;

import org.mokey.acupple.dashcam.common.models.TraceResult;
import org.mokey.acupple.dashcam.common.models.thrift.Span;

import java.io.InvalidObjectException;
import java.util.List;

/**
 * Created by Yuan on 2015/7/2.
 */
public interface TraceService {
	/**
	 * Persist trace span
	 * 
	 * @param span
	 * @return
	 */
	int persist(int appId, String envGroup, String env, String hostName,
				String hostIp, Span span);

	/**
	 * Persist trace span
	 * 
	 * @param spans
	 * @return
	 */
	int persist(int appId, String envGroup, String env, String hostName,
				String hostIp, List<Span> spans);

	/**
	 * Get all spans of a trace
	 * 
	 * @param traceId
	 * @return
	 */
	TraceResult search(long traceId) throws InvalidObjectException;

}
