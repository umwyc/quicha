# 数据库初始化

-- 创建库
create database if not exists quicha;

-- 切换库
use quicha;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 题目表
create table if not exists question
(
    id           bigint auto_increment comment 'id' primary key,
    title        varchar(512)                       null comment '标题',
    content      text                               null comment '内容',
    tags         varchar(1024)                      null comment '标签列表（json 数组）',
    answer       text                               null comment '题目答案',
    submitNum    int      default 0                 not null comment '提交次数',
    acceptedNum  int      default 0                 not null comment '通过次数',
    judgeCase    text                               null comment '样例',
    judgeConfig  text                               null comment '题目配置',
    thumbNum     int      default 0                 not null comment '点赞数',
    favourNum    int      default 0                 not null comment '收藏数',
    userId       bigint                             not null comment '创建用户id',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '题目表' collate = utf8mb4_unicode_ci;

-- 题目提交表
create table if not exists question_submit
(
    id         bigint auto_increment comment 'id' primary key,
    language   varchar(128)                       not null comment '使用语言',
    code       text                               not null comment '用户代码',
    judgeInfo  text                               null comment '判题信息',
    status     int  default 0                     not null comment '0-未判题  1-判题中  2-成功  3-失败',
    questionId bigint                                not null comment '题目id',
    userId     bigint                             not null comment '提交用户id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_questionId (questionId),
    index idx_userId (userId)
) comment '题目提交列表';
