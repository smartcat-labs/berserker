package io.smartcat.berserker.datasource;

import java.util.Iterator;
import java.util.SplittableRandom;

import io.smartcat.berserker.api.DataSource;

/**
 * Endless data source generating random <code>Double</code> values.
 */
public class RandomDoubleDataSource implements DataSource<Double> {

    private final Iterator<Double> it = new SplittableRandom().doubles().iterator();

    @Override
    public boolean hasNext(long time) {
        return true;
    }

    @Override
    public Double getNext(long time) {
        return it.next();
    }
}
