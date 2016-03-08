package org.mokey.acupple.dashcam.agent.works.events;

import com.lmax.disruptor.EventFactory;
import org.apache.thrift.TBase;

/**
 * Created by Forest on 2016/1/19.
 */
public class TBaseEvent {
    private TBase base;

    public TBase getBase() {
        return base;
    }

    public void setBase(TBase base) {
        this.base = base;
    }

    public static EventFactory<TBaseEvent> FACTORY =  new EventFactory<TBaseEvent>(){
        @Override
        public TBaseEvent newInstance() {
            return new TBaseEvent();
        }
    };
}
