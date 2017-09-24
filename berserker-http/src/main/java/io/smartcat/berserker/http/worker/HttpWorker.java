package io.smartcat.berserker.http.worker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import io.smartcat.berserker.api.Worker;

/**
 * Worker that sends HTTP requests to HTTP server.
 */
public class HttpWorker implements Worker<Map<String, Object>> {

    private static final String URL = "url";
    private static final String URL_SUFIX = "url-sufix";
    private static final String HEADERS = "headers";
    private static final String METHOD_TYPE = "method-type";
    private static final String BODY = "body";
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String HEAD = "HEAD";
    private static final List<String> METHOD_TYPES = Arrays.asList(GET, POST, PUT, DELETE, HEAD);

    private final String baseUrl;
    private final Map<String, String> headers;

    /**
     * Constructs HTTP worker with specified properties.
     *
     * @param baseUrl Can be concatenated with request property <code>url-sufix</code> to constructs url.
     * @param headers Map of headers to use for each request.
     */
    public HttpWorker(String baseUrl, Map<String, String> headers) {
        this.baseUrl = baseUrl;
        this.headers = headers;
    }

    /**
     * Accepts following arguments:
     * <ul>
     * <li><code><b>url</b></code> - Url to which request will be sent, <code><b>base-url</b></code> from configuration
     * is ignored in this case. Optional, but either <code><b>url</b></code> or <code><b>url-sufix</b></code> must
     * appear.</li>
     * <li><code><b>url-sufix</b></code> - Url sufix to concatenate to <code><b>base-url</b></code> from configuration.
     * <code><b>base-url</b></code> needs to be present in this case, otherwise exception will be thrown. Optional, but
     * either <code><b>url</b></code> or <code><b>url-sufix</b></code> must appear.</li>
     * <li><code><b>headers</b></code> - Key - value map of header names and header values. It will be merged with
     * headers from configuration and override same headers. Optional.</li>
     * <li><code><b>method-type</b></code> - Method type to use for this request. Mandatory.</li>
     * <li><code><b>body</b></code> - Body content, applicable only for <code>POST</code> and <code>PUT</code> method
     * types.</li>
     * </ul>
     */
    @Override
    public void accept(Map<String, Object> requestMetadata) {
        String url = (String) requestMetadata.get(URL);
        String urlSufix = (String) requestMetadata.get(URL_SUFIX);
        Map<String, String> requestHeaders = getHeaders(requestMetadata);
        String methodType = getMethodType(requestMetadata);
        String body = getBodyIfNeeded(requestMetadata, methodType);

        String calculatedUrl = getCalculatedUrl(url, urlSufix);
        Map<String, String> calculatedHeaders = getCalculatedHeaders(requestHeaders);

        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpUriRequest request = createRequest(methodType, calculatedUrl, body);
            calculatedHeaders.forEach((k, v) -> request.addHeader(k, v));
            httpClient.execute(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getHeaders(Map<String, Object> requestMetadata) {
        Map<String, String> result = new HashMap<>();
        Map<String, Object> headers = (Map<String, Object>) requestMetadata.get(HEADERS);
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

    private String getMethodType(Map<String, Object> requestMetadata) {
        String methodType = (String) requestMetadata.get(METHOD_TYPE);
        if (methodType == null) {
            throw new RuntimeException("Method type is mandatory.");
        }
        if (!METHOD_TYPES.contains(methodType)) {
            throw new RuntimeException(
                    "Expected any of supported method types: " + METHOD_TYPES + " but method type was: " + methodType);
        }
        return methodType;
    }

    private String getBodyIfNeeded(Map<String, Object> requestMetadata, String methodType) {
        if (methodType.equals(POST) || methodType.equals(PUT)) {
            return (String) requestMetadata.get(BODY);
        }
        return null;
    }

    private String getCalculatedUrl(String url, String urlSufix) {
        if (url == null && urlSufix == null) {
            throw new RuntimeException("One needs to be specified, either url or url-sufix.");
        }
        if (url != null && urlSufix != null) {
            throw new RuntimeException("Cannot have both url and url-sufix.");
        }
        String result = null;
        if (url != null) {
            result = url;
        }
        if (urlSufix != null && baseUrl == null) {
            throw new RuntimeException("base-url must be specified when url-sufix is used.");
        }
        if (urlSufix != null) {
            result = baseUrl + urlSufix;
        }
        return result;
    }

    private Map<String, String> getCalculatedHeaders(Map<String, String> requestHeaders) {
        Map<String, String> result = new HashMap<>();
        result.putAll(headers);
        result.putAll(requestHeaders);
        return result;
    }

    private HttpUriRequest createRequest(String methodType, String url, String body)
            throws UnsupportedEncodingException {
        if (methodType.equals(GET)) {
            return new HttpGet(url);
        }
        if (methodType.equals(POST)) {
            HttpPost request = new HttpPost(url);
            if (body != null) {
                request.setEntity(new ByteArrayEntity(body.getBytes()));
            }
            return request;
        }
        if (methodType.equals(PUT)) {
            HttpPut request = new HttpPut(url);
            if (body != null) {
                request.setEntity(new ByteArrayEntity(body.getBytes()));
            }
            return request;
        }
        if (methodType.equals(DELETE)) {
            return new HttpDelete(url);
        }
        if (methodType.equals(HEAD)) {
            return new HttpHead(url);
        }
        throw new RuntimeException();
    }
}
