# Berserker HTTP

Worker implementation which sends HTTP request on configured endpoint.

Configuration can define following properties:
1. `async` - Can be `true` or `false`. Determines whether messages will be sent in asynchronous fashion or not. Optional, if not specified, defaults to `false`.
2. `base-url` - Can be concatenated with request property `url-sufix` to construct URL. Optional, depending on whether `url` or `url-sufix` is specified.
3. `headers` - Contains header names with its values which will be added to each request. Optional.

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
  base-url: http://localhost:8080/api/item
  headers:
    Content-Type: application/json
    X-Custom-Header-1: custom-value
```

For whole configuration, take a look at [Ranger-HTTP example](../berserker-runner/src/example/resources/ranger-http.yml).