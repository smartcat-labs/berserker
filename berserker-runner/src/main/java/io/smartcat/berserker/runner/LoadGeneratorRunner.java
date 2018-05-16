package io.smartcat.berserker.runner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.smartcat.berserker.worker.InternalWorker;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.reflections.Reflections;

import com.codahale.metrics.MetricRegistry;

import io.smartcat.berserker.LoadGenerator;
import io.smartcat.berserker.api.DataSource;
import io.smartcat.berserker.api.RateGenerator;
import io.smartcat.berserker.api.Worker;
import io.smartcat.berserker.configuration.BaseConfiguration;
import io.smartcat.berserker.configuration.ConfigurationParseException;
import io.smartcat.berserker.configuration.DataSourceConfiguration;
import io.smartcat.berserker.configuration.GlobalConfiguration;
import io.smartcat.berserker.configuration.LoadGeneratorConfiguration;
import io.smartcat.berserker.configuration.MetricsReporterConfiguration;
import io.smartcat.berserker.configuration.RateGeneratorConfiguration;
import io.smartcat.berserker.configuration.WorkerConfiguration;
import io.smartcat.berserker.configuration.YamlConfigurationLoader;

/**
 * Runner which takes configuration file, constructs {@link LoadGenerator} with depending {@link DataSource},
 * {@link RateGenerator} and {@link Worker} and runs load generator.
 */
public class LoadGeneratorRunner {

    private static final String BERSERKER_BASE_PACKAGE = "io.smartcat.berserker";
    private static final String CONFIG_SHORT = "c";
    private static final String CONFIG_LONG = "config";

    private LoadGeneratorRunner() {
    }

    /**
     * Main method for starting load generator runner.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        Options options = getOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar berserker-runner.jar -c <path_to_config_file>", options);
        }
        if (cmd == null) {
            return;
        }
        try {
            generateLoad(cmd.getOptionValue(CONFIG_LONG));
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void generateLoad(String configFilePath) throws Exception {
        YamlConfigurationLoader configurationLoader = new YamlConfigurationLoader();
        GlobalConfiguration configuration = configurationLoader.loadConfig(getURL(configFilePath));
        LoadGeneratorConfiguration loadGeneratorConfiguration = configuration.loadGeneratorConfiguration;
        loadGeneratorConfiguration.validate();
        DataSource dataSource = getDataSource(loadGeneratorConfiguration.dataSourceConfigurationName,
                configuration.dataSourceConfiguration);
        RateGenerator rateGenerator = getRateGenerator(loadGeneratorConfiguration.rateGeneratorConfigurationName,
                configuration.rateGeneratorConfiguration);
        Worker workerDelegate = getWorker(loadGeneratorConfiguration.workerConfigurationName,
                configuration.workerConfiguration);
        InternalWorker worker = wrapIntoInternalWorker(workerDelegate, loadGeneratorConfiguration.queueCapacity,
                loadGeneratorConfiguration.threadCount, loadGeneratorConfiguration.metricsPrefix);
        createAndStartReporter(worker.getMetricRegistry(), loadGeneratorConfiguration.metricsReporterConfigurationName,
                configuration.metricsReporterConfiguration);
        LoadGenerator loadGenerator = new LoadGenerator(dataSource, rateGenerator, worker);
        loadGenerator.run();
    }

    private static Options getOptions() {
        Option configOption = new Option(CONFIG_SHORT, CONFIG_LONG, true,
                "Path to config YAML file containing runner configuration.");
        configOption.setRequired(true);

        Options options = new Options();
        options.addOption(configOption);
        return options;
    }

    private static URL getURL(String path) throws IOException {
        return new File(path).getCanonicalFile().toURI().toURL();
    }

    private static DataSource<?> getDataSource(String name, Map<String, Object> configuration)
            throws InstantiationException, IllegalAccessException, ConfigurationParseException {
        DataSourceConfiguration dataSourceConfiguration = getConfigurationWithName(name, DataSourceConfiguration.class);
        return dataSourceConfiguration.getDataSource(configuration);
    }

    private static RateGenerator getRateGenerator(String name, Map<String, Object> configuration)
            throws ConfigurationParseException {
        RateGeneratorConfiguration rateGeneratorConfiguration = getConfigurationWithName(name,
                RateGeneratorConfiguration.class);
        return rateGeneratorConfiguration.getRateGenerator(configuration);
    }

    private static Worker<?> getWorker(String name, Map<String, Object> configuration)
            throws ConfigurationParseException {
        WorkerConfiguration workerConfiguration = getConfigurationWithName(name, WorkerConfiguration.class);
        return workerConfiguration.getWorker(configuration);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static InternalWorker wrapIntoInternalWorker(Worker workerDelegate, int queueCapacity, int threadCount,
                                                         String metricsPrefix) {
        return new InternalWorker(workerDelegate, queueCapacity, true, metricsPrefix, threadCount);
    }

    private static void createAndStartReporter(MetricRegistry metricRegistry, String name,
            Map<String, Object> configuration) {
        MetricsReporterConfiguration metricsReporterConfiguration = getConfigurationWithName(name,
                MetricsReporterConfiguration.class);
        metricsReporterConfiguration.createAndStartReporter(metricRegistry, configuration);
    }

    private static <T extends BaseConfiguration> T getConfigurationWithName(String name, Class<T> clazz) {
        try {
            List<T> configurations = new ArrayList<>();
            List<String> classNames = new ArrayList<>();
            for (Class<? extends T> configurationClass : getSubTypesOf(clazz)) {
                T configuration = configurationClass.newInstance();
                if (name.equals(configuration.getName())) {
                    configurations.add(configuration);
                    classNames.add(configurationClass.getCanonicalName());
                }
            }
            if (configurations.isEmpty()) {
                throw new RuntimeException("Configuration with name: " + name + " not found.");
            }
            if (configurations.size() > 1) {
                throw new RuntimeException("Found " + configurations.size() + " configurations on classpath for name: "
                        + name + ", but expected 1. Configuration classes found: " + classNames.toString());
            }
            return configurations.get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> Set<Class<? extends T>> getSubTypesOf(Class<T> clazz) {
        Reflections reflections = new Reflections(BERSERKER_BASE_PACKAGE);
        return reflections.getSubTypesOf(clazz);
    }
}
