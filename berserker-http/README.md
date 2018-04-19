# Berserker HTTP

Worker implementation which sends HTTP request on configured endpoint.

Configuration can define following properties:
1. `async` - Can be `true` or `false`. Determines whether messages will be sent in asynchronous fashion or not. Optional, if not specified defaults to `false`.
2. `keep-alive` - Can be `true` or `false`. Determines whether HTTP keep-alive is enabled or disabled. Optional, defaults to `true`.
3. `max-connections` - The maximum number of connections a HTTP client can handle, or `-1` for no connection limit. Optional, if not specified defaults to `-1`.
4. `max-connections-per-host` - The maximum number of connections per host a HTTP client can handle, or `-1` for no connection limit. Optional, if not specified defaults to `-1`.
5. `connect-timeout` - The maximum time in millisecond a HTTP client can wait when connecting to a remote host. Optional, if not specified defaults to `5000`.
6. `read-timeout` - The maximum time in millisecond a HTTP client can stay idle. Optional, if not specified defaults to `60000`.
7. `pooled-connection-idle-timeout` - The maximum time in millisecond a HTTP client will keep connection in pool. Optional, if not specified defaults to `60000`.
8. `request-timeout` - The maximum time in millisecond a HTTP client waits until the response is completed. Optional, if not specified defaults to `60000`.
9. `follow-redirect` - Can be `true` or `false`. Determines whether HTTP redirect is enabled. Optional, if not specified defaults to `true`.
10. `max-redirects` - The maximum number of HTTP redirects. Optional, if not specified defaults to `5`.
11. `max-request-retry` - The number of time the library will retry when an error occurs by the remote server. Optional, if not specified defaults to `5`.
12. `connection-ttl` - The maximum time in millisecond a HTTP client will keep connection in the pool, or `-1` to keep connection while possible. Optional, if not specified defaults to `-1`.
13. `base-url` - Can be concatenated with request property `url-sufix` to construct URL. Optional, depending on whether `url` or `url-sufix` is specified.
14.. `headers` - Contains headers in a form of name-value map which will be added to each request. Optional.

Worker `accept` method expects following properties:
1. `url` - whole url to be used, it ignores `base-url`. Mutually exclusive with `url-sufix`.
2. `url-sufix` - concatenates to `base-url` to construct url. Mutually exclusive with `url`.
3. `method-type` - Method type of the request. Mandatory.
4. `headers` - header names with its values. It will override headers with same name defined in configuration. Optional.
5. `body` - Payload of the request. Available only when `POST` or `PUT` method types are used. Optional.

## Configuration

Example yaml configuration:

```yaml
worker-configuration:
  async: false
  keep-alive: true
  max-connections: -1
  max-connections-per-host: -1
  connect-timeout: 5000
  read-timeout: 60000
  pooled-connection-idle-timeout: 60000
  request-timeout: 60000
  follow-redirect: true
  max-redirects: 5
  max-request-retry: 5
  connection-ttl: -1
  base-url: http://localhost:8080/api/item
  headers:
    Content-Type: application/json
    X-Custom-Header-1: custom-value
```

For whole configuration, take a look at [Ranger-HTTP example](../berserker-runner/src/example/resources/ranger-http.yml).
