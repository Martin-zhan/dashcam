package org.mokey.acupple.dashcam.services.constants;

import org.apache.hadoop.hbase.util.Bytes;

public class TraceConstants {
	
	public static final String TRACE_TABLE_NAME = "dashcam_trace";
	
	public static final String SPAN_TABLE_NAME = "dashcam_span";
	
	
	public static final byte[] traceColumnFamily = Bytes.toBytes("trace");
	public static final byte[] spanColumnFamily = Bytes.toBytes("span");
	
	public static final byte[] rootSpan = Bytes.toBytes("spanroot");
	
	
	
}
