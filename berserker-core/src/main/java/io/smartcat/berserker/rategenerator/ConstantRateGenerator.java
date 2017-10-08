package io.smartcat.berserker.rategenerator;

import io.smartcat.berserker.api.RateGenerator;

/**
 * Rate generator which generates constant rate.
 */
public class ConstantRateGenerator implements RateGenerator {

    private final double perSecondRate;

    /**
     * Constructs rate generator with specified <code>perSecondRate</code>.
     *
     * @param perSecondRate Rate of the rate generator per second, must be positive number.
     */
    public ConstantRateGenerator(double perSecondRate) {
        if (perSecondRate <= 0) {
            throw new IllegalArgumentException("Rate must be positive number.");
        }
        this.perSecondRate = perSecondRate;
    }

    @Override
    public double getRate(long time) {
        return perSecondRate;
    }
}
