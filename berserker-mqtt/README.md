# Berserker RabbitMQ

Worker implementation which publishes messages to MQTT broker.

Configuration defines several properties:

1. `async` - Can be `true` or `false`. Determines whether messages will be sent in asynchronous fashion or not. Optional, if not specified, defaults to `false`.
2. `broker-url`- URL of MQTT broker to which to connect to. Mandatory.
3. `client-id` - Client ID. Mandatory.
4. `max-inflight` - Maximum number of inflight messages. Should be increased when using asynchronous worker or QoS > 0. Value should follow the desired rate. Optional, if not specified, defaults to 10.
5. `clean-session` - Can be `true` or `false`. Determines whether client and server should remember state across restarts and reconnects. Optional, if not specified, defaults to `true`.
6. `connection-timeout` - Connection timeout in seconds. Optional, if not specified, defaults to `30` seconds.
7. `mqtt-version` - MQTT version to use. Possible values: `3.1.1` and `3.1`. Optional, if not specified, defaults to `3.1.1`, if that fails, tries `3.1`.
8. `username` - Username to connect to MQTT broker. Optional, if not specified, no username will be used.
9. `password` - Password to connect to MQTT broker. Optional, if not specified, no password will be used.

Worker `accept` method expects following properties:

1. `topic` (String) - Topic to which message will be published. Mandatory.
2. `qos` (Integer) - Quality of Service. Possible values: 0 (At most once), 1 (At least once), 2 (Exactly once). Mandatory.
3. `payload` (String) - Payload of message to be published. Mandatory.

## Configuration

Example yaml configuration:

```yaml
worker-configuration:
  async: false
  broker-url: "tcp://localhost:1883"
  client-id: client-1
  clean-session: true
  connection-timeout: 20
  mqtt-version: 3.1.1
```

For whole configuration, take a look at [Ranger-MQTT example](../berserker-runner/src/example/resources/ranger-mqtt.yml).
