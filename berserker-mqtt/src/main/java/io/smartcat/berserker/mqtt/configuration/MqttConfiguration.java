package io.smartcat.berserker.mqtt.configuration;

import static io.smartcat.berserker.configuration.ConfigurationHelper.getMandatoryValue;
import static io.smartcat.berserker.configuration.ConfigurationHelper.getOptionalValue;

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
        boolean async = getOptionalValue(configuration, ASYNC, false);
        String brokerUrl = getMandatoryValue(configuration, BROKER_URL);
        String clientId = getMandatoryValue(configuration, CLIENT_ID);
        int maxInflight = getOptionalValue(configuration, MAX_INFLIGHT, 10);
        boolean cleanSession = getOptionalValue(configuration, CLEAN_SESSION, true);
        int connectionTimeout = getOptionalValue(configuration, CONNECTION_TIMEOUT, 30);
        int mqttVersion = calculateMqttVersion((String) configuration.get(MQTT_VERSION));
        String username = (String) configuration.get(USERNAME);
        String password = (String) configuration.get(PASSWORD);

        return new MqttWorker(async, brokerUrl, clientId, maxInflight, cleanSession, connectionTimeout, mqttVersion,
                username, password);
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
