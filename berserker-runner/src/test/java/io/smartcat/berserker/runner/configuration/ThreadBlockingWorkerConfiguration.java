package io.smartcat.berserker.runner.configuration;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import io.smartcat.berserker.api.Worker;
import io.smartcat.berserker.configuration.ConfigurationParseException;
import io.smartcat.berserker.configuration.WorkerConfiguration;

public class ThreadBlockingWorkerConfiguration implements WorkerConfiguration {

    @Override
    public String getName() {
        return "ThreadBlockingWorker";
    }

    @Override
    public Worker<?> getWorker(Map<String, Object> configuration) throws ConfigurationParseException {
        return (x) -> {
            CountDownLatch blockLatch = new CountDownLatch(1);
            try {
                blockLatch.await();
            } catch (InterruptedException e) {
            }
        };
    }
}
