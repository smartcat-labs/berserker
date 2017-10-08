package io.smartcat.berserker.rategenerator;

import io.smartcat.berserker.api.RateGenerator;

/**
 * Proxy around rate generator.
 */
public class RateGeneratorProxy implements RateGenerator {

    private RateGenerator delegate;

    /**
     * Constructs proxy without delegate.
     */
    public RateGeneratorProxy() {
    }

    /**
     * Constructs proxy with specified <code>delegate</code>.
     *
     * @param delegate Value which will be evaluated and cached.
     */
    public RateGeneratorProxy(RateGenerator delegate) {
        setDelegate(delegate);
    }

    /**
     * Sets value to this proxy.
     *
     * @param delegate Value which will be evaluated and cached.
     */
    public void setDelegate(RateGenerator delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate cannot be null.");
        }
        this.delegate = delegate;
    }

    @Override
    public double getRate(long time) {
        checkDelegate();
        return delegate.getRate(time);
    }

    private void checkDelegate() {
        if (delegate == null) {
            throw new DelegateNotSetException();
        }
    }

    /**
     * Signals that delegate is not set.
     */
    public static class DelegateNotSetException extends RuntimeException {

        private static final long serialVersionUID = 6257779717961934851L;

        /**
         * Constructs {@link DelegateNotSetException} with default message.
         */
        public DelegateNotSetException() {
            super("Delegate not set for ValueProxy.");
        }
    }
}
