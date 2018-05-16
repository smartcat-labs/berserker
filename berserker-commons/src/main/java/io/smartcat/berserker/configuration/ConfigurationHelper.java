package io.smartcat.berserker.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Helper class for configuration related operations.
 */
public class ConfigurationHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationHelper.class);

    private ConfigurationHelper() {
    }

    /**
     * Tries to get mandatory value, if value does not exist, exception is thrown.
     *
     * @param configuration Configuration from which to get value.
     * @param name Name of the value.
     * @param <T> Type of the value.
     * @return Value if found, otherwise exception is thrown.
     */
    public static <T> T getMandatoryValue(Map<String, Object> configuration, String name) {
        T value = (T) configuration.get(name);
        if (value == null || (value instanceof String && ((String) value).isEmpty())) {
            throw new RuntimeException("'" + name + "' is mandatory.");
        }
        LOGGER.info("'" + name + "' set to value: " + value);
        return value;
    }

    /**
     * Tries to get optional value, if value does not exist, default value is returned.
     *
     * @param configuration Configuration from which to get value.
     * @param name Name of the value.
     * @param defaultValue Default value.
     * @param <T> Type of the value.
     * @return Value if found, otherwise default value.
     */
    public static <T> T getOptionalValue(Map<String, Object> configuration, String name, T defaultValue) {
        T value = (T) configuration.get(name);
        if (value == null) {
            LOGGER.info("'" + name + "' not set, using default value: " + defaultValue);
            return defaultValue;
        }
        if (defaultValue != null && !defaultValue.getClass().isAssignableFrom(value.getClass())) {
            throw new RuntimeException("'" + name + "' set to wrong value. Value is of type: "
                    + value.getClass().getName() + ", but should be of type: " + defaultValue.getClass().getName());
        }
        LOGGER.info("'" + name + "' set to value: " + value);
        return value;
    }
}
