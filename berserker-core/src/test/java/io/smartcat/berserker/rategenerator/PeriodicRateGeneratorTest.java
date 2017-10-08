package io.smartcat.berserker.rategenerator;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class PeriodicRateGeneratorTest {

    private static final double DELTA = 0.001d;

    @Test
    public void rate_should_return_1_when_period_is_100_and_time_is_201() {
        // GIVEN
        ConcretePeriodicRateGenerator periodicRateGenerator = new ConcretePeriodicRateGenerator(100);

        // WHEN
        double rate = periodicRateGenerator.getRate(TimeUnit.SECONDS.toNanos(201));

        // THEN
        Assert.assertEquals(1d, rate, DELTA);
    }

    @Test
    public void rate_should_return_0_when_period_is_50_and_time_is_150() {
        // GIVEN
        ConcretePeriodicRateGenerator periodicRateGenerator = new ConcretePeriodicRateGenerator(50);

        // WHEN
        double rate = periodicRateGenerator.getRate(TimeUnit.SECONDS.toNanos(150));

        // THEN
        Assert.assertEquals(0d, rate, DELTA);
    }

    @Test
    public void rate_should_return_60_when_period_is_10_and_time_is_16() {
        // GIVEN
        ConcretePeriodicRateGenerator periodicRateGenerator = new ConcretePeriodicRateGenerator(10);

        // WHEN
        double rate = periodicRateGenerator.getRate(TimeUnit.SECONDS.toNanos(16));

        // THEN
        Assert.assertEquals(60d, rate, DELTA);
    }

    @Test
    public void rate_should_return_99_when_period_is_100_and_time_is_399() {
        // GIVEN
        ConcretePeriodicRateGenerator periodicRateGenerator = new ConcretePeriodicRateGenerator(100);

        // WHEN
        double rate = periodicRateGenerator.getRate(TimeUnit.SECONDS.toNanos(399));

        // THEN
        Assert.assertEquals(99d, rate, DELTA);
    }

    private class ConcretePeriodicRateGenerator extends PeriodicRateGenerator {

        public ConcretePeriodicRateGenerator(long periodInSeconds) {
            super(periodInSeconds);
        }

        @Override
        protected double rateFunction(double value) {
            return value * 100;
        }
    }
}
