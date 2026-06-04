-- 若字典显示英文或问号，在 MySQL 中执行（库名按实际修改）
USE `ry-vue`;

UPDATE sys_dict_type SET dict_name = '商品上架状态', remark = '商城商品上架状态' WHERE dict_type = 'mall_product_status';
UPDATE sys_dict_type SET dict_name = '商城订单状态', remark = '商城订单状态' WHERE dict_type = 'mall_order_status';

UPDATE sys_dict_data SET dict_label = '上架', remark = '商品上架' WHERE dict_code = 30;
UPDATE sys_dict_data SET dict_label = '下架', remark = '商品下架' WHERE dict_code = 31;
UPDATE sys_dict_data SET dict_label = '待付款' WHERE dict_code = 32;
UPDATE sys_dict_data SET dict_label = '已付款' WHERE dict_code = 33;
UPDATE sys_dict_data SET dict_label = '已发货' WHERE dict_code = 34;
UPDATE sys_dict_data SET dict_label = '已完成' WHERE dict_code = 35;
UPDATE sys_dict_data SET dict_label = '已取消' WHERE dict_code = 36;
