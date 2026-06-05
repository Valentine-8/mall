-- 商城订单状态：增加「已退款」（执行一次即可）
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 37, 6, '已退款', '5', 'mall_order_status', '', 'danger', 'N', '0', 'admin', sysdate(), '商城订单已退款'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM sys_dict_data WHERE dict_type = 'mall_order_status' AND dict_value = '5'
);
