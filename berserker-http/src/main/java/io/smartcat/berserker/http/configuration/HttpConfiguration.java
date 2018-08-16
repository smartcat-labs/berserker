package io.smartcat.berserker.http.configuration;

import static io.smartcat.berserker.configuration.ConfigurationHelper.getOptionalValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.smartcat.berserker.api.Worker;
import io.smartcat.berserker.configuration.WorkerConfiguration;
import io.smartcat.berserker.http.worker.HttpWorker;

/**
 * Configuration for HTTP worker.
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

    /**
     * Creates an instance of {@link HttpWorker} for given set of configuration properties.
     * Configuration map should contain following:
     * <ul>
     * <li><code><b>async</b></code> - Indicates whether request should be sent in async or sync fashion. Optional,
     * defaults to <code>false</code>.</li>
     * <li><code><b>keep-alive</b></code> - Indicates whether HTTP keep-alive is enabled or disabled. Optional,
     * defaults to <code>true</code>.</li>
     * <li><code><b>max-connections</b></code> - The maximum number of connections a HTTP client can handle, or
     * <code>-1</code> for no connection limit. Optional, defaults to <code>-1</code>.</li>
     * <li><code><b>max-connections-per-host</b></code> - The maximum number of connections per host a HTTP client can
     * handle, or <code>-1</code> for no connection limit. Optional, defaults to <code>-1</code>.</li>
     * <li><code><b>connect-timeout</b></code> - The maximum time in millisecond a HTTP client can wait when connecting
     * to a remote host. Optional, defaults to <code>5000</code>.</li>
     * <li><code><b>read-timeout</b></code> - The maximum time in millisecond a HTTP client can stay idle. Optional,
     * defaults to <code>60000</code>.</li>
     * <li><code><b>pooled-connection-idle-timeout</b></code> - The maximum time in millisecond a HTTP client will keep
     * connection in pool. Optional, defaults to <code>60000</code>.</li>
     * <li><code><b>request-timeout</b></code> - The maximum time in millisecond a HTTP client waits until the response
     * is completed. Optional, defaults to <code>60000</code>.</li>
     * <li><code><b>follow-redirect</b></code> - Indicates whether HTTP redirect is enabled. Optional, defaults to
     * <code>true</code>.</li>
     * <li><code><b>max-redirects</b></code> - The maximum number of HTTP redirects. Optional, defaults to
     * <code>5</code>.</li>
     * <li><code><b>max-request-retry</b></code> - The number of time the library will retry when an error occurs by
     * the remote server. Optional, defaults to <code>5</code>.</li>
     * <li><code><b>connection-ttl</b></code> - The maximum time in millisecond a HTTP client will keep connection in
     * the pool, or `-1` to keep connection while possible. Optional, defaults to <code>-1</code>.</li>
     * <li><code><b>base-url</b></code> - Base url to use, mandatory if worker will accept
     * <code><b>url-sufix</b></code>, unnecessary if worker will accept <code><b>url</b></code>.</li>
     * <li><code><b>headers</b></code> - Contains headers in a form of name-value map which will be added to each
     * request. Optional.</li>
     * <li><code><b>error-codes</b></code> - List of HTTP codes to be considered as errors. Optional, defaults to
     * all <code>4**</code> and <code>5**</code> codes.</li>
     * </ul>
     * @param configuration Configuration specific to this worker.
     * @return An instance of {@link HttpWorker}.
     */
    @Override
    public Worker<?> getWorker(Map<String, Object> configuration) {
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
