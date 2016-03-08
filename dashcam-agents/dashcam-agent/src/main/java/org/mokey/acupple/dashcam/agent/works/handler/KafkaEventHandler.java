package org.mokey.acupple.dashcam.agent.works.handler;

import com.lmax.disruptor.EventHandler;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.mokey.acupple.dashcam.agent.conf.DashcamProperties;
import org.mokey.acupple.dashcam.agent.works.events.KafkaEvent;
import org.mokey.acupple.dashcam.common.utils.NetAddressList;

import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mokey.acupple.dashcam.common.Constants.MSG_TOPIC;

/**
 * Created by Forest on 2016/1/21.
 */
public class KafkaEventHandler  implements EventHandler<KafkaEvent> {
    private static Logger logger = Logger.getLogger(KafkaEventHandler.class.getName());
    private static final int CLEAR_INTERVAL = 500;
    private Producer<String, byte[]> producer;

    public KafkaEventHandler(){
        String brokerList = DashcamProperties.GET().getBrokerList();
        try{
            Properties props = new Properties();
            NetAddressList addressList = new NetAddressList(brokerList);
            props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, addressList.toString());
            this.producer = new KafkaProducer<>(props, new StringSerializer(), new ByteArraySerializer());
            logger.log(Level.INFO, "Kafka client has been initialized successfully: " + brokerList);
        }catch (Exception e){
            logger.log(Level.SEVERE, "Kafka client has been initialized failed: " + brokerList);
            e.printStackTrace();
        }
    }

    @Override
    public void onEvent(KafkaEvent event, long sequence, boolean endOfBatch) throws Exception {
        if(event.getMessage().length > 0) {
            producer.send(new ProducerRecord<>(MSG_TOPIC, UUID.randomUUID().toString(), event.getMessage()));
        }
    }

    public void shutdown(){
        try{
            Thread.sleep(CLEAR_INTERVAL);
        }catch (Exception e){}
        producer.close();
    }
}
