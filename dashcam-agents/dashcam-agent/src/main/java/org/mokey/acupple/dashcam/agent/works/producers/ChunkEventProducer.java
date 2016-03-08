package org.mokey.acupple.dashcam.agent.works.producers;

import com.lmax.disruptor.RingBuffer;
import org.apache.thrift.TBase;
import org.mokey.acupple.dashcam.agent.Metrics;
import org.mokey.acupple.dashcam.agent.works.events.TBaseEvent;

/**
 * Created by Forest on 2016/1/21.
 */
public class ChunkEventProducer {
    private final RingBuffer<TBaseEvent> ringBuffer;

    public ChunkEventProducer(RingBuffer<TBaseEvent> ringBuffer){
        this.ringBuffer = ringBuffer;
    }

    public void put(TBase base) {
        long sequence = ringBuffer.next();
        try
        {
            TBaseEvent event = ringBuffer.get(sequence);
            event.setBase(base);
        }
        finally
        {
            ringBuffer.publish(sequence);
            Metrics.instance().getPutQueueCounter().incrementAndGet();
        }
    }
}
