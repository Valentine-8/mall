-- ----------------------------
-- 商城菜单与权限（执行前请先执行 mall.sql）
-- ----------------------------

-- 一级菜单：商城管理
insert into sys_menu values('3000', '商城管理', '0', '5', 'mall', null, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), '', null, '商城管理目录');

-- 二级菜单
insert into sys_menu values('3001', '商品分类', '3000', '1', 'category', 'mall/category/index', '', '', 1, 0, 'C', '0', '0', 'mall:category:list', 'tree', 'admin', sysdate(), '', null, '商品分类菜单');
insert into sys_menu values('3002', '商品管理', '3000', '2', 'product', 'mall/product/index', '', '', 1, 0, 'C', '0', '0', 'mall:product:list', 'goods', 'admin', sysdate(), '', null, '商品管理菜单');
insert into sys_menu values('3003', '订单管理', '3000', '3', 'order', 'mall/order/index', '', '', 1, 0, 'C', '0', '0', 'mall:order:list', 'list', 'admin', sysdate(), '', null, '订单管理菜单');

-- 分类按钮
insert into sys_menu values('3101', '分类查询', '3001', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:category:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3102', '分类新增', '3001', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:category:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3103', '分类修改', '3001', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:category:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3104', '分类删除', '3001', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:category:remove', '#', 'admin', sysdate(), '', null, '');

-- 商品按钮
insert into sys_menu values('3111', '商品查询', '3002', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:product:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3112', '商品新增', '3002', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:product:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3113', '商品修改', '3002', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:product:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3114', '商品删除', '3002', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:product:remove', '#', 'admin', sysdate(), '', null, '');

-- 订单按钮
insert into sys_menu values('3121', '订单查询', '3003', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:order:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3122', '订单发货', '3003', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:order:ship', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3123', '订单完成', '3003', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:order:finish', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3124', '订单取消', '3003', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:order:cancel', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3125', '订单删除', '3003', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:order:remove', '#', 'admin', sysdate(), '', null, '');

-- 管理员角色授权
insert into sys_role_menu values ('1', '3000');
insert into sys_role_menu values ('1', '3001');
insert into sys_role_menu values ('1', '3002');
insert into sys_role_menu values ('1', '3003');
insert into sys_role_menu values ('1', '3101');
insert into sys_role_menu values ('1', '3102');
insert into sys_role_menu values ('1', '3103');
insert into sys_role_menu values ('1', '3104');
insert into sys_role_menu values ('1', '3111');
insert into sys_role_menu values ('1', '3112');
insert into sys_role_menu values ('1', '3113');
insert into sys_role_menu values ('1', '3114');
insert into sys_role_menu values ('1', '3121');
insert into sys_role_menu values ('1', '3122');
insert into sys_role_menu values ('1', '3123');
insert into sys_role_menu values ('1', '3124');
insert into sys_role_menu values ('1', '3125');
