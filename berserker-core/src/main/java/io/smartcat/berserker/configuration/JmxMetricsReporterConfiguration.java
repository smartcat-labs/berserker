package io.smartcat.berserker.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.jmx.JmxReporter.Builder;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

/**
 * Configuration to construct {@link JmxReporter}.
 * Example of supported configuration:
 * <pre>
 * {@code
 * metrics-reporter-configuration:
 *   domain: berserkerMetrics
 *   filter: io.smartcat.berserker.waitTime, io.smartcat.berserker.serviceTime}
 * </pre>
 */
public class JmxMetricsReporterConfiguration implements MetricsReporterConfiguration {

    private static final String DOMAIN = "domain";
    private static final String FILTER = "filter";

    @Override
    public String getName() {
        return "JMX";
    }

    @Override
    public void createAndStartReporter(MetricRegistry metricRegistry, Map<String, Object> configuration) {
        Builder builder = JmxReporter.forRegistry(metricRegistry).convertRatesTo(TimeUnit.DAYS);
        if (configuration.containsKey(DOMAIN)) {
            String domain = (String) configuration.get(DOMAIN);
            if (domain != null && !domain.isEmpty()) {
                builder.inDomain(domain);
            }
        }
        if (configuration.containsKey(FILTER)) {
            String filter = (String) configuration.get(FILTER);
            if (filter != null && !filter.isEmpty()) {
                List<String> metrics = Arrays.asList(filter.split(",")).stream().map(x -> x.trim())
                        .collect(Collectors.toList());
                MetricFilter metricFilter = new MetricFilter() {
                    @Override
                    public boolean matches(String name, Metric metric) {
                        return metrics.contains(name);
                    }
                };
                builder.filter(metricFilter);
            }
        }
        JmxReporter reporter = builder.build();
        reporter.start();
    }
}
