package si.iskratel.simulator.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

public class MyJsonDeserializer<T> implements Deserializer<T> {

    private final ObjectMapper mapper;
    private final Class<T> clazz;

    public MyJsonDeserializer(ObjectMapper mapper, Class<T> clazz) {
        this.mapper = mapper;
        this.clazz = clazz;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return mapper.readValue(data, clazz);
        } catch (IOException | RuntimeException e) {
            throw new SerializationException("Error deserializing from JSON with Jackson", e);
        }
    }

    @Override
    public void close() {
    }

}
