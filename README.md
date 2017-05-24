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
  rate-generator-configuration-name: ConstantRateGenerator
  worker-configuration-name: Kafka

data-source-configuration:
  objectConfiguration:
    fields:
      -
        name: key
        values: Key1,Key2,Key3,Key4,Key5,Key6,Key7,Key8,Key9,Key10
      -
        name: value
        values: Value1,Value2,Value3,Value4,Value5,Value6,Value7,Value8
    numberOfObjects: 10000

rate-generator-configuration:
  rate: 1000

worker-configuration:
  bootstrap.servers: 192.168.1.1:9092,192.168.1.2:9092,192.168.1.3:9092
  topic.name: test-topic
```

Main part of configuration is `load-generator-configuration` where concrete modules which will be used for data source, rate generator and worker need to be specified. After `load-generator-configuration` section, there should be exactly one section for data source, rate generator and worker.
Each section is allowed to contain module specific configuration as configuration interpretation will be done by module itself.
In order for berserker-runner to be able to find particular module, each module jar must be in classpath.

### Modules

List of existing modules:

#### Berserker Ranger

[Berserker Ranger](berserker-ranger) is Ranger data source implementation.

#### Berserker Kafka

[Berserker Kafka](berserker-kafka) is worker implementation which sends messages to Kafka cluster.

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

Download [Berserker Runner](https://bintray.com/smartcat-labs/maven/download_file?file_path=io%2Fsmartcat%2Franger-runner%2F0.0.5%2Franger-runner-0.0.5.jar).

Run following command: `java -jar berserker-runner-0.0.5.jar -c <path_to_config_file>`

For more info on configuration file, see [Berserker Runner](#berserker-runner) section.
