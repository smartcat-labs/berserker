# Berserker Kinesis

Worker implementation which sends messages to AWS Kinesis.

Configuration can define following properties:
1. `async` - Can be `true` or `false`. Determines whether messages will be sent in asynchronous fashion or not. Optional, if not specified defaults to `false`.
2. `stream` - Name of the Kinesis stream to which message will be sent to. Required. 
3. `producer-configuration` - Below this placeholder Kinesis specific properties should be defined. List of properties is defined within Kinesis [documentation](https://docs.aws.amazon.com/streams/latest/dev/kinesis-kpl-config.html).

Worker `accept` method expects following properties:
2. `msg` - (String) Content of Kinesis message. Mandatory.

## Configuration

Example yaml configuration:

```yaml
worker-configuration:
  async: true
  stream: op-test
  producer-configuration:
    RecordMaxBufferedTime: 100
    MaxConnections: 4
    RequestTimeout: 6000
    Region: eu-central-1
```

Authentication to AWS Kinesis is done by reading ENV vars or `~/.aws/credentials` with precedence defined in KPL's `DefaultAWSCredentialsProviderChain()` method. 