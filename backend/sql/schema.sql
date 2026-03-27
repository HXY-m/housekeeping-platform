CREATE DATABASE IF NOT EXISTS housekeeping_platform
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE housekeeping_platform;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(20) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL,
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
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
  name VARCHAR(100) NOT NULL COMMENT '分类名称',
  description VARCHAR(300) NOT NULL COMMENT '分类描述',
  price_label VARCHAR(50) NOT NULL COMMENT '价格展示文本',
  slug VARCHAR(100) NOT NULL UNIQUE COMMENT '分类标识'
) COMMENT='服务分类表';

CREATE TABLE IF NOT EXISTS worker_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '服务人员ID',
  name VARCHAR(50) NOT NULL COMMENT '姓名',
  role_label VARCHAR(50) NOT NULL COMMENT '角色标签',
  rating DECIMAL(3,2) NOT NULL DEFAULT 5.00 COMMENT '评分',
  completed_orders INT NOT NULL DEFAULT 0 COMMENT '完成订单数',
  hourly_price INT NOT NULL COMMENT '时薪',
  city VARCHAR(50) NOT NULL COMMENT '城市',
  intro VARCHAR(500) NOT NULL COMMENT '简介',
  tags VARCHAR(500) NOT NULL COMMENT '技能标签，逗号分隔',
  next_available VARCHAR(100) NOT NULL COMMENT '最近可约时间',
  years_of_experience INT NOT NULL DEFAULT 0 COMMENT '从业年限',
  certificates VARCHAR(500) NOT NULL COMMENT '证书，逗号分隔',
  service_areas VARCHAR(500) NOT NULL COMMENT '服务区域，逗号分隔',
  service_cases VARCHAR(500) NOT NULL COMMENT '服务案例，逗号分隔'
) COMMENT='服务人员表';

CREATE TABLE IF NOT EXISTS booking_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
  service_name VARCHAR(100) NOT NULL COMMENT '服务项目名称',
  worker_id BIGINT NOT NULL COMMENT '服务人员ID',
  customer_name VARCHAR(50) NOT NULL COMMENT '联系人',
  contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
  service_address VARCHAR(255) NOT NULL COMMENT '服务地址',
  booking_date VARCHAR(20) NOT NULL COMMENT '预约日期',
  booking_slot VARCHAR(50) NOT NULL COMMENT '预约时段',
  status VARCHAR(30) NOT NULL COMMENT '订单状态',
  progress_note VARCHAR(255) NOT NULL COMMENT '当前进度说明',
  remark VARCHAR(500) NOT NULL COMMENT '需求备注',
  CONSTRAINT fk_booking_order_worker FOREIGN KEY (worker_id) REFERENCES worker_profile(id)
) COMMENT='预约订单表';

CREATE TABLE IF NOT EXISTS booking_order_progress (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '进度ID',
  order_id BIGINT NOT NULL COMMENT '订单ID',
  progress_status VARCHAR(30) NOT NULL COMMENT '进度状态',
  progress_note VARCHAR(255) NOT NULL COMMENT '进度说明',
  CONSTRAINT fk_order_progress_order FOREIGN KEY (order_id) REFERENCES booking_order(id)
) COMMENT='订单进度表';

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
  password VARCHAR(255) NOT NULL COMMENT '密码哈希',
  real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态'
) COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
  role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
  role_name VARCHAR(50) NOT NULL COMMENT '角色名称'
) COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  UNIQUE KEY uk_user_role (user_id, role_id)
) COMMENT='用户角色关联表';
