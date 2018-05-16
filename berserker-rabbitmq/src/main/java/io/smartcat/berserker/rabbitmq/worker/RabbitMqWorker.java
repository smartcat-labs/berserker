package io.smartcat.berserker.rabbitmq.worker;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import io.smartcat.berserker.api.Worker;

/**
 * Worker that publishes accepted message to RabbitMQ. Message must contain <code>exchangeName</code>,
 * <code>routingKey</code> and <code>messageContent</code> fields.
 */
public class RabbitMqWorker implements Worker<Map<String, Object>>, AutoCloseable {

    private static final String EXCHANGE_NAME = "exchangeName";
    private static final String ROUTING_KEY = "routingKey";
    private static final String MESSAGE_CONTENT = "messageContent";

    private static final String CONTENT_TYPE = "contentType";
    private static final String CONTENT_ENCODING = "contentEncoding";
    private static final String HEADERS = "headers";
    private static final String DELIVERY_MODE = "deliveryMode";
    private static final String PRIORITY = "priority";
    private static final String CORRELATION_ID = "correlationId";
    private static final String REPLY_TO = "replyTo";
    private static final String EXPIRATION = "expiration";
    private static final String MESSAGE_ID = "messageId";
    private static final String TIMESTAMP = "timestamp";
    private static final String TYPE = "type";
    private static final String USER_ID = "userId";
    private static final String APP_ID = "appId";
    private static final String CLUSTER_ID = "clusterId";

    private final Connection connection;
    private final Channel channel;

    /**
     * Constructs RabbitMQ worker with specified <code>connection</code> to be used.
     *
     * @param connection Connection object to RabbitMQ.
     */
    public RabbitMqWorker(Connection connection) {
        this.connection = connection;
        try {
            this.channel = connection.createChannel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void accept(Map<String, Object> message, Runnable commitSuccess, Runnable commitFailure) {
        String exchangeName = (String) message.get(EXCHANGE_NAME);
        String routingKey = (String) message.get(ROUTING_KEY);
        BasicProperties props = createProperties(message);
        String messageContent = (String) message.get(MESSAGE_CONTENT);
        try {
            channel.basicPublish(exchangeName, routingKey, props, messageContent.getBytes());
            commitSuccess.run();
        } catch (IOException e) {
            commitFailure.run();
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private BasicProperties createProperties(Map<String, Object> message) {
        BasicProperties.Builder propsBuilder = new BasicProperties.Builder();
        String contentType = (String) message.get(CONTENT_TYPE);
        if (!isEmpty(contentType)) {
            propsBuilder.contentType(contentType);
        }
        String contentEncoding = (String) message.get(CONTENT_ENCODING);
        if (!isEmpty(contentEncoding)) {
            propsBuilder.contentEncoding(contentEncoding);
        }
        Map<String, Object> headers = (Map) message.get(HEADERS);
        if (headers != null && !headers.isEmpty()) {
            propsBuilder.headers(headers);
        }
        Integer deliveryMode = (Integer) message.get(DELIVERY_MODE);
        if (deliveryMode != null) {
            propsBuilder.deliveryMode(deliveryMode);
        }
        Integer priority = (Integer) message.get(PRIORITY);
        if (priority != null) {
            propsBuilder.priority(priority);
        }
        String correlationId = (String) message.get(CORRELATION_ID);
        if (!isEmpty(correlationId)) {
            propsBuilder.correlationId(correlationId);
        }
        String replyTo = (String) message.get(REPLY_TO);
        if (!isEmpty(replyTo)) {
            propsBuilder.replyTo(replyTo);
        }
        String expiration = (String) message.get(EXPIRATION);
        if (!isEmpty(expiration)) {
            propsBuilder.expiration(expiration);
        }
        String messageId = (String) message.get(MESSAGE_ID);
        if (!isEmpty(messageId)) {
            propsBuilder.messageId(messageId);
        }
        Date timestamp = (Date) message.get(TIMESTAMP);
        if (timestamp != null) {
            propsBuilder.timestamp(timestamp);
        }
        String type = (String) message.get(TYPE);
        if (!isEmpty(type)) {
            propsBuilder.type(type);
        }
        String userId = (String) message.get(USER_ID);
        if (!isEmpty(userId)) {
            propsBuilder.userId(userId);
        }
        String appId = (String) message.get(APP_ID);
        if (!isEmpty(appId)) {
            propsBuilder.appId(appId);
        }
        String clusterId = (String) message.get(CLUSTER_ID);
        if (!isEmpty(clusterId)) {
            propsBuilder.clusterId(clusterId);
        }
        return propsBuilder.build();
    }

    @Override
    public void close() throws Exception {
        channel.close();
        connection.close();
    }

    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
