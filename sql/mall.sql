-- ----------------------------
-- Mall module tables (UTF-8)
-- Use ry_mall_complete.sql for full init
-- ----------------------------

-- =============================================================================
-- 商城业务模块
-- =============================================================================

drop table if exists mall_category;
create table mall_category (
  category_id     bigint(20)      not null auto_increment    comment '分类ID',
  parent_id       bigint(20)      default 0                  comment '父分类ID',
  category_name   varchar(64)     not null                   comment '分类名称',
  order_num       int(4)          default 0                  comment '显示顺序',
  status          char(1)         default '0'                comment '状态（0正常 1停用）',
  del_flag        char(1)         default '0'                comment '删除标志（0存在 2删除）',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (category_id)
) engine=innodb auto_increment=1 comment = '商品分类表';

drop table if exists mall_product;
create table mall_product (
  product_id      bigint(20)      not null auto_increment    comment '商品ID',
  category_id     bigint(20)      default 0                  comment '分类ID',
  product_name    varchar(128)    not null                   comment '商品名称',
  product_sn      varchar(64)     default ''                 comment '商品货号',
  pic             varchar(512)    default ''                 comment '主图URL',
  price           decimal(10,2)   default 0.00               comment '销售价格',
  stock           int(11)         default 0                  comment '库存',
  sale_count      int(11)         default 0                  comment '销量',
  status          char(1)         default '0'                comment '状态（0上架 1下架）',
  description     text                                       comment '商品描述',
  del_flag        char(1)         default '0'                comment '删除标志（0存在 2删除）',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (product_id)
) engine=innodb auto_increment=1 comment = '商品表';

drop table if exists mall_order;
create table mall_order (
  order_id          bigint(20)      not null auto_increment    comment '订单ID',
  order_sn          varchar(32)     not null                   comment '订单编号',
  user_id           bigint(20)      default 0                  comment '用户ID',
  user_name         varchar(64)     default ''                 comment '用户账号',
  total_amount      decimal(10,2)   default 0.00               comment '订单总额',
  pay_amount        decimal(10,2)   default 0.00               comment '应付金额',
  status            char(1)         default '0'                comment '订单状态（0待付款 1已付款 2已发货 3已完成 4已取消）',
  receiver_name     varchar(64)     default ''                 comment '收货人',
  receiver_phone    varchar(20)     default ''                 comment '收货电话',
  receiver_address  varchar(255)    default ''                 comment '收货地址',
  pay_time          datetime        default null               comment '支付时间',
  delivery_time     datetime        default null               comment '发货时间',
  finish_time       datetime        default null               comment '完成时间',
  cancel_time       datetime        default null               comment '取消时间',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (order_id),
  unique key uk_order_sn (order_sn)
) engine=innodb auto_increment=1 comment = '订单表';

drop table if exists mall_order_item;
create table mall_order_item (
  item_id         bigint(20)      not null auto_increment    comment '明细ID',
  order_id        bigint(20)      not null                   comment '订单ID',
  product_id      bigint(20)      default 0                  comment '商品ID',
  product_name    varchar(128)    default ''                 comment '商品名称',
  product_pic     varchar(512)    default ''                 comment '商品图片',
  product_price   decimal(10,2)   default 0.00               comment '商品单价',
  quantity        int(11)         default 1                  comment '购买数量',
  total_amount    decimal(10,2)   default 0.00               comment '小计金额',
  primary key (item_id),
  key idx_order_id (order_id)
) engine=innodb auto_increment=1 comment = '订单明细表';

drop table if exists mall_cart;
create table mall_cart (
  cart_id       bigint(20)    not null auto_increment  comment '购物车ID',
  user_id       bigint(20)    not null                 comment '用户ID',
  product_id    bigint(20)    not null                 comment '商品ID',
  quantity      int(11)       default 1                comment '数量',
  checked       char(1)       default '1'              comment '是否选中（1是 0否）',
  create_time   datetime                               comment '创建时间',
  update_time   datetime                               comment '更新时间',
  primary key (cart_id),
  unique key uk_user_product (user_id, product_id)
) engine=innodb auto_increment=1 comment = '购物车表';

insert into mall_category values(1, 0, '数码家电', 1, '0', '0', 'admin', sysdate(), '', null, '数码家电分类');
insert into mall_category values(2, 0, '服饰鞋包', 2, '0', '0', 'admin', sysdate(), '', null, '服饰鞋包分类');
insert into mall_category values(3, 1, '手机通讯', 1, '0', '0', 'admin', sysdate(), '', null, null);

insert into mall_product values(1, 3, '示例智能手机', 'SPU001', '', 2999.00, 100, 0, '0', '示例商品，可在后台替换', '0', 'admin', sysdate(), '', null, null);
insert into mall_product values(2, 2, '示例休闲T恤', 'SPU002', '', 99.00, 500, 12, '0', '纯棉休闲T恤', '0', 'admin', sysdate(), '', null, null);

insert into mall_order values(1, '202606040001', 2, 'ry', 2999.00, 2999.00, '1', '张三', '13800138000', '北京市朝阳区示例路1号', sysdate(), null, null, null, 'admin', sysdate(), '', null, '示例订单');
insert into mall_order_item values(1, 1, 1, '示例智能手机', '', 2999.00, 1, 2999.00);

insert into sys_dict_type values(11, '商品上架状态', 'mall_product_status', '0', 'admin', sysdate(), '', null, '商城商品上架状态');
insert into sys_dict_type values(12, '商城订单状态', 'mall_order_status', '0', 'admin', sysdate(), '', null, '商城订单状态');

insert into sys_dict_data values(30, 1, '上架', '0', 'mall_product_status', '', 'success', 'Y', '0', 'admin', sysdate(), '', null, '商品上架');
insert into sys_dict_data values(31, 2, '下架', '1', 'mall_product_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '商品下架');
insert into sys_dict_data values(32, 1, '待付款', '0', 'mall_order_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(33, 2, '已付款', '1', 'mall_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(34, 3, '已发货', '2', 'mall_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(35, 4, '已完成', '3', 'mall_order_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, null);
insert into sys_dict_data values(36, 5, '已取消', '4', 'mall_order_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, null);

insert into sys_menu values('3000', '商城管理', '0', '5', 'mall', null, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), '', null, '商城管理目录');
insert into sys_menu values('3001', '商品分类', '3000', '1', 'category', 'mall/category/index', '', '', 1, 0, 'C', '0', '0', 'mall:category:list', 'tree', 'admin', sysdate(), '', null, '商品分类菜单');
insert into sys_menu values('3002', '商品管理', '3000', '2', 'product', 'mall/product/index', '', '', 1, 0, 'C', '0', '0', 'mall:product:list', 'goods', 'admin', sysdate(), '', null, '商品管理菜单');
insert into sys_menu values('3003', '订单管理', '3000', '3', 'order', 'mall/order/index', '', '', 1, 0, 'C', '0', '0', 'mall:order:list', 'list', 'admin', sysdate(), '', null, '订单管理菜单');

insert into sys_menu values('3101', '分类查询', '3001', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:category:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3102', '分类新增', '3001', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:category:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3103', '分类修改', '3001', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:category:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3104', '分类删除', '3001', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:category:remove', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3111', '商品查询', '3002', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:product:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3112', '商品新增', '3002', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:product:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3113', '商品修改', '3002', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:product:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3114', '商品删除', '3002', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:product:remove', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3121', '订单查询', '3003', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:order:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3122', '订单发货', '3003', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:order:ship', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3123', '订单完成', '3003', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:order:finish', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3124', '订单取消', '3003', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:order:cancel', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3125', '订单删除', '3003', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'mall:order:remove', '#', 'admin', sysdate(), '', null, '');

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

SET FOREIGN_KEY_CHECKS = 1;
