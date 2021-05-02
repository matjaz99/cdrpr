package si.iskratel.simulator.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import si.iskratel.cdr.parser.CdrBean;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class NodeFilter {

    public static void main(final String[] args) throws IOException {

        Properties props = new Properties();
        props.putIfAbsent(StreamsConfig.APPLICATION_ID_CONFIG, "alarms-stream-filter");
//        props.putIfAbsent(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "pgcentos:9092");
        props.putIfAbsent(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "centosvm:9092");
        props.putIfAbsent(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
//        props.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        ObjectMapper mapper = new ObjectMapper();
        Serde<CdrBean> serde = Serdes.serdeFrom(
                new MyJsonSerializer<>(mapper),
                new MyJsonDeserializer<>(mapper, CdrBean.class));

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, CdrBean> stream = builder.stream("pmon_all_calls_topic", Consumed.with(Serdes.String(), serde));

        // filter out only critical alarms
        stream.filter((severity, alarm) -> alarm.getNodeId().equalsIgnoreCase("critical"))
                .to("pmon_all_calls", Produced.with(Serdes.String(), serde));

        System.out.println(builder.build().describe()); // describe topology
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("alarms-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (final Throwable e) {
            System.exit(1);
        }
        System.exit(0);

    }

}
