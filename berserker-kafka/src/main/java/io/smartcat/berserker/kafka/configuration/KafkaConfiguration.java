package io.smartcat.berserker.kafka.configuration;

import java.util.Map;

import io.smartcat.berserker.api.Worker;
import io.smartcat.berserker.configuration.ConfigurationParseException;
import io.smartcat.berserker.configuration.WorkerConfiguration;
import io.smartcat.berserker.kafka.worker.KafkaWorker;

/**
 * Configuration for Kafka worker.
 */
public class KafkaConfiguration implements WorkerConfiguration {

    @Override
    public String getName() {
        return "Kafka";
    }

    @Override
    public Worker<?> getWorker(Map<String, Object> configuration) throws ConfigurationParseException {
        return new KafkaWorker(configuration);
    }
}
