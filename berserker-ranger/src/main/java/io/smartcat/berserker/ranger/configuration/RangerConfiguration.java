package io.smartcat.berserker.ranger.configuration;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.smartcat.berserker.api.DataSource;
import io.smartcat.berserker.configuration.ConfigurationParseException;
import io.smartcat.berserker.configuration.DataSourceConfiguration;
import io.smartcat.berserker.configuration.model.KafkaPayload;
import io.smartcat.berserker.ranger.datasource.RangerDataSource;
import io.smartcat.ranger.AggregatedObjectGenerator;
import io.smartcat.ranger.ObjectGenerator;

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
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectConfiguration objectConfiguration = objectMapper.convertValue(configuration.get("objectConfiguration"),
                ObjectConfiguration.class);
        AggregatedObjectGenerator<KafkaPayload> aggregatedObjectGenerator = createAggregatedObjectGenerator(
                objectConfiguration);
        return new RangerDataSource(aggregatedObjectGenerator);
    }

    private AggregatedObjectGenerator<KafkaPayload> createAggregatedObjectGenerator(
            ObjectConfiguration objectConfiguration) {
        ObjectGenerator.Builder<KafkaPayload> objectGeneratorBuilder = new ObjectGenerator.Builder<KafkaPayload>(
                KafkaPayload.class);
        objectGeneratorBuilder.toBeGenerated(objectConfiguration.getNumberOfObjects());
        for (Field field : objectConfiguration.getFields()) {
            String name = field.getName();
            String[] values = field.getValues().split(",");
            objectGeneratorBuilder.withValues(name, values);
        }
        ObjectGenerator<KafkaPayload> objectGenerator = objectGeneratorBuilder.build();
        AggregatedObjectGenerator<KafkaPayload> aggregatedObjectGenerator =
                new AggregatedObjectGenerator.Builder<KafkaPayload>().withObjectGenerator(objectGenerator).build();
        return aggregatedObjectGenerator;
    }
}
