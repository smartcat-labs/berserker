package io.smartcat.berserker.cassandra.worker;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

import io.smartcat.berserker.api.Worker;
import io.smartcat.berserker.cassandra.configuration.PreparedStatement;

/**
 * Worker that executes CQL statements on provided Cassandra connection points. It uses DataStax's java driver
 * internally.
 */
public class CassandraWorker implements Worker<Map<String, Object>> {

    private static final String QUERY = "query";
    private static final String VALUES = "values";
    private static final String PREPARED_STATEMENT_ID = "preparedStatementId";
    private static final String CONSISTENCY_LEVEL = "consistencyLevel";

    private final Cluster cluster;
    private final Session session;
    private final boolean async;
    private final Map<String, com.datastax.driver.core.PreparedStatement> preparedStatements;

    /**
     * Constructs Cassandra worker with specified properties.
     *
     * @param connectionPointsWithPorts List of connection points to use.
     * @param keyspace Name of keyspace in database to use.
     * @param async Indicates whether statements will be executed synchronously or asynchronously.
     * @param bootstrapDDLCommands List of CQL commands to execute only once after connection to Cassandra cluster is
     *            established. Suitable for creating keyspaces, tables and populating some initial data if needed.
     * @param prepStatements List of prepared statements to create. Each statement is defined with id and can be
     *            referenced from {@link #accept(Map)} method.
     */
    public CassandraWorker(List<InetSocketAddress> connectionPointsWithPorts, String keyspace, boolean async,
            List<String> bootstrapDDLCommands, List<PreparedStatement> prepStatements) {
        if (connectionPointsWithPorts == null || connectionPointsWithPorts.isEmpty()) {
            throw new IllegalArgumentException("List of connection points with ports cannot be null nor empty");
        }
        if (keyspace == null || keyspace.isEmpty()) {
            throw new IllegalArgumentException("Keyspace cannot be null nor empty.");
        }
        cluster = Cluster.builder().addContactPointsWithPorts(connectionPointsWithPorts).build();
        session = cluster.connect();
        if (bootstrapDDLCommands != null) {
            for (String command : bootstrapDDLCommands) {
                session.execute(command);
            }
        }
        session.execute("USE " + keyspace + ";");
        preparedStatements = new HashMap<>();
        if (prepStatements != null && !prepStatements.isEmpty()) {
            for (PreparedStatement prepStatement : prepStatements) {
                RegularStatement toPrepare = new SimpleStatement(prepStatement.getQuery());
                preparedStatements.put(prepStatement.getId(), session.prepare(toPrepare));
            }
        }
        this.async = async;
    }

    /**
     * Accepts two possible combinations:
     * <ul>
     * <li><b>Query</b>, which contains:
     * <ul>
     * <li>consistencyLevel - Consistency level of statement which will be executed. If not specified, ONE will be
     * used.</li>
     * <li>query - String representation of CQL statement which will be executed.</li>
     * </ul>
     * </li>
     * <li><b>Prepared statement</b>, which contains:
     * <ul>
     * <li>consistencyLevel - Consistency level of statement which will be executed. If not specified, ONE will be
     * used.</li>
     * <li>preparedStatementId - Id of prepared statement to execute (defined within constructor).</li>
     * <li>values - List of values to bind to prepared statement's variables.</li>
     * </ul>
     * </li>
     * </ul>
     * Depending on the map content, appropriate option will be executed (either query or prepared statement).
     */
    @SuppressWarnings("unchecked")
    @Override
    public void accept(Map<String, Object> queryMetadata) {
        ConsistencyLevel consistencyLevel = getConsistencyLevel(queryMetadata);
        String statement = (String) queryMetadata.get(QUERY);
        Statement toExecute = null;
        if (statement != null) {
            toExecute = new SimpleStatement(statement);
        } else {
            String statementId = (String) queryMetadata.get(PREPARED_STATEMENT_ID);
            if (statementId == null || statementId.isEmpty()) {
                throw new InvalidQueryMetadataException("Statement id cannot be null nor empty");
            }
            List<Object> values = (List<Object>) queryMetadata.get(VALUES);
            if (values == null || values.isEmpty()) {
                throw new InvalidQueryMetadataException("Values cannot be null nor empty");
            }
            com.datastax.driver.core.PreparedStatement preparedStatement = preparedStatements.get(statementId);
            if (preparedStatement == null) {
                throw new InvalidQueryMetadataException(
                        "Prepared statement with id: " + statementId + " not defined within configuration.");
            }
            toExecute = preparedStatement.bind(values.toArray(new Object[values.size()]));
        }

        toExecute.setConsistencyLevel(consistencyLevel);
        if (async) {
            session.executeAsync(toExecute);
        } else {
            session.execute(toExecute);
        }
    }

    private ConsistencyLevel getConsistencyLevel(Map<String, Object> queryMetadata) {
        String consistency = (String) queryMetadata.get(CONSISTENCY_LEVEL);
        if (consistency == null || consistency.isEmpty()) {
            return ConsistencyLevel.ONE;
        }
        return ConsistencyLevel.valueOf(consistency);
    }
}
