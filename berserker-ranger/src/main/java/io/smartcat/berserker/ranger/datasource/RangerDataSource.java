package io.smartcat.berserker.ranger.datasource;

import java.util.Map;

import io.smartcat.berserker.api.DataSource;
import io.smartcat.ranger.ObjectGenerator;

/**
 * Ranger data source implementation.
 */
public class RangerDataSource implements DataSource<Map<String, Object>> {

    private final ObjectGenerator<Map<String, Object>> objectGenerator;

    /**
     * Constructs ranger data source with specified <code>aggregatedObjectGenerator</code>.
     *
     * @param objectGenerator Generator which will be used to generate objects.
     */
    public RangerDataSource(ObjectGenerator<Map<String, Object>> objectGenerator) {
        this.objectGenerator = objectGenerator;
    }

    @Override
    public boolean hasNext(long time) {
        return true;
    }

    @Override
    public Map<String, Object> getNext(long time) {
        return (Map<String, Object>) objectGenerator.next();
    }
}
