-- ----------------------------
-- Shopping cart (run after mall.sql)
-- ----------------------------

drop table if exists mall_cart;
create table mall_cart (
  cart_id       bigint(20)    not null auto_increment  comment 'cart id',
  user_id       bigint(20)    not null                 comment 'user id',
  product_id    bigint(20)    not null                 comment 'product id',
  quantity      int(11)       default 1                comment 'quantity',
  checked       char(1)       default '1'              comment 'checked 1 yes 0 no',
  create_time   datetime                               comment 'create time',
  update_time   datetime                               comment 'update time',
  primary key (cart_id),
  unique key uk_user_product (user_id, product_id)
) engine=innodb auto_increment=1 comment = 'shopping cart';
