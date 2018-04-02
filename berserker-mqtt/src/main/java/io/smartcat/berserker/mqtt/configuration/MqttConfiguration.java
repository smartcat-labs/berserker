package io.smartcat.berserker.mqtt.configuration;

import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smartcat.berserker.api.Worker;
import io.smartcat.berserker.configuration.ConfigurationParseException;
import io.smartcat.berserker.configuration.WorkerConfiguration;
import io.smartcat.berserker.mqtt.worker.MqttWorker;

/**
 * Configuration for MQTT worker.
 */
public class MqttConfiguration implements WorkerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttConfiguration.class);

    private static final String ASYNC = "async";
    private static final String BROKER_URL = "broker-url";
    private static final String CLIENT_ID = "client-id";

    private static final String MAX_INFLIGHT = "max-inflight";
    private static final String CLEAN_SESSION = "clean-session";
    private static final String CONNECTION_TIMEOUT = "connection-timeout";
    private static final String MQTT_VERSION = "mqtt-version";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private static final String MQTT_VERSION_3_1_1 = "3.1.1";
    private static final String MQTT_VERSION_3_1 = "3.1";

    @Override
    public String getName() {
        return "MQTT";
    }

    @Override
    public Worker<?> getWorker(Map<String, Object> configuration) throws ConfigurationParseException {
        Boolean async = (Boolean) configuration.get(ASYNC);
        String brokerUrl = (String) configuration.get(BROKER_URL);
        String clientId = (String) configuration.get(CLIENT_ID);
        Integer maxInflight = (Integer) configuration.get(MAX_INFLIGHT);
        Boolean cleanSession = (Boolean) configuration.get(CLEAN_SESSION);
        Integer connectionTimeout = (Integer) configuration.get(CONNECTION_TIMEOUT);
        String mqttVersion = (String) configuration.get(MQTT_VERSION);
        String username = (String) configuration.get(USERNAME);
        String password = (String) configuration.get(PASSWORD);

        boolean calculatedAsync = calculateAsync(async);
        validateField(brokerUrl, BROKER_URL);
        validateField(clientId, CLIENT_ID);
        int calculatedMaxInflight = calculateMaxInflight(maxInflight);
        boolean calculatedCleanSession = calculateCleanSession(cleanSession);
        int calculatedConnectionTimeout = calculateConnectionTimeout(connectionTimeout);
        int calculatedMqttVersion = calculateMqttVersion(mqttVersion);

        return new MqttWorker(calculatedAsync, brokerUrl, clientId, calculatedMaxInflight, calculatedCleanSession,
                calculatedConnectionTimeout, calculatedMqttVersion, username, password);
    }

    private boolean calculateAsync(Boolean async) {
        if (async == null) {
            LOGGER.info("'" + ASYNC + "' not set, using default value: false");
            return false;
        } else {
            LOGGER.info("'" + ASYNC + "' set to value: " + async);
            return async;
        }
    }

    private void validateField(String field, String fieldName) {
        if (field == null || field.isEmpty()) {
            throw new RuntimeException("'" + fieldName + "' is mandatory.");
        }
    }

    private int calculateMaxInflight(Integer maxInflight) {
        if (maxInflight == null) {
            LOGGER.info("'" + MAX_INFLIGHT + "' not set, using default value: 10");
            return 10;
        } else {
            LOGGER.info("'" + MAX_INFLIGHT + "' set to value: " + maxInflight);
            return maxInflight;
        }
    }

    private boolean calculateCleanSession(Boolean cleanSession) {
        if (cleanSession == null) {
            LOGGER.info("'" + CLEAN_SESSION + "' not set, using default value: true");
            return true;
        } else {
            LOGGER.info("'" + CLEAN_SESSION + "' set to value: " + CLEAN_SESSION);
            return cleanSession;
        }
    }

    private int calculateConnectionTimeout(Integer connectionTimeout) {
        if (connectionTimeout == null) {
            LOGGER.info("'" + CONNECTION_TIMEOUT + "' not set, using default value: 30");
            return 30;
        } else {
            LOGGER.info("'" + CONNECTION_TIMEOUT + "' set to value: " + connectionTimeout);
            return connectionTimeout;
        }
    }

    private int calculateMqttVersion(String mqttVersion) {
        if (mqttVersion == null) {
            LOGGER.info("'" + MQTT_VERSION + "' not set, using default version: 3.1.1 or 3.1 if that fails.");
            return 0;
        } else if (MQTT_VERSION_3_1_1.equals(mqttVersion)) {
            LOGGER.info("'" + MQTT_VERSION + "' set to value: " + MQTT_VERSION_3_1_1);
            return MqttConnectOptions.MQTT_VERSION_3_1_1;
        } else if (MQTT_VERSION_3_1.equals(mqttVersion)) {
            LOGGER.info("'" + MQTT_VERSION + "' set to value: " + MQTT_VERSION_3_1);
            return MqttConnectOptions.MQTT_VERSION_3_1;
        } else {
            throw new RuntimeException("'" + MQTT_VERSION + "' has invalid value: " + mqttVersion
                    + " Only supported versions are: " + MQTT_VERSION_3_1 + " and " + MQTT_VERSION_3_1_1);
        }
    }
}
