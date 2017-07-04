package io.smartcat.berserker.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.smartcat.berserker.api.Worker;
import io.smartcat.berserker.worker.ConfigurableLatencyWorker;
import io.smartcat.berserker.worker.ConfigurableLatencyWorker.LatencyDescription;

/**
 * Configuration to construct {@link ConfigurableLatencyWorker}.
 */
public class ConfigurableLatencyWorkerConfiguration implements WorkerConfiguration {

    private static final String LATENCY_DESCRIPTIONS = "latency-descriptions";
    private static final String INTERVAL = "interval";
    private static final String LATENCY = "latency";

    @Override
    public String getName() {
        return "ConfigurableLatency";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Worker<?> getWorker(Map<String, Object> configuration) throws ConfigurationParseException {
        if (configuration == null || !configuration.containsKey(LATENCY_DESCRIPTIONS)) {
            throw new RuntimeException("Configuration must contain '" + LATENCY_DESCRIPTIONS + "'.");
        }
        List<Map<String, Object>> latencyDescs = (List<Map<String, Object>>) configuration.get(LATENCY_DESCRIPTIONS);
        List<LatencyDescription> latencyDescriptions = createLatencyDescriptions(latencyDescs);
        return new ConfigurableLatencyWorker(latencyDescriptions);
    }

    private List<LatencyDescription> createLatencyDescriptions(List<Map<String, Object>> latencyDescs) {
        List<LatencyDescription> result = new ArrayList<>();
        for (Map<String, Object> latencyDesc : latencyDescs) {
            result.add(createLatencyDescription(latencyDesc));
        }
        return result;
    }

    private LatencyDescription createLatencyDescription(Map<String, Object> latencyDesc) {
        if (latencyDesc == null || !latencyDesc.containsKey(INTERVAL) || !latencyDesc.containsKey(LATENCY)) {
            throw new RuntimeException("Latency description must contain '" + INTERVAL + "' and '" + LATENCY + "'.");
        }
        long interval = ((Number) latencyDesc.get(INTERVAL)).longValue();
        int latency = ((Number) latencyDesc.get(LATENCY)).intValue();
        return new LatencyDescription(interval, latency);
    }
}
