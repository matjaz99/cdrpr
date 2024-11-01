package si.matjazcerkvenik.datasims.cdrpr.simulator.streams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class MyJsonSerializer<T> implements Serializer<T> {

    private final ObjectMapper mapper;

    public MyJsonSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }

        try {
            return mapper.writeValueAsBytes(data);
        } catch (JsonProcessingException | RuntimeException e) {
            throw new SerializationException("Error serializing to JSON with Jackson", e);
        }
    }

    @Override
    public void close() {
    }
}
