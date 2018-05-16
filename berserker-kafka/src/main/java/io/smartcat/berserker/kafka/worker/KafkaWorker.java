package io.smartcat.berserker.kafka.worker;

import java.util.Map;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import io.smartcat.berserker.api.Worker;

/**
 * Worker that publishes accepted message to Kafka cluster. Message must contain key and value. It uses
 * {@link KafkaProducer} internally to publish messages, producer can be configured using YAML config file containing
 * following <a href="https://kafka.apache.org/documentation/#producerconfigs">configuration properties</a>.
 * Additionally, file must contain <code>topic</code> property set to value of the topic to which messages will
 * be published and may contain <code>async</code> property to determine whether messages will be sent asynchronously or
 * synchronously.
 */
public class KafkaWorker implements Worker<Map<String, Object>>, AutoCloseable {

    private static final String KEY = "key";
    private static final String VALUE = "value";

    private final Map<String, Object> configuration;
    private final boolean async;
    private final String topic;

    private Producer<String, String> producer;

    /**
     * Constructs Kafka worker with specified <code>configuration</code> to be used by {@link KafkaProducer}.
     *
     * @param configuration Configuration to be used by {@link KafkaProducer}.
     * @param async Indicates whether messages should be sent asynchronously or synchronously.
     * @param topic Kafka topic to which to send messages.
     */
    public KafkaWorker(Map<String, Object> configuration, boolean async, String topic) {
        this.configuration = configuration;
        this.async = async;
        this.topic = topic;
        init();
    }

    @Override
    public void accept(Map<String, Object> message, Runnable commitSuccess, Runnable commitFailure) {
        String key = (String) message.get(KEY);
        String value = (String) message.get(VALUE);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
        Future<RecordMetadata> futureResponse = producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                commitSuccess.run();
            } else {
                commitFailure.run();
            }
        });
        if (!async) {
            try {
                futureResponse.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() throws Exception {
        producer.close();
    }

    private void init() {
        StringSerializer keySerializer = getSerializer(configuration, true);
        StringSerializer valueSerializer = getSerializer(configuration, false);
        producer = new KafkaProducer<>(configuration, keySerializer, valueSerializer);
    }

    private StringSerializer getSerializer(Map<String, Object> configuration, boolean isKey) {
        StringSerializer serializer = new StringSerializer();
        serializer.configure(configuration, isKey);
        return serializer;
    }
}
