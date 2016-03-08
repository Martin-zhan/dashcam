package org.mokey.acupple.dashcam.agent;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.YieldingWaitStrategy;
import org.mokey.acupple.dashcam.agent.conf.DashcamProperties;
import org.mokey.acupple.dashcam.agent.works.DashcamThreadFactory;
import org.mokey.acupple.dashcam.agent.works.events.KafkaEvent;
import org.mokey.acupple.dashcam.agent.works.events.TBaseEvent;
import org.mokey.acupple.dashcam.agent.works.handler.KafkaEventHandler;
import org.mokey.acupple.dashcam.agent.works.handler.TBaseEventSerializerHandler;
import org.mokey.acupple.dashcam.agent.works.producers.ChunkEventProducer;
import org.mokey.acupple.dashcam.agent.works.producers.KafkaMessageProducer;
import org.mokey.acupple.dashcam.common.models.thrift.*;
import org.mokey.acupple.dashcam.common.utils.HostUtil;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.lmax.disruptor.RingBuffer.createMultiProducer;


/**
 * Created by Forest on 2016/1/19.
 */
public class AgentManager {
    private static AgentManager instance = new AgentManager();
    private static Logger logger = Logger.getLogger(AgentManager.class.getName());
    private AgentManager(){
        this.kafkaMessageProducer.setData(tBaseEventSerializerHandler.serializer(getEnvironmentChunk()));

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    public static AgentManager getInstance(){
        return instance;
    }

    private static final int BUFFER_SIZE = 1024 * 8;
    private static final int NUM_EVENT_PROCESSORS = 2;
    private final ExecutorService executor = Executors.newFixedThreadPool(NUM_EVENT_PROCESSORS, DashcamThreadFactory.getInstance());

    private final RingBuffer<TBaseEvent> ringBuffer =
            createMultiProducer(TBaseEvent.FACTORY, BUFFER_SIZE, new YieldingWaitStrategy());
    private final RingBuffer<KafkaEvent> kafkaRingBuffer =
            createMultiProducer(KafkaEvent.FACTORY, BUFFER_SIZE, new YieldingWaitStrategy());

    private final SequenceBarrier serializerSequenceBarrier = ringBuffer.newBarrier();
    private final KafkaMessageProducer kafkaMessageProducer = new KafkaMessageProducer(kafkaRingBuffer);
    private final TBaseEventSerializerHandler tBaseEventSerializerHandler = new TBaseEventSerializerHandler(kafkaMessageProducer);
    private final KafkaEventHandler kafkaEventHandler = new KafkaEventHandler();

    private final BatchEventProcessor<TBaseEvent> serializerBatchProcessor =
            new BatchEventProcessor<TBaseEvent>(ringBuffer, serializerSequenceBarrier, tBaseEventSerializerHandler);

    private final SequenceBarrier kafkaSequenceBarrier = kafkaRingBuffer.newBarrier();
    private final BatchEventProcessor<KafkaEvent> kafkaBatchProcessor =
            new BatchEventProcessor<KafkaEvent>(kafkaRingBuffer, kafkaSequenceBarrier, kafkaEventHandler);

    {
        ringBuffer.addGatingSequences(serializerBatchProcessor.getSequence());
        kafkaRingBuffer.addGatingSequences(kafkaBatchProcessor.getSequence());

        executor.submit(serializerBatchProcessor);
        executor.submit(kafkaBatchProcessor);
    }

    private final ChunkEventProducer chunkEventProducer = new ChunkEventProducer(ringBuffer);

    public ChunkEventProducer getMessageProducer(){
        return chunkEventProducer;
    }

    private Chunk getEnvironmentChunk(){
        Chunk chunk = new Chunk();
        chunk.setEnvGroup(DashcamProperties.GET().getEnvGroup());
        chunk.setEnv(DashcamProperties.GET().getEnv());
        chunk.setHostIp(HostUtil.getHostIp());
        chunk.setHostName(HostUtil.getHostName());
        chunk.setAppId(DashcamProperties.GET().getAppId());
        chunk.setLogEvents(new ArrayList<LogEvent>());
        chunk.setMetrics(new ArrayList<MetricEvent>());
        chunk.setSpans(new ArrayList<Span>());
        chunk.setEvents(new ArrayList<Event>());

        return chunk;
    }

    /**
     * Terminate the log agent and wait kafka send the rest messages
     */
    public void shutdown(){
        serializerBatchProcessor.halt();
        kafkaBatchProcessor.halt();

        executor.shutdown();
        while (true){
            if(executor.isTerminated()){
                tBaseEventSerializerHandler.shutdown();
                kafkaEventHandler.shutdown();
                break;
            }

            try{
                Thread.sleep(500);
            }catch (Exception e){}
        }

        logger.log(Level.INFO, "Dashcam agent has bee shutdown");
    }

    /**
     * Terminate the log agent, some logs will be rejected if not send to kafka clauses.
     */
    public void shutdownNow(){
        serializerBatchProcessor.halt();
        kafkaBatchProcessor.halt();
        executor.shutdown();
        tBaseEventSerializerHandler.shutdown();
        kafkaEventHandler.shutdown();
        logger.log(Level.INFO, "Dashcam agent has bee shutdown");
    }
}
