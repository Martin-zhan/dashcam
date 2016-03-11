package org.mokey.acupple.dashcam.services.utils;

import com.google.common.collect.Sets;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by enousei on 3/10/16.
 */
public class LogId {
    private static LogId logId = new LogId("");

    private final static byte maxId = Byte.MAX_VALUE;
    private final static String instances = "/iqunxing/writers";

    private long id = 1L;
    private CuratorFramework curator;
    private AtomicLong incrementId = new AtomicLong(0);

    private LogId(String zk_address){}

    public static LogId getLogId(){
        return logId;
    }

    public void init(String zk_address) throws Exception {
        curator = CuratorFrameworkFactory.newClient(zk_address, 30000, 30000,
                new ExponentialBackoffRetry(1000, 3));
        curator.start();

        boolean valid = false;
        Stat stat = curator.checkExists().forPath(instances);
        if (null != stat) {
            List<String> children = curator.getChildren().forPath(instances);
            Set<String> childrenSet = Sets.newHashSet(children);
            for (byte from = 1; from <= maxId; from++) {
                if (!childrenSet.contains(String.valueOf(from))) {
                    id = from;
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                throw new Exception("No more space for the writer, allow max to 255 instances");
            }
        }

        curator.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL).forPath(instances + "/" + id);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (null != curator) {
                    curator.close();
                }
            }
        });
    }

    /**
     * 很复杂的ID算法，首先用writer的id限制高8位，
     *
     * 有这样一种场景，agent在同一个时间发送了10条日志，但是writer启动后只读了5条就挂了，这5条从0开始计数，再次启动之后，
     * writer的id相同，但是还是从0开始计数，此时就和之前的5条重复了，这种场景很难发生，但是为了避免这种情况，采用如下设计：
     *
     * writerID + day_of_week + hour + minute + second + incrementID
     *
     * 8 + 3 + 5 + 6 + 6 + 36, 36位最大值为 68719476736，同一秒百亿级别，足够使用了
     *
     * 这种设计有如下要求：
     *
     * 1. writer的进程数不能超过255 2.
     * 一条日志从发出之日起，必须在一周之内消费完，这个要求一定能达到，因为kafka现在的超时时间设置为7天
     * @return
     */
    public long nextId() {
        long currentId = incrementId.incrementAndGet();

        Calendar calendar = Calendar.getInstance();
        long dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        long hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        long minute = calendar.get(Calendar.MINUTE);
        long second = calendar.get(Calendar.SECOND);

        // 左移28位，再右移28位，将高28位全部置为0

        long shiftId = id << 56;
        long shiftDay = (dayOfWeek << 61) >> 8;
        long shiftHour = (hourOfDay << 59) >> 11;
        long shiftMinute = (minute << 58) >> 16;
        long shiftSecond = (second << 58) >> 22;
        currentId = (currentId << 28) >> 28;
        // 将id左移56位，再合并currentId
        long nextSid = shiftId | shiftDay | shiftHour | shiftMinute
                | shiftSecond | currentId;
        return nextSid;
    }

    /**
     * Return server Id
     * @return
     */
    public long getServerId() {
        return id;
    }
}
