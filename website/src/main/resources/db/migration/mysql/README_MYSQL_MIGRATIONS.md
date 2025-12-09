MySQL migration scripts (preserve only for MySQL deployments)

If you are running the application against MySQL, place MySQL-compatible Flyway migration files here (for example `V1__init_categories.sql`, `V2__update_categories.sql`, etc.).

When running against SQL Server, DO NOT leave MySQL scripts in the active Flyway location `classpath:db/migration` — they will likely fail.

To restore MySQL behavior, update `application.properties` to point `spring.flyway.locations=classpath:db/migration/mysql` or use a Spring profile.
