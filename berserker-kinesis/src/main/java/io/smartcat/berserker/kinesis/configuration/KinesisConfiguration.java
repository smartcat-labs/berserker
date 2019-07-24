package io.smartcat.berserker.kinesis.configuration;

import java.util.Map;
import java.util.Properties;

import io.smartcat.berserker.api.Worker;
import io.smartcat.berserker.configuration.WorkerConfiguration;
import io.smartcat.berserker.kinesis.worker.KinesisWorker;

import static io.smartcat.berserker.configuration.ConfigurationHelper.getOptionalValue;

/**
 * Configuration for Kinesis worker.
 */
public class KinesisConfiguration implements WorkerConfiguration {

    private static final String ASYNC = "async";
    private static final String STREAM = "stream";
    private static final String PRODUCER_CONFIGURATION = "producer-configuration";

    private static final Properties props = new Properties();

    @Override
    public String getName() {
        return "Kinesis";
    }

    /**
     * Creates an instance of {@link KinesisWorker} for given set of configuration properties.
     * Configuration map should contain following:
     * <ul>
     * <li><code><b>async</b></code> - Indicates whether messages should be sent to Kinesis in an async or sync
     * fashion. Optional, defaults to <code>false</code>.</li>
     * <li><code><b>stream</b></code> - Name of the Kinesis stream to which messages will be sent.</li>
     * <li><code><b>producer-configuration</b></code> - Set of producer properties as defined within
     * <a href="https://docs.aws.amazon.com/streams/latest/dev/kinesis-kpl-config.html">
     *     configuration properties</a>.</li>
     * </ul>
     * @param configuration Configuration specific to this worker.
     * @return An instance of {@link KinesisWorker}.
     */
    @Override
    public Worker<?> getWorker(Map<String, Object> configuration) {
        boolean async = getOptionalValue(configuration, ASYNC, false);
        String stream = getOptionalValue(configuration, STREAM, null);
        Map<String, Object> producerConfiguration = (Map<String, Object>) configuration.get(PRODUCER_CONFIGURATION);
        props.putAll(producerConfiguration);
        return new KinesisWorker(props, async, stream);
    }
}
