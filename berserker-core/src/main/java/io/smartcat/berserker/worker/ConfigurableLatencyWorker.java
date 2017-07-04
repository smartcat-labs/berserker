package io.smartcat.berserker.worker;

import java.util.List;

import io.smartcat.berserker.api.Worker;

/**
 * Purpose of this worker is testing. It can be configured to mimic any possible latency.
 */
public class ConfigurableLatencyWorker implements Worker<Object> {

    private final List<LatencyDescription> latencyDescriptions;
    private int current = -1;
    private long startMillis;

    /**
     * Constructs configurable latency worker with specified latency descriptions.
     * Descriptions are used in same order as specified. If all are depleted, worker starts from beginning of the list.
     *
     * @param latencyDescriptions List of latency descriptions to use.
     */
    public ConfigurableLatencyWorker(List<LatencyDescription> latencyDescriptions) {
        this.latencyDescriptions = latencyDescriptions;
    }

    @Override
    public void accept(Object t) {
        try {
            init();
            if (elapsed()) {
                increment();
            }
            Thread.sleep(latencyDescriptions.get(current).latencyMs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void init() {
        if (current < 0) {
            current = 0;
            startMillis = System.currentTimeMillis();
        }
    }

    private boolean elapsed() {
        return (System.currentTimeMillis() - startMillis) >= latencyDescriptions.get(current).intervalMs;
    }

    private void increment() {
        current++;
        if (current == latencyDescriptions.size()) {
            current = 0;
        }
    }

    /**
     * Description of a latency in service. For how long (intervalMs) should service have a latency of latencyMs.
     */
    public static class LatencyDescription {

        private final long intervalMs;
        private final int latencyMs;

        /**
         * Constructs latency description.
         *
         * @param intervalMs Interval in milliseconds for how long service should return specified latency.
         * @param latencyMs Latency in milliseconds.
         */
        public LatencyDescription(long intervalMs, int latencyMs) {
            this.intervalMs = intervalMs;
            this.latencyMs = latencyMs;
        }
    }
}
