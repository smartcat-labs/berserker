package io.smartcat.berserker.rategenerator;

import io.smartcat.berserker.api.RateGenerator;

/**
 * Rate generator that divides rates of two {@link RateGenerator}s.
 */
public class DivisionRateGenerator implements RateGenerator {

    private final RateGenerator dividend;
    private final RateGenerator divisor;

    /**
     * Constructs division rate generator with dividend and divisor.
     *
     * @param dividend Rate generator which will be used as dividend for this division.
     * @param divisor Rate generator which will be used as divisor for this division.
     */
    public DivisionRateGenerator(RateGenerator dividend, RateGenerator divisor) {
        if (dividend == null) {
            throw new IllegalArgumentException("dividend cannot be null.");
        }
        if (divisor == null) {
            throw new IllegalArgumentException("divisor cannot be null.");
        }
        this.dividend = dividend;
        this.divisor = divisor;
    }

    @Override
    public double getRate(long time) {
        return dividend.getRate(time) / divisor.getRate(time);
    }
}
