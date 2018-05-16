package io.smartcat.berserker.cassandra.configuration;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.smartcat.berserker.api.Worker;
import io.smartcat.berserker.cassandra.worker.CassandraWorker;
import io.smartcat.berserker.configuration.ConfigurationParseException;
import io.smartcat.berserker.configuration.WorkerConfiguration;

import static io.smartcat.berserker.configuration.ConfigurationHelper.getMandatoryValue;
import static io.smartcat.berserker.configuration.ConfigurationHelper.getOptionalValue;
import static java.util.Collections.emptyList;

/**
 * Configuration for Cassandra worker. Configuration map should contain following:
 * <ul>
 * <li><b>connection-points</b> - Comma separated values of hostname and port. (10.10.0.1:9042, host1:9042, host2:9043)
 * It cannot be null nor empty.</li>
 * <li><b>keyspace</b> - Name of keyspace to use. Cannot be null nor empty.</li>
 * <li><b>async</b> - Indicates whether statements will be executed synchronously or asynchronously. Can be either true
 * or false.</li>
 * <li><b>bootstrap-commands</b> - List of CQL commands to execute only once after connection to Cassandra cluster is
 * established. Suitable for creating keyspaces, tables and populating some initial data if needed. It is optional.</li>
 * <li><b>prepared-statements</b> - List of prepared statements to create. It is optional and each statement is a map
 * containing:
 * <ul>
 * <li>id - Id of statement which can be later referenced.</li>
 * <li>query - Statement CQL query.</li>
 * </ul>
 * </li>
 * </ul>
 */
public class CassandraConfiguration implements WorkerConfiguration {

    private static final String CONNECTION_POINTS = "connection-points";
    private static final String USE_SSL = "use-ssl";
    private static final String KEYSPACE = "keyspace";
    private static final String ASYNC = "async";
    private static final String BOOTSTRAP_COMMANDS = "bootstrap-commands";
    private static final String PREPARED_STATEMENTS = "prepared-statements";

    @Override
    public String getName() {
        return "Cassandra";
    }

    @Override
    public Worker<?> getWorker(Map<String, Object> configuration) throws ConfigurationParseException {
        List<InetSocketAddress> connectionPointsWithPorts = getConnectionPointsWithPorts(configuration);
        boolean useSSL = getOptionalValue(configuration, USE_SSL, false);
        String keyspace = getMandatoryValue(configuration, KEYSPACE);
        boolean async = getOptionalValue(configuration, ASYNC, false);
        List<String> bootstrapDDLCommands = getOptionalValue(configuration, BOOTSTRAP_COMMANDS, new ArrayList<>(0));
        List<PreparedStatement> prepStatements = getPreparedStatements(configuration);
        return new CassandraWorker(connectionPointsWithPorts, useSSL, keyspace, async, bootstrapDDLCommands,
            prepStatements);
    }

    private List<InetSocketAddress> getConnectionPointsWithPorts(Map<String, Object> configuration) {
        String connectionPoints = (String) configuration.get(CONNECTION_POINTS);
        if (connectionPoints == null || configuration.isEmpty()) {
            throw new RuntimeException(CONNECTION_POINTS + " cannot be null nor empty.");
        }
        List<InetSocketAddress> result = new ArrayList<>();
        for (String connectionPoint : connectionPoints.split(",")) {
            String[] hostnameAndPort = connectionPoint.trim().split(":");
            InetSocketAddress address = new InetSocketAddress(hostnameAndPort[0], Integer.parseInt(hostnameAndPort[1]));
            result.add(address);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<PreparedStatement> getPreparedStatements(Map<String, Object> configuration) {
        List<Object> preparedStatements = (List<Object>) configuration.get(PREPARED_STATEMENTS);
        if (preparedStatements == null) {
            return emptyList();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(preparedStatements, new TypeReference<List<PreparedStatement>>() {
        });
    }
}
