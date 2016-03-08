package org.mokey.acupple.dashcam.agent.works;

import org.apache.thrift.TBase;
import org.mokey.acupple.dashcam.agent.conf.DashcamProperties;
import org.mokey.acupple.dashcam.common.models.thrift.*;
import org.mokey.acupple.dashcam.common.utils.HostUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Yuan on 2015/7/14.
 */
public class ChunkBuilder {
    private static Logger logger = Logger.getLogger(ChunkBuilder.class.getName());
    private final static int MAX_SIZE = 1000;
    private Chunk chunk;
    private int chunkSize;

    public ChunkBuilder(){
        this.chunk = new Chunk();
        chunk.setEnvGroup(DashcamProperties.GET().getEnvGroup());
        chunk.setEnv(DashcamProperties.GET().getEnv());
        chunk.setHostIp(HostUtil.getHostIp());
        chunk.setHostName(HostUtil.getHostName());
        chunk.setAppId(DashcamProperties.GET().getAppId());
        chunk.setProcessId(0);

        List<LogEvent> logEventList = new LinkedList<LogEvent>();
        List<Span> spanList = new LinkedList<>();
        List<MetricEvent> metricList = new LinkedList<MetricEvent>();
        List<Event> eventList = new LinkedList<>();
        chunk.setLogEvents(logEventList);
        chunk.setSpans(spanList);
        chunk.setMetrics(metricList);
        chunk.setEvents(eventList);
    }

    public void clear(){
        this.chunk.getLogEvents().clear();
        this.chunk.getMetrics().clear();
        this.chunk.getSpans().clear();
        this.chunk.getEvents().clear();
        this.chunkSize = 0;
    }

    public void putMsg(TBase base){
        if(this.chunkSize >= MAX_SIZE){
            logger.log(Level.WARNING, "The volume of chunk is full, logs will be rejected!");
            return;
        }
        if(base instanceof LogEvent) {
            this.chunk.getLogEvents().add((LogEvent) base);
            this.chunkSize ++;
            return;
        }else if(base instanceof  MetricEvent){
            this.chunk.getMetrics().add((MetricEvent) base);
            this.chunkSize++;
            return;
        }else if(base instanceof  Span){
            this.chunk.getSpans().add((Span) base);
            this.chunkSize ++;
            return;
        }else if(base instanceof Event){
            this.chunk.getEvents().add((Event) base);
            this.chunkSize ++;
            return;
        }
    }

    public int getChunkSize(){
        return this.chunkSize;
    }

    public Chunk getChunk() {
        return chunk;
    }
}
