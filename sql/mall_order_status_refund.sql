-- 商城订单状态：增加「已退款」（执行一次即可；若 dict_code 37 已存在请跳过）
INSERT INTO sys_dict_data VALUES(37, 6, '已退款', '5', 'mall_order_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '商城订单已退款');
