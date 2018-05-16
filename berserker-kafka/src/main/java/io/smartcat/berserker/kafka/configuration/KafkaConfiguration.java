package io.smartcat.berserker.kafka.configuration;

import java.util.Map;

import io.smartcat.berserker.api.Worker;
import io.smartcat.berserker.configuration.ConfigurationParseException;
import io.smartcat.berserker.configuration.WorkerConfiguration;
import io.smartcat.berserker.kafka.worker.KafkaWorker;

import static io.smartcat.berserker.configuration.ConfigurationHelper.getMandatoryValue;
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

    @Override
    public Worker<?> getWorker(Map<String, Object> configuration) throws ConfigurationParseException {
        boolean async = getOptionalValue(configuration, ASYNC, false);
        String topic = getMandatoryValue(configuration, TOPIC);
        Map<String, Object> producerConfiguration = (Map<String, Object>) configuration.get(PRODUCER_CONFIGURATION);
        return new KafkaWorker(producerConfiguration, async, topic);
    }
}
