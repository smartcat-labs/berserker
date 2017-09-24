package io.smartcat.berserker.http.configuration;

import java.util.HashMap;
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

    private static final String BASE_URL = "base-url";
    private static final String HEADERS = "headers";

    @Override
    public String getName() {
        return "HTTP";
    }

    @Override
    public Worker<?> getWorker(Map<String, Object> configuration) throws ConfigurationParseException {
        String baseUrl = (String) configuration.get(BASE_URL);
        Map<String, String> headers = getHeaders(configuration);
        return new HttpWorker(baseUrl, headers);
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
