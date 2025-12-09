-- V3 (MySQL): Verify categories - sample
DELETE FROM categories WHERE id NOT IN (1,2,3);
UPDATE categories SET name = 'Sách bán chạy' WHERE id = 1;
UPDATE categories SET name = 'Sách thiếu nhi' WHERE id = 2;
UPDATE categories SET name = 'Sách kỹ năng' WHERE id = 3;
