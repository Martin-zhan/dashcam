package org.mokey.acupple.dashcam.agent.works.events;

import com.lmax.disruptor.EventFactory;

/**
 * Created by Forest on 2016/1/21.
 */
public class KafkaEvent {
    private byte[] message;

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public static EventFactory<KafkaEvent> FACTORY = new EventFactory<KafkaEvent>() {
        @Override
        public KafkaEvent newInstance() {
            return new KafkaEvent();
        }
    };
}
