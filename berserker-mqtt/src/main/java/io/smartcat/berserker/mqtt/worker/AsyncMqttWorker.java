package io.smartcat.berserker.mqtt.worker;

import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import io.smartcat.berserker.api.Worker;

/**
 * MQTT worker which publishes messages in an asynchronous fashion.
 */
public class AsyncMqttWorker implements Worker<Map<String, Object>>, AutoCloseable {

    private static final String TOPIC = "topic";
    private static final String QOS = "qos";
    private static final String PAYLOAD = "payload";

    private final MqttAsyncClient client;
    private final MqttConnectOptions connectOptions;
    private volatile boolean connected = false;

    /**
     * Constructs MQTT asynchronous worker with specified <code>client</code> and <code>connectOptions</code> to be
     * used.
     *
     * @param client Asynchronous MQTT client which will publish messages.
     * @param connectOptions Options to use to connect to MQTT broker.
     */
    public AsyncMqttWorker(MqttAsyncClient client, MqttConnectOptions connectOptions) {
        this.client = client;
        this.connectOptions = connectOptions;
    }

    @Override
    public void accept(Map<String, Object> message) {
        ensureConnected();
        String topic = (String) message.get(TOPIC);
        int qos = (Integer) message.get(QOS);
        String payload = (String) message.get(PAYLOAD);
        MqttMessage mqttMessage = new MqttMessage(payload.getBytes());
        mqttMessage.setQos(qos);
        try {
            client.publish(topic, mqttMessage);
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
                        IMqttToken connectToken = client.connect(connectOptions);
                        connectToken.waitForCompletion();
                        connected = true;
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
