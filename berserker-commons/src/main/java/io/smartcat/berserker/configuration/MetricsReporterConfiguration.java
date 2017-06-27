package io.smartcat.berserker.configuration;

import java.util.Map;

import com.codahale.metrics.MetricRegistry;

/**
 * Creates and starts {@link com.codahale.metrics.Reporter Reporter}. Each {@link com.codahale.metrics.Reporter
 * Reporter} should go with corresponding metrics reporter configuration implementation which would be used to
 * construct that metrics reporter.
 */
public interface MetricsReporterConfiguration extends BaseConfiguration {

    /**
     * Creates and starts reporter for specified <code>metricRegistry</code> based on provided configuration.
     *
     * @param metricRegistry Metric registry for which to create and start reporter.
     * @param configuration Configuration specific to metrics reporter it should construct.
     */
    void createAndStartReporter(MetricRegistry metricRegistry, Map<String, Object> configuration);
}
