package io.smartcat.berserker.rabbitmq.configuration;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import io.smartcat.berserker.api.Worker;
import io.smartcat.berserker.configuration.ConfigurationParseException;
import io.smartcat.berserker.configuration.WorkerConfiguration;
import io.smartcat.berserker.rabbitmq.worker.RabbitMqWorker;

/**
 * Configuration for RabbitMQ worker.
 */
public class RabbitMqConfiguration implements WorkerConfiguration {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String VIRTUAL_HOST = "virtual-host";

    @Override
    public String getName() {
        return "RabbitMQ";
    }

    @Override
    public Worker<?> getWorker(Map<String, Object> configuration) throws ConfigurationParseException {
        String username = (String) configuration.get(USERNAME);
        String password = (String) configuration.get(PASSWORD);
        String host = (String) configuration.get(HOST);
        int port = (int) configuration.get(PORT);
        String virtualHost = (String) configuration.get(VIRTUAL_HOST);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        factory.setHost(host);
        factory.setPort(port);
        Connection connection;
        try {
            connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            throw new ConfigurationParseException(e);
        }
        return new RabbitMqWorker(connection);
    }
}
