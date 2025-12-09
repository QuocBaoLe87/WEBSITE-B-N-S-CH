-- ================================================
-- Tạo database bookmax và sao chép dữ liệu từ laptopdb
-- ================================================

-- Bước 1: Tạo database mới
CREATE DATABASE IF NOT EXISTS bookmax CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Bước 2: Sao chép cấu trúc và dữ liệu từng bảng
-- Thay đổi các tên bảng dưới đây theo cấu trúc thực tế của laptopdb

-- Lấy danh sách tất cả bảng từ laptopdb
USE laptopdb;
SHOW TABLES;

-- ================================================
-- Sao chép từng bảng (ví dụ)
-- ================================================

-- Cách 1: Tạo bảng mới và copy dữ liệu (theo đúng thứ tự bảng thực tế)

-- Bảng announcements
CREATE TABLE bookmax.announcements LIKE laptopdb.announcements;
INSERT INTO bookmax.announcements SELECT * FROM laptopdb.announcements;

-- Bảng brand
CREATE TABLE bookmax.brand LIKE laptopdb.brand;
INSERT INTO bookmax.brand SELECT * FROM laptopdb.brand;

-- Bảng categories
CREATE TABLE bookmax.categories LIKE laptopdb.categories;
INSERT INTO bookmax.categories SELECT * FROM laptopdb.categories;

-- Bảng category
CREATE TABLE bookmax.category LIKE laptopdb.category;
INSERT INTO bookmax.category SELECT * FROM laptopdb.category;

-- Bảng chat_messages
CREATE TABLE bookmax.chat_messages LIKE laptopdb.chat_messages;
INSERT INTO bookmax.chat_messages SELECT * FROM laptopdb.chat_messages;

-- Bảng discounts
CREATE TABLE bookmax.discounts LIKE laptopdb.discounts;
INSERT INTO bookmax.discounts SELECT * FROM laptopdb.discounts;

-- Bảng laptops
CREATE TABLE bookmax.laptops LIKE laptopdb.laptops;
INSERT INTO bookmax.laptops SELECT * FROM laptopdb.laptops;

-- Bảng order_items
CREATE TABLE bookmax.order_items LIKE laptopdb.order_items;
INSERT INTO bookmax.order_items SELECT * FROM laptopdb.order_items;

-- Bảng orders
CREATE TABLE bookmax.orders LIKE laptopdb.orders;
INSERT INTO bookmax.orders SELECT * FROM laptopdb.orders;

-- Bảng password_reset_tokens
CREATE TABLE bookmax.password_reset_tokens LIKE laptopdb.password_reset_tokens;
INSERT INTO bookmax.password_reset_tokens SELECT * FROM laptopdb.password_reset_tokens;

-- Bảng return_requests
CREATE TABLE bookmax.return_requests LIKE laptopdb.return_requests;
INSERT INTO bookmax.return_requests SELECT * FROM laptopdb.return_requests;

-- Bảng reviews
CREATE TABLE bookmax.reviews LIKE laptopdb.reviews;
INSERT INTO bookmax.reviews SELECT * FROM laptopdb.reviews;

-- Bảng users
CREATE TABLE bookmax.users LIKE laptopdb.users;
INSERT INTO bookmax.users SELECT * FROM laptopdb.users;

-- ================================================
-- LƯU Ý: 
-- 1. Kiểm tra danh sách bảng thực tế của bạn bằng: SHOW TABLES;
-- 2. Thêm/bớt các bảng tương ứng
-- 3. Chạy từng đoạn trong MySQL Workbench
-- ================================================
