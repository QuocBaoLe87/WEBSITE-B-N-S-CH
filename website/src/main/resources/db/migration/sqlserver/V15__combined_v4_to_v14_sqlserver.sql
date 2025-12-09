-- V15: Combined migration (SQL Server) covering converted changes from original MySQL V4..V14
SET NOCOUNT ON;
PRINT 'Starting combined migration V15__combined_v4_to_v14_sqlserver.sql';

BEGIN TRY
    BEGIN TRANSACTION;

    -------------------------------------------------------------------------
    -- 1) Drop legacy foreign key and column (equivalent to V4 / V13)
    -------------------------------------------------------------------------
    PRINT 'Dropping legacy FK and laptop_id column from order_items (if present)';
    IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FKntwt12snw188o34x2m01na7jm' AND parent_object_id = OBJECT_ID('order_items'))
    BEGIN
        ALTER TABLE order_items DROP CONSTRAINT [FKntwt12snw188o34x2m01na7jm];
        PRINT 'Dropped FK FKntwt12snw188o34x2m01na7jm';
    END

    IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('order_items') AND name = 'laptop_id')
    BEGIN
        ALTER TABLE order_items DROP COLUMN laptop_id;
        PRINT 'Dropped column laptop_id from order_items';
    END

    -------------------------------------------------------------------------
    -- 2) Categories setup (converted from V1..V3 -> V10..V12)
    -------------------------------------------------------------------------
    PRINT 'Applying categories initialization and updates';
    IF EXISTS (SELECT 1 FROM sys.tables WHERE name = 'categories')
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM categories WHERE id = 1)
            INSERT INTO categories (id, name) VALUES (1, N'Sách bán chạy');
        IF NOT EXISTS (SELECT 1 FROM categories WHERE id = 2)
            INSERT INTO categories (id, name) VALUES (2, N'Sách thiếu nhi');
        IF NOT EXISTS (SELECT 1 FROM categories WHERE id = 3)
            INSERT INTO categories (id, name) VALUES (3, N'Sách kỹ năng');
    END

    -------------------------------------------------------------------------
    -- 3) Convert columns and drop dependent constraints (V14)
    -------------------------------------------------------------------------
    PRINT 'Dropping dependent default/check constraints and altering column types';

    DECLARE @dfName SYSNAME;
    DECLARE @sql NVARCHAR(MAX);

    DECLARE @i INT = 1;
    WHILE @i <= 5
    BEGIN
        DECLARE @colName NVARCHAR(50) = 'image' + CAST(@i AS NVARCHAR(2));
        IF EXISTS(SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('books') AND name = @colName)
        BEGIN
            SELECT @dfName = dc.name
            FROM sys.default_constraints dc
            JOIN sys.columns c ON dc.parent_object_id = c.object_id AND dc.parent_column_id = c.column_id
            WHERE dc.parent_object_id = OBJECT_ID('books') AND c.name = @colName;

            IF @dfName IS NOT NULL
            BEGIN
                SET @sql = N'ALTER TABLE books DROP CONSTRAINT ' + QUOTENAME(@dfName) + ';';
                EXEC sp_executesql @sql;
            END

            SET @sql = N'ALTER TABLE books ALTER COLUMN ' + QUOTENAME(@colName) + ' VARBINARY(MAX) NULL;';
            EXEC sp_executesql @sql;
        END
        SET @i = @i + 1;
    END

    IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('discounts') AND name = 'active')
    BEGIN
        SELECT @dfName = dc.name
        FROM sys.default_constraints dc
        JOIN sys.columns c ON dc.parent_object_id = c.object_id AND dc.parent_column_id = c.column_id
        WHERE dc.parent_object_id = OBJECT_ID('discounts') AND c.name = 'active';
        IF @dfName IS NOT NULL
        BEGIN
            SET @sql = N'ALTER TABLE discounts DROP CONSTRAINT ' + QUOTENAME(@dfName) + ';'; EXEC sp_executesql @sql;
        END
        ALTER TABLE discounts ALTER COLUMN active BIT NULL;
    END

    IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'payment_method')
    BEGIN
        SELECT @dfName = dc.name
        FROM sys.default_constraints dc
        JOIN sys.columns c ON dc.parent_object_id = c.object_id AND dc.parent_column_id = c.column_id
        WHERE dc.parent_object_id = OBJECT_ID('orders') AND c.name = 'payment_method';
        IF @dfName IS NOT NULL
        BEGIN SET @sql = N'ALTER TABLE orders DROP CONSTRAINT ' + QUOTENAME(@dfName) + ';'; EXEC sp_executesql @sql; END
        ALTER TABLE orders ALTER COLUMN payment_method VARCHAR(255) NULL;
    END

    IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'status')
    BEGIN
        SELECT @dfName = dc.name
        FROM sys.default_constraints dc
        JOIN sys.columns c ON dc.parent_object_id = c.object_id AND dc.parent_column_id = c.column_id
        WHERE dc.parent_object_id = OBJECT_ID('orders') AND c.name = 'status';
        IF @dfName IS NOT NULL
        BEGIN SET @sql = N'ALTER TABLE orders DROP CONSTRAINT ' + QUOTENAME(@dfName) + ';'; EXEC sp_executesql @sql; END
        ALTER TABLE orders ALTER COLUMN status VARCHAR(255) NULL;
    END

    IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('return_requests') AND name = 'status')
    BEGIN
        SELECT @dfName = dc.name
        FROM sys.default_constraints dc
        JOIN sys.columns c ON dc.parent_object_id = c.object_id AND dc.parent_column_id = c.column_id
        WHERE dc.parent_object_id = OBJECT_ID('return_requests') AND c.name = 'status';
        IF @dfName IS NOT NULL
        BEGIN SET @sql = N'ALTER TABLE return_requests DROP CONSTRAINT ' + QUOTENAME(@dfName) + ';'; EXEC sp_executesql @sql; END
        ALTER TABLE return_requests ALTER COLUMN status VARCHAR(255) NULL;
    END

    COMMIT TRANSACTION;
    PRINT 'Combined migration V15 committed successfully.';
END TRY
BEGIN CATCH
    PRINT 'Error encountered during combined migration: ' + ERROR_MESSAGE();
    IF XACT_STATE() <> 0
    BEGIN
        ROLLBACK TRANSACTION;
        PRINT 'Transaction rolled back.';
    END
    THROW;
END CATCH;
GO
