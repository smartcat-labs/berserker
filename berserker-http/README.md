# Berserker HTTP

Worker implementation which sends HTTP request on configured endpoint.

Configuration can define two properties:
1. `base-url` - can be concatenated with request property `url-sufix` to construct url.
2. `headers` - contains header names with its values.

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
  base-url: http://localhost:8080/api/item
  headers:
    Content-Type: application/json
    X-Custom-Header-1: custom-value
```

For whole configuration, take a look at [Ranger-HTTP example](../berserker-runner/src/example/resources/ranger-http.yml).