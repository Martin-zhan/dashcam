package org.mokey.acupple.dashcam.services;

import org.mokey.acupple.dashcam.common.models.LogSearchParam;
import org.mokey.acupple.dashcam.common.models.LogSearchResult;
import org.mokey.acupple.dashcam.common.models.thrift.LogEvent;

import java.util.List;

/**
 * Log services service
 * Created by Yuan on 2015/7/2.
 */
public interface LogEventService {
    /**
     * Batch insert LogEvent list according to appId, hostName and hostIP
     * @param appId App identity
     * @param env system environment(dev/pro)
     * @param hostName host name
     * @param hostIp host ipV4
     * @param logEvents Log instance list
     * @return count persisted successfully
     */
    int insert(int appId, String envGroup, String env, String hostName, String hostIp, List<LogEvent> logEvents);

    /**
     * Search Log instance according the search parameters, paging supported
     * @param param Log search parameters
     * @param lastResultIndex page index
     * @param limit page size
     * @return
     */
    LogSearchResult search(LogSearchParam param, int lastResultIndex, int limit);


    /**
     * Search long according to raw-keys
     * @param rowkeys
     * @return
     */
    LogSearchResult search(List<byte[]> rowkeys);
}
