package io.smartcat.berserker.http.worker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.asynchttpclient.Dsl.*;

import org.asynchttpclient.*;
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

    private AsyncHttpClient asyncHttpClient;

    /**
     * Constructs HTTP worker with specified properties.
     *
     * @param baseUrl Can be concatenated with request property <code>url-sufix</code> to constructs url.
     * @param headers Map of headers to use for each request.
     */
    public HttpWorker(String baseUrl, Map<String, String> headers) {
        this.baseUrl = baseUrl;
        this.headers = headers;

        AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder().build();
        asyncHttpClient = new DefaultAsyncHttpClient(config);
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

        asyncHttpClient.executeRequest(
                createRequest(methodType, calculatedUrl, calculatedHeaders, body),
                new AsyncCompletionHandler<Response>() {
                    @Override
                    public Response onCompleted(Response response) throws Exception {
                        return response;
                    }

                    @Override
                    public void onThrowable(Throwable t) {
                        throw new RuntimeException(t);
                    }
                });
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

    private Request createRequest(String methodType, String url, Map<String, String> requestHeaders, String body) {
        final RequestBuilder request;

        switch (methodType) {
            case GET:
                request = get(url);
                break;
            case POST:
                request = post(url);
                if (body != null) {
                    request.setBody(body.getBytes());
                }
                break;
            case PUT:
                request = put(url);
                if (body != null) {
                    request.setBody(body.getBytes());
                }
                break;
            case DELETE:
                request = delete(url);
                break;
            case HEAD:
                request = head(url);
                break;
            default:
                throw new RuntimeException("Unsupported method type: " + methodType);
        }
        requestHeaders.forEach((name, value) -> request.setHeader(name, value));
        return request.build();
    }
}
