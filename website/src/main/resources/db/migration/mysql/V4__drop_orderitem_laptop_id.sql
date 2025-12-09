-- V4 (MySQL): Remove legacy laptop_id column from order_items
ALTER TABLE order_items
    DROP FOREIGN KEY FKntwt12snw188o34x2m01na7jm;

ALTER TABLE order_items
    DROP COLUMN laptop_id;
