CREATE DATABASE IF NOT EXISTS housekeeping_platform
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE housekeeping_platform;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(20) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
) COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(50) NOT NULL UNIQUE,
  role_name VARCHAR(50) NOT NULL
) COMMENT='系统角色表';

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_role (user_id, role_id),
  CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) COMMENT='用户角色关系表';

CREATE TABLE IF NOT EXISTS service_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(300) NOT NULL,
  price_label VARCHAR(50) NOT NULL,
  slug VARCHAR(100) NOT NULL UNIQUE
) COMMENT='服务分类表';

CREATE TABLE IF NOT EXISTS worker_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,
  name VARCHAR(50) NOT NULL,
  role_label VARCHAR(50) NOT NULL,
  rating DECIMAL(3,2) NOT NULL DEFAULT 5.00,
  completed_orders INT NOT NULL DEFAULT 0,
  hourly_price INT NOT NULL,
  city VARCHAR(50) NOT NULL,
  intro VARCHAR(500) NOT NULL,
  tags VARCHAR(500) NOT NULL,
  next_available VARCHAR(100) NOT NULL,
  years_of_experience INT NOT NULL DEFAULT 0,
  certificates VARCHAR(500) NOT NULL,
  service_areas VARCHAR(500) NOT NULL,
  service_cases VARCHAR(500) NOT NULL
) COMMENT='服务人员档案表';

CREATE TABLE IF NOT EXISTS worker_application (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  service_types VARCHAR(255) NOT NULL,
  years_of_experience INT NOT NULL DEFAULT 0,
  certificates VARCHAR(500) NOT NULL,
  service_areas VARCHAR(255) NOT NULL,
  available_schedule VARCHAR(255) NOT NULL,
  intro VARCHAR(500) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  admin_remark VARCHAR(255) DEFAULT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  CONSTRAINT fk_worker_application_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT='服务人员入驻申请表';

CREATE TABLE IF NOT EXISTS booking_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,
  service_name VARCHAR(100) NOT NULL,
  worker_id BIGINT NOT NULL,
  customer_name VARCHAR(50) NOT NULL,
  contact_phone VARCHAR(20) NOT NULL,
  service_address VARCHAR(255) NOT NULL,
  booking_date VARCHAR(20) NOT NULL,
  booking_slot VARCHAR(50) NOT NULL,
  status VARCHAR(30) NOT NULL,
  progress_note VARCHAR(255) NOT NULL,
  remark VARCHAR(500) NOT NULL,
  CONSTRAINT fk_booking_order_worker FOREIGN KEY (worker_id) REFERENCES worker_profile(id)
) COMMENT='预约订单表';

CREATE TABLE IF NOT EXISTS booking_order_progress (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  progress_status VARCHAR(30) NOT NULL,
  progress_note VARCHAR(255) NOT NULL,
  CONSTRAINT fk_order_progress_order FOREIGN KEY (order_id) REFERENCES booking_order(id)
) COMMENT='订单进度表';

CREATE TABLE IF NOT EXISTS order_review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  worker_id BIGINT NOT NULL,
  rating INT NOT NULL,
  content VARCHAR(500) NOT NULL,
  created_at DATETIME NOT NULL,
  UNIQUE KEY uk_order_review_order (order_id),
  CONSTRAINT fk_order_review_order FOREIGN KEY (order_id) REFERENCES booking_order(id),
  CONSTRAINT fk_order_review_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_order_review_worker FOREIGN KEY (worker_id) REFERENCES worker_profile(id)
) COMMENT='订单评价表';

CREATE TABLE IF NOT EXISTS order_after_sale (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  worker_id BIGINT NOT NULL,
  issue_type VARCHAR(50) NOT NULL,
  content VARCHAR(500) NOT NULL,
  contact_phone VARCHAR(20) NOT NULL,
  status VARCHAR(30) NOT NULL,
  admin_remark VARCHAR(255) NOT NULL DEFAULT '',
  created_at DATETIME NOT NULL,
  handled_at DATETIME NULL,
  UNIQUE KEY uk_after_sale_order (order_id),
  CONSTRAINT fk_after_sale_order FOREIGN KEY (order_id) REFERENCES booking_order(id),
  CONSTRAINT fk_after_sale_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_after_sale_worker FOREIGN KEY (worker_id) REFERENCES worker_profile(id)
) COMMENT='售后反馈表';
