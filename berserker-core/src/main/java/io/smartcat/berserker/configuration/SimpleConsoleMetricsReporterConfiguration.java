package io.smartcat.berserker.configuration;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;

/**
 * Creates simple {@link ConsoleReporter} without any customization.
 */
public class SimpleConsoleMetricsReporterConfiguration implements MetricsReporterConfiguration {

    @Override
    public String getName() {
        return "SimpleConsoleReporter";
    }

    @Override
    public void createAndStartReporter(MetricRegistry metricRegistry, Map<String, Object> configuration) {
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry).build();
        reporter.start(10, TimeUnit.SECONDS);
    }
}
