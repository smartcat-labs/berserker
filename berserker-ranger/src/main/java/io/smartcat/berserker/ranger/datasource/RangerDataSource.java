package io.smartcat.berserker.ranger.datasource;

import java.util.Iterator;

import io.smartcat.berserker.api.DataSource;
import io.smartcat.berserker.configuration.model.KafkaPayload;
import io.smartcat.ranger.AggregatedObjectGenerator;

/**
 * Ranger data source implementation.
 */
public class RangerDataSource implements DataSource<KafkaPayload> {

    private final Iterator<KafkaPayload> iterator;

    /**
     * Constructs ranger data source with specified <code>aggregatedObjectGenerator</code>.
     *
     * @param aggregatedObjectGenerator Generator which will be used to generate objects.
     */
    public RangerDataSource(AggregatedObjectGenerator<KafkaPayload> aggregatedObjectGenerator) {
        this.iterator = aggregatedObjectGenerator.iterator();
    }

    @Override
    public boolean hasNext(long time) {
        return iterator.hasNext();
    }

    @Override
    public KafkaPayload getNext(long time) {
        return iterator.next();
    }
}
