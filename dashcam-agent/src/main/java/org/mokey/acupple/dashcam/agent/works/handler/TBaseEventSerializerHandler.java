package org.mokey.acupple.dashcam.agent.works.handler;

import com.lmax.disruptor.EventHandler;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.mokey.acupple.dashcam.agent.Metrics;
import org.mokey.acupple.dashcam.agent.conf.DashcamProperties;
import org.mokey.acupple.dashcam.agent.works.ChunkBuilder;
import org.mokey.acupple.dashcam.agent.works.DashcamThreadFactory;
import org.mokey.acupple.dashcam.agent.works.events.TBaseEvent;
import org.mokey.acupple.dashcam.agent.works.producers.KafkaMessageProducer;
import org.mokey.acupple.dashcam.common.models.thrift.Chunk;
import org.mokey.acupple.dashcam.common.models.thrift.Span;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Forest on 2016/1/21.
 */
public class TBaseEventSerializerHandler implements EventHandler<TBaseEvent> {

    private static Logger logger = Logger.getLogger(TBaseEventSerializerHandler.class.getName());

    private static final int CLEAR_INTERVAL = 500;
    private ChunkBuilder chunkBuilder = new ChunkBuilder();
    private AtomicBoolean running = new AtomicBoolean(true);
    private AtomicLong start = new AtomicLong(System.currentTimeMillis());
    private final KafkaMessageProducer kafkaMessageProducer;
    private final Thread clearThread = DashcamThreadFactory.getInstance().newThread(new Runnable() {
        @Override
        public void run() {
            while (running.get()){
                try{
                    if(System.currentTimeMillis() - start.get() >= CLEAR_INTERVAL
                            && chunkBuilder.getChunkSize() > 0) {
                        clear();
                    }
                    Thread.sleep(CLEAR_INTERVAL);
                }catch (Exception e){}
            }
        }
    });

    {
        clearThread.start();
    }

    private final ThreadLocal<TSerializer> serializer = new ThreadLocal<TSerializer>(){
        @Override
        protected TSerializer initialValue() {
            return new TSerializer();
        }
    };

    public TBaseEventSerializerHandler(KafkaMessageProducer kafkaMessageProducer){
        this.kafkaMessageProducer = kafkaMessageProducer;
    }

    @Override
    public void onEvent(TBaseEvent event,long sequence, boolean endOfBatch) throws Exception {
        Metrics.instance().getPollQueueCounter().incrementAndGet();
        this.chunkBuilder.putMsg(event.getBase());
        if(chunkBuilder.getChunkSize() >= DashcamProperties.GET().getChunkSize()) {
            this.clear();
        }
    }

    public byte[] serializer(Chunk chunk){
        try {
            return this.serializer.get().serialize(chunk);
        } catch (TException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    private synchronized void clear(){
        try {
            //Record metrics
            Metrics.instance().getSendLogEventCounter().addAndGet(chunkBuilder.getChunk().getLogEventsSize());
            Metrics.instance().getSpanCounter().addAndGet(chunkBuilder.getChunk().getSpansSize());
            if(chunkBuilder.getChunk().getSpansSize() > 0){
                for (Span span : chunkBuilder.getChunk().getSpans()){
                    Metrics.instance().getSendSpanLogEventCounter().addAndGet(span.getLogEventsSize());
                }
            }
            byte[] bytes = this.serializer.get().serialize(chunkBuilder.getChunk());
            kafkaMessageProducer.setData(bytes);
            start = new AtomicLong(System.currentTimeMillis());
            this.chunkBuilder.clear();
        } catch (TException e) {
            logger.log(Level.SEVERE, "Serialize chunk to bytes failure", e);
        }
    }

    public void shutdown(){
        running.set(false);
        try{
            Thread.sleep(CLEAR_INTERVAL);
        }catch (Exception ex){}
        clearThread.interrupt();
        if(chunkBuilder.getChunkSize() > 0){
            clear();
        }

        serializer.remove();
    }
}
