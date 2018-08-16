package io.smartcat.berserker.kafka.worker;

import java.util.Map;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import io.smartcat.berserker.api.Worker;

/**
 * Worker that publishes accepted message to Kafka cluster.
 */
public class KafkaWorker implements Worker<Map<String, Object>>, AutoCloseable {

    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String TOPIC = "topic";

    private final Map<String, Object> configuration;
    private final boolean async;
    private final String topic;

    private Producer<String, String> producer;

    /**
     * Constructs Kafka worker with specified properties.
     *
     * @param configuration Map containing configuration properties to be used by {@link KafkaProducer}. Map contains
     * common Kafka producer
     * <a href="https://kafka.apache.org/documentation/#producerconfigs">configuration properties</a>.
     * @param async Indicates whether messages should be sent asynchronously or synchronously.
     * @param topic Kafka topic to which to send messages. Optional.
     */
    public KafkaWorker(Map<String, Object> configuration, boolean async, String topic) {
        this.configuration = configuration;
        this.async = async;
        this.topic = topic;
        init();
    }

    /**
     * Accepts following arguments:
     * <ul>
     * <li><code><b>key</b></code> - Key of the message. Optional, if not specified, Kafka producer will calculate
     * partition based on message value.</li>
     * <li><code><b>value</b></code> - Value of the message. Mandatory.</li>
     * <li><code><b>topic</b></code> - Topic to which to send message. Mandatory if topic on configuration level is not
     * specified. If it is, this topic value will override it. If topic is not specified neither on configuration level
     * nor here, exception will be thrown.</li>
     * </ul>
     */
    @Override
    public void accept(Map<String, Object> message, Runnable commitSuccess, Runnable commitFailure) {
        String key = (String) message.get(KEY);
        String value = (String) message.get(VALUE);
        if (value == null) {
            throw new RuntimeException("'value' is mandatory.");
        }
        String messageLevelTopic = (String) message.get(TOPIC);
        String calculatedTopic = getCalculatedTopic(messageLevelTopic);
        ProducerRecord<String, String> record = new ProducerRecord<>(calculatedTopic, key, value);
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
    public void close() {
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

    private String getCalculatedTopic(String messageLevelTopic) {
        String calculatedTopic = messageLevelTopic != null ? messageLevelTopic : topic;
        if (calculatedTopic == null) {
            throw new RuntimeException("Topic must be present on either configuration or message level.");
        }
        return calculatedTopic;
    }
}
