USE housekeeping_platform;

SET @legacy_worker_a := (SELECT id FROM sys_user WHERE phone = '13800000023' LIMIT 1);
SET @legacy_worker_b := (SELECT id FROM sys_user WHERE phone = '13800000024' LIMIT 1);
SET @user_0044 := (SELECT id FROM sys_user WHERE phone = '13800000044' LIMIT 1);
SET @user_0045 := (SELECT id FROM sys_user WHERE phone = '13800000045' LIMIT 1);
SET @worker_0055_user := (SELECT id FROM sys_user WHERE phone = '13800000055' LIMIT 1);
SET @worker_0066_user := (SELECT id FROM sys_user WHERE phone = '13800000066' LIMIT 1);
SET @demo_admin := (SELECT id FROM sys_user WHERE phone = '13800000033' LIMIT 1);
SET @demo_worker_user := (SELECT id FROM sys_user WHERE phone = '13800000022' LIMIT 1);

DELETE FROM order_after_sale_attachment
WHERE after_sale_id IN (
  SELECT id FROM (
    SELECT id
    FROM order_after_sale
    WHERE order_id IN (
      SELECT id FROM (
        SELECT id FROM booking_order WHERE remark LIKE '[DEMO_%]%'
      ) demo_orders
    )
  ) demo_after_sales
);

DELETE FROM order_after_sale
WHERE order_id IN (
  SELECT id FROM (
    SELECT id FROM booking_order WHERE remark LIKE '[DEMO_%]%'
  ) demo_orders
);

DELETE FROM booking_order_service_record_attachment
WHERE service_record_id IN (
  SELECT id FROM (
    SELECT id
    FROM booking_order_service_record
    WHERE order_id IN (
      SELECT id FROM (
        SELECT id FROM booking_order WHERE remark LIKE '[DEMO_%]%'
      ) demo_orders
    )
  ) demo_records
);

DELETE FROM booking_order_service_record
WHERE order_id IN (
  SELECT id FROM (
    SELECT id FROM booking_order WHERE remark LIKE '[DEMO_%]%'
  ) demo_orders
);

DELETE FROM order_review
WHERE order_id IN (
  SELECT id FROM (
    SELECT id FROM booking_order WHERE remark LIKE '[DEMO_%]%'
  ) demo_orders
);

DELETE FROM booking_order_progress
WHERE order_id IN (
  SELECT id FROM (
    SELECT id FROM booking_order WHERE remark LIKE '[DEMO_%]%'
  ) demo_orders
);

DELETE FROM booking_order_payment
WHERE order_id IN (
  SELECT id FROM (
    SELECT id FROM booking_order WHERE remark LIKE '[DEMO_%]%'
  ) demo_orders
);

DELETE FROM order_message
WHERE order_id IN (
  SELECT id FROM (
    SELECT id FROM booking_order WHERE remark LIKE '[DEMO_%]%'
  ) demo_orders
);

DELETE FROM user_notification
WHERE related_type IN ('ORDER', 'AFTER_SALE')
  AND related_id IN (
    SELECT id FROM (
      SELECT id FROM booking_order WHERE remark LIKE '[DEMO_%]%'
    ) demo_orders
  );

DELETE FROM booking_order
WHERE remark LIKE '[DEMO_%]%';

DELETE FROM user_notification
WHERE recipient_user_id IN (@user_0044, @user_0045, @worker_0055_user, @worker_0066_user)
   OR (recipient_user_id = @demo_admin AND type = 'AFTER_SALE')
   OR (recipient_user_id = @demo_worker_user AND type = 'ORDER_MESSAGE')
   OR (recipient_user_id IN (@worker_0055_user, @worker_0066_user) AND type = 'WORKER_APPLICATION');

DELETE FROM worker_application_attachment
WHERE application_id IN (
  SELECT id FROM (
    SELECT id
    FROM worker_application
    WHERE user_id IN (@demo_worker_user, @worker_0055_user, @worker_0066_user)
  ) demo_applications
);

DELETE FROM worker_application
WHERE user_id IN (@demo_worker_user, @worker_0055_user, @worker_0066_user);

DELETE FROM favorite_worker
WHERE user_id IN (@user_0044, @user_0045);

DELETE FROM worker_profile
WHERE user_id IN (@legacy_worker_a, @legacy_worker_b, @worker_0055_user, @worker_0066_user);

DELETE FROM worker_profile
WHERE user_id IS NULL
  AND name IN ('王师傅', '周阿姨');

DELETE FROM user_address
WHERE user_id IN (@legacy_worker_a, @legacy_worker_b, @user_0044, @user_0045, @worker_0055_user, @worker_0066_user);

DELETE FROM user_profile
WHERE user_id IN (@legacy_worker_a, @legacy_worker_b, @user_0044, @user_0045, @worker_0055_user, @worker_0066_user);

DELETE FROM sys_user_role
WHERE user_id IN (@legacy_worker_a, @legacy_worker_b, @user_0044, @user_0045, @worker_0055_user, @worker_0066_user);

DELETE FROM sys_user
WHERE phone IN ('13800000023', '13800000024', '13800000044', '13800000045', '13800000055', '13800000066');
