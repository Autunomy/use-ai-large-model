use use_ai_large_model;

drop table if exists openai_chat_model;
create table openai_chat_model(
    id int primary key auto_increment,
    name varchar(100) not null comment '模型名称',
    max_tokens int not null comment '模型能考虑的最大上下文大小',
    status int not null default '0' comment '模型状态,0表示目前可使用，1表示目前模型已过期',
    input_price double not null comment '模型输入价格，单位:美元',
    output_price double not null comment '模型输出价格，单位:美元'
) comment '聊天模型表';

drop table if exists openai_chat_history_message;
create table openai_chat_history_message(
    id int primary key auto_increment,
    message_card varchar(100) comment '消息编号',
    role varchar(20) not null comment '角色名称',
    content text not null comment '内容',
    create_time datetime default CURRENT_TIMESTAMP comment '创建日期',
    tokens int not null comment 'token消耗',
    chat_window_id int  not null comment '聊天窗口id，方便查询同一个聊天窗口的全部消息'
)comment '聊天消息列表';

drop table if exists openai_chat_window;
create table openai_chat_window(
    id int primary key auto_increment,
    user_id int not null comment '用户',
    window_id varchar(100) not null comment '窗口id',
    model_id int not null comment '使用的模型',
    create_time datetime not null default CURRENT_TIMESTAMP comment '创建日期',
    title varchar(100) not null default '新聊天' comment '窗口名称,在创建窗口的时候根据第一条消息来进行截取来生成标题，用户可以自定义',
    is_title_gen int not null default 0 comment '标题是否被AI自动生成，0-否 1-是',
    is_delete int not null default 0 comment '0-未删除 1-删除'
) comment '聊天窗口';

drop table if exists user;
create table user(
    id int primary key auto_increment,
    email varchar(100) comment '用户邮箱',
    phone_num varchar(100) comment '手机号',
    password varchar(100) comment '密码',
    bill_last int not null default 0 comment '账户剩余额度,token数量，可以为负数',
    create_time datetime default CURRENT_TIMESTAMP comment '注册时间'
)comment '用户表';

drop table if exists top_up_order;
create table top_up_order(
    id int primary key auto_increment,
    create_time datetime default CURRENT_TIMESTAMP comment '充值时间',
    count_dollar double not null comment '充值金额，单位:美元',
    count_rmb double not null comment '充值金额，单位:人民币',
    order_id varchar(100) not null comment '订单id',
    user_id int not null comment '用户id'
) comment '充值记录表';