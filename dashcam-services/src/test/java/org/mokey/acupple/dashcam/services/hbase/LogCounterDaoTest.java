package org.mokey.acupple.dashcam.services.hbase;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mokey.acupple.dashcam.hbase.HFxClient;
import org.mokey.acupple.dashcam.services.hbase.models.*;
import org.mokey.acupple.dashcam.services.models.AggregationType;
import org.mokey.acupple.dashcam.services.utils.AggregateUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by enousei on 3/12/16.
 */
public class LogCounterDaoTest {
    private static final String zk_addr = "127.0.0.1:2181";
    private static final String hbase_root = "/hbase";
    private static HFxClient client;
    private static LogCounterDao dao;

    private final int info = 10;
    private final int warn = 15;
    private final int debug = 25;
    private final int error = 5;
    private final int fatal = 5;

    private final String devGroup = "DEV";
    private final String uatGroup = "UAT";


    @BeforeClass
    public static void setUp(){
        client = new HFxClient(zk_addr, hbase_root);
        dao = new LogCounterDao(client);

        try {
            client.createTable(AppLogCounter.class);
            client.createTable(AppLogCounterHourly.class);
            client.createTable(AppLogCounterWithRateHourly.class);
            client.createTable(LogCounter.class);
            client.createTable(LogCounterHourly.class);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @AfterClass
    public static void clear(){
        try {
            client.deleteTable(AppLogCounter.class);
            client.deleteTable(AppLogCounterHourly.class);
            client.deleteTable(AppLogCounterWithRateHourly.class);
            client.deleteTable(LogCounter.class);
            client.deleteTable(LogCounterHourly.class);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void testDao(){
        long from = AggregateUtil.getMinutePart(System.currentTimeMillis());
        long to = increment(from, 9999);
        List<CounterInfo> countDev = dao.getCount("DEV", from, to, AggregationType.MINUTE);
        Assert.assertTrue(600 == countDev.size());
        List<CounterInfo> countUat = dao.getCount("UAT", from, to, AggregationType.MINUTE);
        Assert.assertTrue(600 == countUat.size());

        int mTotal = 0;
        for (CounterInfo info: countDev){
            mTotal += info.getTotal();
        }

        List<CounterInfo> countDevHour = dao.getCount("DEV", from, to, AggregationType.HOUR);
        int hTotal = 0;
        for (CounterInfo info: countDevHour){
            hTotal += info.getTotal();
        }

        Assert.assertTrue(mTotal == hTotal);

        to = increment(from, 8888);

        List<CounterInfo> rateDev = dao.getCount("DEV", from, to);
        int rTotal = 0;
        for (CounterInfo info: rateDev){
            rTotal += info.getTotal();
        }

        Assert.assertTrue(mTotal * 2 == rTotal);
    }

    private long increment(long from, int appId){
        Map<Long, AppLogCounter> devAppMLogs = new HashMap<>();
        Map<Long, AppLogCounter> uatAppMLogs = new HashMap<>();
        long to = from;
        for (int i = 0; i < 10; i++){ //10小时
            for (int j = 0; j < 60 * 2; j++) { //60分钟2次
                long minute = AggregateUtil.getMinutePart(to);
                AppLogCounter counter = devAppMLogs.get(minute);
                if(counter == null){
                    counter = new AppLogCounter(appId, devGroup, minute);
                    devAppMLogs.put(minute, counter);
                }

                counter.setTotal(info + debug + warn + error + fatal);
                counter.setInfo(info);
                counter.setDebug(debug);
                counter.setWarn(warn);
                counter.setError(error);
                counter.setFatal(fatal);

                to += 1000 * 10;

                AppLogCounter counterUat = uatAppMLogs.get(minute);
                if(counterUat == null){
                    counterUat = new AppLogCounter(appId, uatGroup, minute);
                    uatAppMLogs.put(minute, counterUat);
                }
                counterUat.setTotal(info + debug + warn + error + fatal);
                counterUat.setInfo(info);
                counterUat.setDebug(debug);
                counterUat.setWarn(warn);
                counterUat.setError(error);
                counterUat.setFatal(fatal);

                to += 1000 * 20;
            }
        }

        dao.increment(devAppMLogs);
        dao.increment(uatAppMLogs);

        return to;
    }
}
