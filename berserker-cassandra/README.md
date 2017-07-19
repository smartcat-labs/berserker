# Berserker Cassandra

Worker implementation which executes CQL statements on Cassandra cluster.

## Configuration

Example yaml configuration:

```yaml
worker-configuration:
  connection-points: 127.0.0.1:9042
  keyspace: custom
  async: false
  bootstrap-commands:
    - "CREATE KEYSPACE IF NOT EXISTS custom WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};"
    - USE custom;
    - CREATE TABLE IF NOT EXISTS user (id bigint, firstName text, lastName text, year bigint, primary key (id));
  prepared-statements:
    - id: st1
      query: INSERT INTO user (id, firstName, lastName, year) VALUES (?, ?, ?, ?);
``` 