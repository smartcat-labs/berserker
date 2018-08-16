package io.smartcat.berserker.kafka.configuration;

import java.util.Map;

import io.smartcat.berserker.api.Worker;
import io.smartcat.berserker.configuration.WorkerConfiguration;
import io.smartcat.berserker.kafka.worker.KafkaWorker;

import static io.smartcat.berserker.configuration.ConfigurationHelper.getOptionalValue;

/**
 * Configuration for Kafka worker.
 */
public class KafkaConfiguration implements WorkerConfiguration {

    private static final String ASYNC = "async";
    private static final String TOPIC = "topic";
    private static final String PRODUCER_CONFIGURATION = "producer-configuration";

    @Override
    public String getName() {
        return "Kafka";
    }

    /**
     * Creates an instance of {@link KafkaWorker} for given set of configuration properties.
     * Configuration map should contain following:
     * <ul>
     * <li><code><b>async</b></code> - Indicates whether messages should be sent to broker in an async or sync
     * fashion. Optional, defaults to <code>false</code>.</li>
     * <li><code><b>topic</b></code> - Name of the topic to which messages will be sent. Optional, defaults to
     * <code>null</code>. Can be overridden
     * by message level <code>topic</code> property.</li>
     * <li><code><b>producer-configuration</b></code> - Set of producer properties as defined within
     * <a href="https://kafka.apache.org/documentation/#producerconfigs">configuration properties</a>.</li>
     * </ul>
     * @param configuration Configuration specific to this worker.
     * @return An instance of {@link KafkaWorker}.
     */
    @Override
    public Worker<?> getWorker(Map<String, Object> configuration) {
        boolean async = getOptionalValue(configuration, ASYNC, false);
        String topic = getOptionalValue(configuration, TOPIC, null);
        Map<String, Object> producerConfiguration = (Map<String, Object>) configuration.get(PRODUCER_CONFIGURATION);
        return new KafkaWorker(producerConfiguration, async, topic);
    }
}
