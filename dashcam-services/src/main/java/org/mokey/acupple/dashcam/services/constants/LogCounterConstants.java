package org.mokey.acupple.dashcam.services.constants;

import org.apache.hadoop.hbase.util.Bytes;

public class LogCounterConstants {
	public static final int APP_COUNTER_ROWKEY_LEN = 4 + 4 + 4 + 8;
	public static final int COUNTER_ROWKEY_LEN = 4 + 8;
	public static final int COUNTER_RATE_ROWKEY_LEN = 4 + 8 + 4;
	
	/**
	 * the count of logs per minute, of all appid
	 */
	public static final String COUNTER_TABLE = "dashcam_counter";
	/**
	 * the count of logs per hour, of all appid
	 */
	public static final String HOUR_COUNTER_TABLE = "dashcam_hcounter";
	/**
	 * the count of logs per minute of a specified appid
	 */
	public static final String APP_COUNTER_TABLE = "dashcam_app_counter";
	/**
	 * the count of logs per hour of a specified appid
	 */
	public static final String APP_HOUR_COUNTER_TABLE = "dashcam_app_hcounter";
	/**
	 * similar to APP_HOUR_COUNTER_TABLE, but the rowkey is not the same
	 * structure
	 */
	public static final String HOUR_COUNTER_RATE_TABLE = "dashcam_hcounter_rate";
	
	public static final byte[] mCountFamily = Bytes.toBytes("mcount");
	public static final byte[] hCountFamily = Bytes.toBytes("hcount");
	public static final byte[] appMCountFamily = Bytes.toBytes("app_mcount");
	public static final byte[] appHCountFamily = Bytes.toBytes("app_hcount");
	public static final byte[] hCountRateFamily = Bytes.toBytes("hcount_rate");
	
	public static final byte[] minute = Bytes.toBytes("minute");
	public static final byte[] total = Bytes.toBytes("total");
	public static final byte[] debug = Bytes.toBytes("debug");
	public static final byte[] info = Bytes.toBytes("info");
	public static final byte[] warn = Bytes.toBytes("warn");
	public static final byte[] error = Bytes.toBytes("error");
	public static final byte[] fatal = Bytes.toBytes("fatal");
}
