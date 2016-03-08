package org.mokey.acupple.dashcam.agent.works.producers;


import com.lmax.disruptor.RingBuffer;
import org.mokey.acupple.dashcam.agent.works.events.KafkaEvent;

/**
 * Created by Forest on 2016/1/21.
 */
public class KafkaMessageProducer {
    private final RingBuffer<KafkaEvent> ringBuffer;

    public KafkaMessageProducer(RingBuffer<KafkaEvent> ringBuffer){
        this.ringBuffer = ringBuffer;
    }

    public void setData(byte[] bytes){
        long sequence = ringBuffer.next();
        try
        {
            KafkaEvent event = ringBuffer.get(sequence);
            event.setMessage(bytes);
        }
        finally
        {
            ringBuffer.publish(sequence);
        }
    }
}
