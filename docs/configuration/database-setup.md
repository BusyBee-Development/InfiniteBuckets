---
id: database-setup
title: Database Setup
sidebar_position: 4
---

InfiniteBuckets stores bucket cooldowns in a database, so a player can't skip a cooldown by
relogging or waiting for a restart. Nothing else is stored there: bucket uses live on the items
themselves.

## SQLite (default)

No setup. The database is `plugins/InfiniteBuckets/database.db`.

## MySQL

Use MySQL when several servers should share cooldowns. Set it in `config.yml`, then run
`/infb reload` or restart:

```yaml
database:
  type: "MYSQL"
  mysql:
    host: "localhost"
    port: 3306
    database: "infinitebuckets"
    username: "root"
    password: ""
    use-ssl: false
    pool-settings:
      maximum-pool-size: 10
      minimum-idle: 2
      max-lifetime: 1800000
      connection-timeout: 5000
```

| Setting                                    | Description                                       | Default         |
|:-------------------------------------------|:--------------------------------------------------|:----------------|
| `database.type`                            | `SQLITE` or `MYSQL`.                               | `SQLITE`        |
| `database.mysql.host` / `port`             | Where the MySQL server is.                         | `localhost` / `3306` |
| `database.mysql.database`                  | Database name; create it first.                    | `infinitebuckets` |
| `database.mysql.username` / `password`     | Login.                                             | `root` / empty  |
| `database.mysql.use-ssl`                   | Connect over SSL.                                  | `false`         |
| `database.mysql.pool-settings.*`           | Connection pool size and timeouts (milliseconds).  | as above        |

The `player_cooldowns` table is created on first start. On success the console shows
`Successfully initialized MYSQL database.`
