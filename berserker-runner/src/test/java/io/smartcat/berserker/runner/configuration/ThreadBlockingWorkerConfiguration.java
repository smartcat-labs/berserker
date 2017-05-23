package io.smartcat.berserker.runner.configuration;

import java.util.Map;

import io.smartcat.berserker.api.Worker;
import io.smartcat.berserker.configuration.ConfigurationParseException;
import io.smartcat.berserker.configuration.WorkerConfiguration;

public class ThreadBlockingWorkerConfiguration implements WorkerConfiguration {

    private static final int DEFAULT_BLOCK_TIME_MS = 1000;
    private static final String BLOCK_TIME = "blockTime";

    @Override
    public String getName() {
        return "ThreadBlockingWorker";
    }

    @Override
    public Worker<?> getWorker(Map<String, Object> configuration) throws ConfigurationParseException {
        int blockTimeMs;
        if (configuration.containsKey(BLOCK_TIME)) {
            blockTimeMs = (int) configuration.get(BLOCK_TIME);
        } else {
            blockTimeMs = DEFAULT_BLOCK_TIME_MS;
        }
        return (x) -> {
            try {
                Thread.sleep(blockTimeMs);
            } catch (InterruptedException e) {
            }
        };
    }
}
