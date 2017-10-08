package io.smartcat.berserker.configuration;

import java.util.Map;

import io.smartcat.berserker.api.RateGenerator;
import io.smartcat.berserker.configuration.rategenerator.RateGeneratorConfigurationParser;

/**
 * Configuration to construct rate generator out of rate generator expressions.
 */
public class DefaultRateGeneratorConfiguration implements RateGeneratorConfiguration {

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public RateGenerator getRateGenerator(Map<String, Object> configuration) throws ConfigurationParseException {
        RateGeneratorConfigurationParser parser = new RateGeneratorConfigurationParser(configuration);
        return parser.build();
    }
}
