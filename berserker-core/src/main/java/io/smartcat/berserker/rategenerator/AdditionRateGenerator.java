package io.smartcat.berserker.rategenerator;

import io.smartcat.berserker.api.RateGenerator;

/**
 * Rate generator that adds up rates of two {@link RateGenerator}s.
 */
public class AdditionRateGenerator implements RateGenerator {

    private final RateGenerator summand1;
    private final RateGenerator summand2;

    /**
     * Constructs addition rate generator with two summands.
     *
     * @param summand1 Rate generator which will be used as summand 1 for this addition.
     * @param summand2 Rate generator which will be used as summand 2 for this addition.
     */
    public AdditionRateGenerator(RateGenerator summand1, RateGenerator summand2) {
        if (summand1 == null) {
            throw new IllegalArgumentException("summand1 cannot be null.");
        }
        if (summand2 == null) {
            throw new IllegalArgumentException("summand2 cannot be null.");
        }
        this.summand1 = summand1;
        this.summand2 = summand2;
    }

    @Override
    public double getRate(long time) {
        return summand1.getRate(time) + summand2.getRate(time);
    }
}
