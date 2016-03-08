package org.mokey.acupple.dashcam.agent.works;

import java.util.concurrent.ThreadFactory;

/**
 * Created by Forest on 2016/1/20.
 */
public class DashcamThreadFactory implements ThreadFactory{
    private static String NAME = "Dashcam-Thread-";
    private static int count = 0;

    private DashcamThreadFactory(){}

    private static DashcamThreadFactory instance = new DashcamThreadFactory();

    public static ThreadFactory getInstance(){
        return instance;
    }

    @Override
    public Thread newThread(Runnable run) {
        Thread thread =  new Thread(run, NAME + count ++);
        thread.setDaemon(true);
        return  thread;
    }
}
