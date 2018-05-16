package io.smartcat.berserker.mqtt.worker;

import java.util.Map;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import io.smartcat.berserker.api.Worker;

/**
 * MQTT worker which publishes messages to MQTT broker.
 */
public class MqttWorker implements Worker<Map<String, Object>>, AutoCloseable {

    private static final String TOPIC = "topic";
    private static final String QOS = "qos";
    private static final String PAYLOAD = "payload";

    private final boolean async;
    private final MqttAsyncClient client;
    private final MqttConnectOptions connectOptions;
    private volatile boolean connected = false;

    /**
     * Constructs MQTT asynchronous worker with specified <code>client</code> and <code>connectOptions</code> to be
     * used.
     *
     * @param async Indicates whether worker should behave in asynchronous fashion or not. True if worker is to behave
     *            asynchronously, otherwise false.
     * @param brokerUrl Url to MQTT broker.
     * @param clientId ID of MQTT client.
     * @param maxInflight Maximum number of inflight messages.
     * @param cleanSession Indicates whether client and server should remember state across restarts and reconnects or
     *            not. True is state is not to be remembered, otherwise false.
     * @param connectionTimeout Connection timeout in seconds.
     * @param mqttVersion Version of MQTT specification to use.
     * @param username Username to connect to MQTT broker.
     * @param password Password to connect to MQTT broker.
     */
    public MqttWorker(boolean async, String brokerUrl, String clientId, int maxInflight, boolean cleanSession,
            int connectionTimeout, int mqttVersion, String username, String password) {
        try {
            this.async = async;
            this.client = new MqttAsyncClient(brokerUrl, clientId, new MemoryPersistence());
            this.connectOptions = new MqttConnectOptions();
            this.connectOptions.setMaxInflight(maxInflight);
            this.connectOptions.setCleanSession(cleanSession);
            this.connectOptions.setConnectionTimeout(connectionTimeout);
            this.connectOptions.setMqttVersion(mqttVersion);
            if (username != null) {
                this.connectOptions.setUserName(username);
            }
            if (password != null) {
                this.connectOptions.setPassword(password.toCharArray());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void accept(Map<String, Object> message, Runnable commitSuccess, Runnable commitFailure) {
        ensureConnected();
        String topic = (String) message.get(TOPIC);
        int qos = (Integer) message.get(QOS);
        String payload = (String) message.get(PAYLOAD);
        MqttMessage mqttMessage = new MqttMessage(payload.getBytes());
        mqttMessage.setQos(qos);
        try {
            IMqttDeliveryToken token = client.publish(topic, mqttMessage, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    commitSuccess.run();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    commitFailure.run();
                }
            });
            if (!async) {
                token.waitForCompletion();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        try {
            client.close();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureConnected() {
        if (!connected) {
            synchronized (this) {
                if (!connected) {
                    try {
                        client.connect(connectOptions).waitForCompletion();
                        connected = true;
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
