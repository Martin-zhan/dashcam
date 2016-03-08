package org.mokey.acupple.dashcam.agent.works.events;

import com.lmax.disruptor.EventFactory;
import org.mokey.acupple.dashcam.common.models.thrift.Chunk;

/**
 * Created by Forest on 2016/1/21.
 */
public class TChunkEvent {
    private Chunk chunk;

    public Chunk getChunk() {
        return chunk;
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public static EventFactory<TChunkEvent> FACTORY = new EventFactory<TChunkEvent>() {
        @Override
        public TChunkEvent newInstance() {
            return new TChunkEvent();
        }
    };
}
