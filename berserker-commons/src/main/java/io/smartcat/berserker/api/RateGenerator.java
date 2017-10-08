package io.smartcat.berserker.api;

/**
 * Interface providing API for rate generation. Generates rate per second.
 */
public interface RateGenerator {

    /**
     * Returns rate per second as a function of time.
     *
     * @param time Relative time in nanoseconds from starting load generator.
     * @return Rate per second as a function of time.
     */
    double getRate(long time);
}
