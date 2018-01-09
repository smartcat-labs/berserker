# Berserker RabbitMQ

Worker implementation which sends AMQP messages to RabbitMQ.

Configuration defines five properties in order for `ConnectionFactory` to be created:

1. `username`
2. `password`
3. `host`
4. `port`
5. `virtual-host`

Worker `accept` method expects following properties:

1. `exchangeName` (String) - Mandatory
2. `routingKey` (String) - Mandatory
3. `messageContent` (String) - Mandatory
4. `contentType` - (String) - Optional
5. `contentEncoding` - (String) - Optional
6. `headers` - (Map) - Optional
7. `deliveryMode` - (Integer) - Optional
8. `priority` - (Integer) - Optional
9. `correlationId` - (String) - Optional
10. `replyTo` - (String) - Optional
11. `expiration` - (String) - Optional
12. `messageId` - (String) - Optional
13. `timestamp` - (Date) - Optional
14. `type` - (String) - Optional
15. `userId` - (String) - Optional
16. `appId` - (String) - Optional
17. `clusterId` - (String) - Optional

## Configuration

Example yaml configuration:

```yaml
worker-configuration:
  username: test
  password: test
  host: localhost
  port: 5672
  virtual-host: /rabbitmq_test
```

For whole configuration, take a look at [Ranger-RabbitMQ example](../berserker-runner/src/example/resources/ranger-rabbitmq.yml).