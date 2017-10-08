package io.smartcat.berserker.rategenerator;

import io.smartcat.berserker.api.RateGenerator;

/**
 * Rate generator that multiplies rates of two {@link RateGenerator}s.
 */
public class MultiplicationRateGenerator implements RateGenerator {

    private final RateGenerator factor1;
    private final RateGenerator factor2;

    /**
     * Constructs multiplication rate generator with two factors.
     *
     * @param factor1 Rate generator which will be used as factor 1 for this multiplication.
     * @param factor2 Rate generator which will be used as factor 2 for this multiplication.
     */
    public MultiplicationRateGenerator(RateGenerator factor1, RateGenerator factor2) {
        if (factor1 == null) {
            throw new IllegalArgumentException("factor1 cannot be null.");
        }
        if (factor2 == null) {
            throw new IllegalArgumentException("factor2 cannot be null.");
        }
        this.factor1 = factor1;
        this.factor2 = factor2;
    }

    @Override
    public double getRate(long time) {
        return factor1.getRate(time) * factor2.getRate(time);
    }
}
