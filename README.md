# Berserker

Load generator with modular architecture.

[![Build Status](https://travis-ci.org/smartcat-labs/berserker.svg?branch=master)](https://travis-ci.org/smartcat-labs/berserker)
[ ![Download](https://api.bintray.com/packages/smartcat-labs/maven/berserker/images/download.svg) ](https://bintray.com/smartcat-labs/maven/berserker/_latestVersion)

## Introduction

Berserker is designed to be modular from beginning as illustrated on the following diagram.

![Core Design](images/core-design.png)

Rate generator controls the rate at which load generator operates, rate is expressed on per second basis, or better say, number of impulses which will be generated within one second. Each time impulse is generated load generator fetches data from data source and passes it to worker. Since those are simple interfaces, it is easy to add additional module implementing either data source, worker and even rate generator.
Following diagram represents possible modules for Load Generator of which some are already implemented.

![Architecture](images/architecture.png)

Berserker is designed as command line tool, but having modular architecture makes it easy to use it as Java library as well.

### Berserker Commons

[Berserker Commons](berserker-commons) holds interface for core and configuration and it provides signature all the modules need to confront to be able to work together.

### Berserker Core

[Berserker Core](berserker-core) contains load generator implementation, and common implementations of data source, rate generator and worker.

### Berserker Runner

[Berserker Runner](berserker-runner) represents runnable jar where desired data source, rate generator and worker can be specified within YAML configuration.
Following section illustrates YAML configuration example.

```yaml
load-generator-configuration:
  data-source-configuration-name: Ranger
  rate-generator-configuration-name: default
  worker-configuration-name: Cassandra
  metrics-reporter-configuration-name: JMX
  thread-count: 10
  queue-capacity: 100000

data-source-configuration:
  values:
    id: uuid()
    firstName: random(['Peter', 'Mike', 'Steven', 'Joshua', 'John', 'Brandon'])
    lastName: random(['Smith', 'Johnson', 'Williams', 'Davis', 'Jackson', 'White', 'Lewis', 'Clark'])
    age: random(20..45)
    email: string('{}@domain.com', randomLengthString(5))
    statement:
      consistencyLevel: ONE
      query: string("INSERT INTO person (id, first_name, last_name, age, email) VALUES ({}, '{}', '{}', {}, '{}');", $id, $firstName, $lastName, $age, $email)
  output: $statement

rate-generator-configuration:
  rates:
    r: 1000
  output: $r

worker-configuration:
  connection-points: 0.0.0.0:32770
  keyspace: my_keyspace
  async: false
  bootstrap-commands:
    - "CREATE KEYSPACE IF NOT EXISTS my_keyspace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};"
    - USE my_keyspace;
    - CREATE TABLE IF NOT EXISTS person (id uuid, first_name text, last_name text, age int, email text, primary key (id));

metrics-reporter-configuration:
  domain: berserker
  filter:
```

Main part of configuration is `load-generator-configuration` where concrete modules which will be used for data source, rate generator and worker need to be specified. After `load-generator-configuration` section, there should be exactly one section for data source, rate generator and worker.
Each section is allowed to contain module specific configuration as configuration interpretation will be done by module itself.
In order for berserker-runner to be able to find particular module, each module jar must be in classpath.

#### Rate generator configuration

Documentation on rate generator configuration can be found [here](rate-generator-configuration.md).

### Modules

List of existing modules:

#### Berserker Ranger

[Berserker Ranger](berserker-ranger) is Ranger data source implementation.

#### Berserker Kafka

[Berserker Kafka](berserker-kafka) is worker implementation which sends messages to Kafka cluster.

#### Berserker Cassandra
[Berserker Cassandra](berserker-cassandra) is worker implementation which executes CQL statements on Cassandra cluster.

#### Berserker HTTP
[Berserker HTTP](berserker-http) is worker implementation which sends HTTP request on configured endpoint.

#### Berserker RabbitMQ
[Berserker RabbitMQ](berserker-rabbitmq) is worker implementation which sends AMQP messages to RabbitMQ.

### Usage

Berserker can be used either as a library or as a stand-alone command line tool.

#### Library usage

Artifact can be fetched from bintray.

Add following `repository` element to your `<repositories>` section in `pom.xml`:

```xml
<repository>
  <id>bintray-smartcat-labs-maven</id>
  <name>bintray</name>
  <url>https://dl.bintray.com/smartcat-labs/maven</url>
</repository>
```

Add the `dependency` element to your `<dependencies>` section in `pom.xml` depending which `artifact` and `version` you need:

```xml
<dependency>
  <groupId>io.smartcat</groupId>
  <artifactId>artifact</artifactId>
  <version>version</version>
</dependency>
```

#### Command line tool usage

- Download latest [Berserker Runner](https://bintray.com/smartcat-labs/maven/berserker) version.
- Create config file (example can be found [here](berserker-runner/src/example/resources/ranger-cassandra.yml)).
- Run following command: `java -jar berserker-runner-<version>.jar -c <path_to_config_file>`
- If you need to specify logging options, you can run berserker this way: `java -jar -Dlogback.configurationFile=<path to logback.xml> berserker-runner-<version>.jar -c <path_to_config_file>`
