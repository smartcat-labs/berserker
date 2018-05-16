# Berserker Kafka

Worker implementation which sends messages to Kafka cluster.

Configuration can define following properties:
1. `async` - Can be `true` or `false`. Determines whether messages will be sent in asynchronous fashion or not. Optional, if not specified defaults to `false`.
2. `topic` - Name of the topic to which message will be sent to. Mandatory.
3. `producer-configuration` - Below this placeholder Kafka specific properties should be defined. List of properties is defined within Kafka [documentation](https://kafka.apache.org/documentation/#producerconfigs).

Worker `accept` method expects following properties:
1. `key` - (String) Key of Kafka message. Mandatory.
2. `value` - (String) Value of Kafka message. Mandatory.

## Configuration

Example yaml configuration:

```yaml
worker-configuration:
  async: true
  topic: topic1
  producer-configuration:
      bootstrap.servers: 192.168.0.5:32772
``` 
