-- V14: convert column types and drop constraints (SQL Server)
SET NOCOUNT ON;
IF OBJECT_ID('books', 'U') IS NOT NULL
BEGIN
    DECLARE @i INT = 1;
    WHILE @i <= 5
    BEGIN
        DECLARE @colName NVARCHAR(50) = 'image' + CAST(@i AS NVARCHAR(2));
        IF EXISTS(SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('books') AND name = @colName)
        BEGIN
            -- Drop default constraint if present
            DECLARE @dfName SYSNAME;
            SELECT @dfName = dc.name
            FROM sys.default_constraints dc
            JOIN sys.columns c ON dc.parent_object_id = c.object_id AND dc.parent_column_id = c.column_id
            WHERE dc.parent_object_id = OBJECT_ID('books') AND c.name = @colName;
            IF @dfName IS NOT NULL
            BEGIN
                EXEC('ALTER TABLE books DROP CONSTRAINT ' + QUOTENAME(@dfName));
            END

            -- Alter column to VARBINARY(MAX)
            EXEC('ALTER TABLE books ALTER COLUMN ' + QUOTENAME(@colName) + ' VARBINARY(MAX) NULL');
        END
        SET @i = @i + 1;
    END
END

IF OBJECT_ID('discounts', 'U') IS NOT NULL
BEGIN
    IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('discounts') AND name = 'active')
    BEGIN
        EXEC('ALTER TABLE discounts ALTER COLUMN active BIT NULL');
    END
END

IF OBJECT_ID('orders', 'U') IS NOT NULL
BEGIN
    IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'payment_method')
        EXEC('ALTER TABLE orders ALTER COLUMN payment_method VARCHAR(255) NULL');
    IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'status')
        EXEC('ALTER TABLE orders ALTER COLUMN status VARCHAR(255) NULL');
END

IF OBJECT_ID('return_requests', 'U') IS NOT NULL
BEGIN
    IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('return_requests') AND name = 'status')
        EXEC('ALTER TABLE return_requests ALTER COLUMN status VARCHAR(255) NULL');
END
