Guidance for SQL Server migrations

This folder should contain SQL Server compatible Flyway migration scripts (e.g. files named `V10__..._sqlserver.sql`, `V11__..._sqlserver.sql`, etc.).

If you previously had MySQL migration files in `src/main/resources/db/migration` (V1..V4), move the SQL Server versions of those files here instead.

Example:

- Move `V10__init_categories_sqlserver.sql` -> this folder
- Move `V11__update_categories_sqlserver.sql` -> this folder

Do NOT keep MySQL-specific scripts (those using `DROP FOREIGN KEY`, MySQL `AUTO_INCREMENT`, etc.) in the same `db/migration` location when running against SQL Server.

If you need to preserve MySQL scripts for other environments, move them to `db/migration/mysql` (see the README there).
