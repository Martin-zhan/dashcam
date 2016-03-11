package org.mokey.acupple.dashcam.services.constants;

import org.apache.hadoop.hbase.util.Bytes;

public class RawLogConstants {
	// Please refer to http://confluence.iqunxing.com/display/QX/HBase+Schema
	public static final String RAWLOG_TABLE = "dashcam_rawlog";
	public static final int RAWLOG_ROWKEY_LEN = 4 + 4 + 1 + 8 + 8;
	
	public static final byte[] contentFamily = Bytes.toBytes("content");
	public static final byte[] tagFamily = Bytes.toBytes("tag");
	
	public static final byte[] envgroup = Bytes.toBytes("envgroup");
	public static final byte[] env = Bytes.toBytes("env");
	public static final byte[] logtime = Bytes.toBytes("logtime");
	public static final byte[] traceid = Bytes.toBytes("traceid");
	public static final byte[] spanid = Bytes.toBytes("spanid");
	public static final byte[] logtype = Bytes.toBytes("logtype");
	public static final byte[] loglevel = Bytes.toBytes("loglevel");
    public static final byte[] source = Bytes.toBytes("source");
	public static final byte[] title = Bytes.toBytes("title");
	public static final byte[] message = Bytes.toBytes("message");
	public static final byte[] hostip = Bytes.toBytes("hostip");
	public static final byte[] hostname = Bytes.toBytes("hostname");
}
