package io.smartcat.berserker.ranger.datasource;

import java.util.Map;

import io.smartcat.berserker.api.DataSource;
import io.smartcat.ranger.core.parser.DataGenerator;

/**
 * Ranger data source implementation.
 */
public class RangerDataSource implements DataSource<Map<String, Object>> {

    private final DataGenerator dataGenerator;

    /**
     * Constructs ranger data source with specified <code>aggregatedObjectGenerator</code>.
     *
     * @param dataGenerator Generator which will be used to generate objects.
     */
    public RangerDataSource(DataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    @Override
    public boolean hasNext(long time) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getNext(long time) {
        return (Map<String, Object>) dataGenerator.next();
    }
}
