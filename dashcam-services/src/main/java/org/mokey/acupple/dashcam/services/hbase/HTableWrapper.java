package org.mokey.acupple.dashcam.services.hbase;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HTableWrapper {

	private static final Logger logger = LoggerFactory
			.getLogger(HTableWrapper.class);

	private static Connection connection = null;

	static {
		try {
			org.apache.hadoop.conf.Configuration conf = HBaseConfiguration.create();
			conf.set("hbase.zookeeper.quorum", "");
			conf.set("zookeeper.znode.parent", "/hbase");
			connection = ConnectionFactory.createConnection(conf);
		} catch (IOException e) {
			logger.error("Failed to create hbase connection", e);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				/*CamUtil.close(connection);*/
			}
		});
	}

	/**
	 * You must close the HTable by yourself
	 * 
	 * @return
	 * @throws IOException
	 */
	public static Table getInstance(String tableName) throws IOException {
		return connection.getTable(TableName.valueOf(tableName));
	}


}
