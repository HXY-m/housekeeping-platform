CREATE DATABASE IF NOT EXISTS housekeeping_platform
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE housekeeping_platform;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(20) NOT NULL UNIQUE,
  username VARCHAR(50) NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
) COMMENT='system users';

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(50) NOT NULL UNIQUE,
  role_name VARCHAR(50) NOT NULL
) COMMENT='system roles';

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_role (user_id, role_id),
  CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
  CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) COMMENT='user role relations';

CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  permission_code VARCHAR(80) NOT NULL UNIQUE,
  permission_name VARCHAR(120) NOT NULL,
  permission_group VARCHAR(80) NOT NULL DEFAULT '',
  description VARCHAR(255) NOT NULL DEFAULT ''
) COMMENT='system permissions';

CREATE TABLE IF NOT EXISTS sys_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  UNIQUE KEY uk_role_permission (role_id, permission_id),
  CONSTRAINT fk_sys_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role(id),
  CONSTRAINT fk_sys_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission(id)
) COMMENT='role permission relations';

CREATE TABLE IF NOT EXISTS user_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  gender VARCHAR(20) NOT NULL DEFAULT '',
  city VARCHAR(50) NOT NULL DEFAULT '',
  bio VARCHAR(500) NOT NULL DEFAULT '',
  avatar_url VARCHAR(255) NOT NULL DEFAULT '',
  UNIQUE KEY uk_user_profile_user (user_id),
  CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT='user profiles';

CREATE TABLE IF NOT EXISTS user_address (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  contact_name VARCHAR(50) NOT NULL,
  contact_phone VARCHAR(20) NOT NULL,
  city VARCHAR(50) NOT NULL,
  detail_address VARCHAR(255) NOT NULL,
  address_tag VARCHAR(30) NOT NULL DEFAULT '',
  default_address TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  KEY idx_user_address_user (user_id),
  CONSTRAINT fk_user_address_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT='user addresses';

CREATE TABLE IF NOT EXISTS service_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(300) NOT NULL,
  price_label VARCHAR(50) NOT NULL,
  slug VARCHAR(100) NOT NULL UNIQUE,
  service_duration VARCHAR(100) NOT NULL DEFAULT '',
  service_area VARCHAR(255) NOT NULL DEFAULT '',
  service_scene VARCHAR(255) NOT NULL DEFAULT '',
  extra_services VARCHAR(255) NOT NULL DEFAULT '',
  image_url VARCHAR(500) NOT NULL DEFAULT '',
  enabled TINYINT(1) NOT NULL DEFAULT 1
) COMMENT='service categories';

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
  service_cases VARCHAR(500) NOT NULL,
  avatar_url VARCHAR(500) NOT NULL DEFAULT '',
  qualification_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED'
) COMMENT='worker profiles';

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
) COMMENT='worker applications';

CREATE TABLE IF NOT EXISTS worker_application_attachment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  application_id BIGINT NOT NULL,
  file_name VARCHAR(150) NOT NULL,
  file_url VARCHAR(500) NOT NULL,
  file_size BIGINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  KEY idx_worker_application_attachment_app (application_id),
  CONSTRAINT fk_worker_application_attachment_app FOREIGN KEY (application_id) REFERENCES worker_application(id)
) COMMENT='worker application attachments';

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
  payable_amount INT NOT NULL DEFAULT 0,
  payment_status VARCHAR(30) NOT NULL DEFAULT 'UNPAID',
  payment_method VARCHAR(30) NOT NULL DEFAULT '',
  paid_at DATETIME NULL,
  CONSTRAINT fk_booking_order_worker FOREIGN KEY (worker_id) REFERENCES worker_profile(id)
) COMMENT='booking orders';

CREATE TABLE IF NOT EXISTS booking_order_payment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  amount INT NOT NULL DEFAULT 0,
  payment_method VARCHAR(30) NOT NULL DEFAULT '',
  payment_status VARCHAR(30) NOT NULL DEFAULT 'UNPAID',
  payment_no VARCHAR(64) NOT NULL,
  created_at DATETIME NOT NULL,
  paid_at DATETIME NULL,
  KEY idx_order_payment_order (order_id),
  KEY idx_order_payment_user (user_id),
  KEY idx_order_payment_status (payment_status),
  UNIQUE KEY uk_order_payment_no (payment_no),
  CONSTRAINT fk_order_payment_order FOREIGN KEY (order_id) REFERENCES booking_order(id),
  CONSTRAINT fk_order_payment_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT='order payments';

CREATE TABLE IF NOT EXISTS booking_order_progress (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  progress_status VARCHAR(30) NOT NULL,
  progress_note VARCHAR(255) NOT NULL,
  CONSTRAINT fk_order_progress_order FOREIGN KEY (order_id) REFERENCES booking_order(id)
) COMMENT='order progress';

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
) COMMENT='order reviews';

CREATE TABLE IF NOT EXISTS booking_order_service_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  worker_id BIGINT NOT NULL,
  stage VARCHAR(30) NOT NULL,
  description VARCHAR(255) NOT NULL,
  created_at DATETIME NOT NULL,
  KEY idx_service_record_order (order_id),
  KEY idx_service_record_worker (worker_id),
  CONSTRAINT fk_service_record_order FOREIGN KEY (order_id) REFERENCES booking_order(id),
  CONSTRAINT fk_service_record_worker FOREIGN KEY (worker_id) REFERENCES worker_profile(id)
) COMMENT='order service records';

CREATE TABLE IF NOT EXISTS booking_order_service_record_attachment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  service_record_id BIGINT NOT NULL,
  file_name VARCHAR(120) NOT NULL,
  file_url VARCHAR(500) NOT NULL,
  created_at DATETIME NOT NULL,
  KEY idx_service_record_attachment_record (service_record_id),
  CONSTRAINT fk_service_record_attachment_record FOREIGN KEY (service_record_id) REFERENCES booking_order_service_record(id)
) COMMENT='service record attachments';

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
) COMMENT='after sale tickets';

CREATE TABLE IF NOT EXISTS order_after_sale_attachment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  after_sale_id BIGINT NOT NULL,
  file_name VARCHAR(120) NOT NULL,
  file_url VARCHAR(500) NOT NULL,
  created_at DATETIME NOT NULL,
  KEY idx_after_sale_attachment_after_sale (after_sale_id),
  CONSTRAINT fk_after_sale_attachment_after_sale FOREIGN KEY (after_sale_id) REFERENCES order_after_sale(id)
) COMMENT='after sale attachments';

CREATE TABLE IF NOT EXISTS favorite_worker (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  worker_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  UNIQUE KEY uk_favorite_worker (user_id, worker_id),
  KEY idx_favorite_worker_user (user_id),
  KEY idx_favorite_worker_worker (worker_id)
) COMMENT='favorite workers';

CREATE TABLE IF NOT EXISTS user_notification (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  recipient_user_id BIGINT NOT NULL,
  recipient_role_code VARCHAR(50) NOT NULL DEFAULT '',
  type VARCHAR(50) NOT NULL DEFAULT '',
  title VARCHAR(120) NOT NULL DEFAULT '',
  content VARCHAR(500) NOT NULL DEFAULT '',
  related_type VARCHAR(50) NOT NULL DEFAULT '',
  related_id BIGINT NULL,
  action_path VARCHAR(255) NOT NULL DEFAULT '',
  read_flag TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  read_at DATETIME NULL,
  KEY idx_user_notification_recipient (recipient_user_id, recipient_role_code),
  KEY idx_user_notification_read_flag (read_flag),
  KEY idx_user_notification_created_at (created_at)
) COMMENT='site notifications';

CREATE TABLE IF NOT EXISTS order_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  sender_user_id BIGINT NOT NULL,
  sender_role_code VARCHAR(50) NOT NULL DEFAULT '',
  sender_name VARCHAR(50) NOT NULL DEFAULT '',
  content VARCHAR(500) NOT NULL DEFAULT '',
  created_at DATETIME NOT NULL,
  KEY idx_order_message_order (order_id),
  KEY idx_order_message_created_at (created_at),
  CONSTRAINT fk_order_message_order FOREIGN KEY (order_id) REFERENCES booking_order(id)
) COMMENT='order messages';

CREATE TABLE IF NOT EXISTS operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator_user_id BIGINT NULL,
  operator_name VARCHAR(50) NOT NULL DEFAULT '',
  role_code VARCHAR(50) NOT NULL DEFAULT '',
  action_type VARCHAR(80) NOT NULL,
  target_type VARCHAR(80) NOT NULL DEFAULT '',
  target_id BIGINT NULL,
  content VARCHAR(500) NOT NULL DEFAULT '',
  ip_address VARCHAR(64) NOT NULL DEFAULT '',
  created_at DATETIME NOT NULL,
  KEY idx_operation_log_created_at (created_at),
  KEY idx_operation_log_action_type (action_type),
  KEY idx_operation_log_role_code (role_code)
) COMMENT='operation logs';
