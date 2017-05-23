package io.smartcat.berserker.configuration;

/**
 * GlobalConfiguration exception.
 */
public class ConfigurationException extends Exception {

    private static final long serialVersionUID = -8589993260328531894L;

    /**
     * Default constructor.
     */
    public ConfigurationException() {
    }

    /**
     * Constructor.
     *
     * @param message Error message.
     * @param cause Exception cause.
     * @param enableSuppression controls exception suppression.
     * @param writableStackTrace stack trace.
     */
    public ConfigurationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Constructor.
     *
     * @param message Error message.
     * @param cause Exception cause.
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     *
     * @param message Error message.
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param cause Exception cause.
     */
    public ConfigurationException(Throwable cause) {
        super(cause);
    }
}
