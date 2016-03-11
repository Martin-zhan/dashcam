package org.mokey.acupple.dashcam.services.constants;

import org.apache.hadoop.hbase.util.Bytes;

public class LogHostConstants {
	public static final String HOST_TABLE = "dashcam_host";
	public static final int LOGHOST_ROWKEY_LEN = 4 + 4 + 4 + 4 + 4;
	
	public static final byte[] hostFamily = Bytes.toBytes("hostinfo");
	
	public static final byte[] envgroup = Bytes.toBytes("envgroup");
	public static final byte[] env = Bytes.toBytes("env");
	public static final byte[] hostip = Bytes.toBytes("hostip");
	public static final byte[] hostname = Bytes.toBytes("hostname");
}
