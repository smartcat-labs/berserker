package io.smartcat.berserker.ranger.configuration;

import java.util.Map;

import io.smartcat.berserker.api.DataSource;
import io.smartcat.berserker.configuration.ConfigurationParseException;
import io.smartcat.berserker.configuration.DataSourceConfiguration;
import io.smartcat.berserker.ranger.datasource.RangerDataSource;
import io.smartcat.ranger.ObjectGenerator;
import io.smartcat.ranger.parser.ConfigurationParser;

/**
 * Configuration to construct {@link RangerDataSource}.
 */
public class RangerConfiguration implements DataSourceConfiguration {

    @Override
    public String getName() {
        return "Ranger";
    }

    @Override
    public DataSource<?> getDataSource(Map<String, Object> configuration) throws ConfigurationParseException {
        ObjectGenerator<Map<String, Object>> objectGenerator = new ConfigurationParser(configuration).build();
        return new RangerDataSource(objectGenerator);
    }
}
