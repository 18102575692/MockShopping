-- 产品信息表 --
create table t_product(
  id int(12) not null auto_increment  comment '编号',
  product_name varchar(50) not null comment '产品名称',
  stock int(10) not null comment '库存',
  price decimal(16,2) not null comment '价格',
  version int(10) not null comment '版本',
  note varchar(255) null comment '备注',
  primary key (id)
);
-- 购买信息表 --
create table t_purchase_record(
    id int(12) not null primary key  auto_increment comment '编号',
    user_id int(12) not null comment '用户信息',
    product_id int(12) not null comment '产品信息',
    price decimal(16,2) not null comment '单价',
    quantity int(12) not null comment '数量',
    sum decimal(16,2) not null comment '总价',
    purchase_date timestamp not null default now() comment '购买时间',
    note varchar(255) null comment '备注'
)