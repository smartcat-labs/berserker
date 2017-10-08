package io.smartcat.berserker.rategenerator;

import io.smartcat.berserker.api.RateGenerator;

/**
 * Rate generator that subtracts rates of two {@link RateGenerator}s.
 */
public class SubtractionRateGenerator implements RateGenerator {

    private final RateGenerator minuend;
    private final RateGenerator subtrahend;

    /**
     * Constructs subtraction rate generator with minuend and subtrahend.
     *
     * @param minuend Rate generator which will be used as minuend for this subtraction.
     * @param subtrahend Rate generator which will be used as subtrahend for this subtraction.
     */
    public SubtractionRateGenerator(RateGenerator minuend, RateGenerator subtrahend) {
        if (minuend == null) {
            throw new IllegalArgumentException("minuend cannot be null.");
        }
        if (subtrahend == null) {
            throw new IllegalArgumentException("subtrahend cannot be null.");
        }
        this.minuend = minuend;
        this.subtrahend = subtrahend;
    }

    @Override
    public double getRate(long time) {
        return minuend.getRate(time) - subtrahend.getRate(time);
    }
}
