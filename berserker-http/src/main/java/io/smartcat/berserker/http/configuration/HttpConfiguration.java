package io.smartcat.berserker.http.configuration;

import static io.smartcat.berserker.configuration.ConfigurationHelper.getOptionalValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.smartcat.berserker.api.Worker;
import io.smartcat.berserker.configuration.ConfigurationParseException;
import io.smartcat.berserker.configuration.WorkerConfiguration;
import io.smartcat.berserker.http.worker.HttpWorker;

/**
 * Configuration for HTTP worker. Configuration map should contain following:
 * <ul>
 * <li><code><b>base-url</b></code> - Base url to use, mandatory if worker will accept <code><b>url-sufix</b></code>,
 * unnecessary if worker will accept <code><b>url</b></code>.</li>
 * <li><code><b>headers</b></code> - Key - value map of header names and header values.</li>
 * </ul>
 * All other values in map are ignored.
 */
public class HttpConfiguration implements WorkerConfiguration {

    private static final String ASYNC = "async";
    private static final String KEEP_ALIVE = "keep-alive";
    private static final String MAX_CONNECTIONS = "max-connections";
    private static final String MAX_CONNECTIONS_PER_HOST = "max-connections-per-host";
    private static final String CONNECT_TIMEOUT = "connect-timeout";
    private static final String READ_TIMEOUT = "read-timeout";
    private static final String POOLED_CONNECTION_IDLE_TIMEOUT = "pooled-connection-idle-timeout";
    private static final String REQUEST_TIMEOUT = "request-timeout";
    private static final String FOLLOW_REDIRECT = "follow-redirect";
    private static final String MAX_REDIRECTS = "max-redirects";
    private static final String MAX_REQUEST_RETRY = "max-request-retry";
    private static final String CONNECTION_TTL = "connection-ttl";
    private static final String BASE_URL = "base-url";
    private static final String HEADERS = "headers";
    private static final String ERROR_CODES = "error-codes";

    private static final List<Integer> DEFAULT_ERROR_CODES = Arrays.asList(400, 401, 402, 403, 404, 405, 406, 407, 408,
            409, 410, 411, 412, 413, 414, 415, 416, 417, 418, 421, 422, 423, 424, 426, 428, 429, 431, 451, 500, 501,
            502, 503, 504, 505, 506, 507, 508, 510, 511);

    @Override
    public String getName() {
        return "HTTP";
    }

    @Override
    public Worker<?> getWorker(Map<String, Object> configuration) throws ConfigurationParseException {
        boolean async = getOptionalValue(configuration, ASYNC, false);
        boolean keepAlive = getOptionalValue(configuration, KEEP_ALIVE, true);
        int maxConnections = getOptionalValue(configuration, MAX_CONNECTIONS, -1);
        int maxConnectionsPerHost = getOptionalValue(configuration, MAX_CONNECTIONS_PER_HOST, -1);
        int connectTimeout = getOptionalValue(configuration, CONNECT_TIMEOUT, 5000);
        int readTimeout = getOptionalValue(configuration, READ_TIMEOUT, 60000);
        int pooledConnectionIdleTimeout = getOptionalValue(configuration, POOLED_CONNECTION_IDLE_TIMEOUT, 60000);
        int requestTimeout = getOptionalValue(configuration, REQUEST_TIMEOUT, 60000);
        boolean followRedirect = getOptionalValue(configuration, FOLLOW_REDIRECT, true);
        int maxRedirects = getOptionalValue(configuration, MAX_REDIRECTS, 5);
        int maxRequestRetry = getOptionalValue(configuration, MAX_REQUEST_RETRY, 5);
        int connectionTtl = getOptionalValue(configuration, CONNECTION_TTL, -1);
        List<Integer> errorCodes = getOptionalValue(configuration, ERROR_CODES, DEFAULT_ERROR_CODES);
        String baseUrl = (String) configuration.get(BASE_URL);
        Map<String, String> headers = getHeaders(configuration);

        return new HttpWorker(async, keepAlive, maxConnections, maxConnectionsPerHost, connectTimeout, readTimeout,
                pooledConnectionIdleTimeout, requestTimeout, followRedirect, maxRedirects, maxRequestRetry,
                connectionTtl, baseUrl, headers, errorCodes);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getHeaders(Map<String, Object> configuration) {
        Map<String, String> result = new HashMap<>();
        Map<String, Object> headers = (Map<String, Object>) configuration.get(HEADERS);
        if (headers == null) {
            return result;
        }
        for (Map.Entry<String, Object> header : headers.entrySet()) {
            if (!(header.getValue() instanceof String)) {
                throw new RuntimeException("All headers need to have string value. Header: " + header.getKey()
                        + " has value: " + header.getValue() + " of type: " + header.getValue().getClass().getName());
            }
            result.put(header.getKey(), (String) header.getValue());
        }
        return result;
    }
}
