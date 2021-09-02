package si.iskratel.metricslib;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class KafkaClient {

    private static int kafkaClientsCount = 0;
    private int clientId;
    private Properties props = new Properties();
    private Producer<String, String> producer;
    private long msgCounter = 0L;

    public KafkaClient() {

        clientId = kafkaClientsCount++;

        props.put("bootstrap.servers", "centosvm:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<String, String>(props);

    }

    /**
     * Generic method to send any String message to Kafka topic. Message could be formatted as plain text, json,
     * or any other format; it is up to consumer how it will deserialize the message.
     * @param topic
     * @param message
     */
    public void sendMsg(String topic, String message) {

        producer.send(new ProducerRecord<String, String>(topic, Long.toString(msgCounter), message));

        if (msgCounter % 1000 == 0) System.out.println("Kafka messages sent: " + msgCounter);
        msgCounter++;

        PromExporter.metricslib_http_requests_total.labels("0", "KafkaProducer-" + clientId, topic).inc();

    }
}
