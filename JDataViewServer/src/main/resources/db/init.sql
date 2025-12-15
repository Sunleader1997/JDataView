-- AGENT 投递的消息内容
create table AGENT_MSG
(
    ID      CHARACTER VARYING not null,
    APPNAME CHARACTER VARYING not null,
);

-- 项目列表
create table APP
(
    ID          CHARACTER VARYING not null,
    NAME        CHARACTER VARYING not null,
    DESCRIPTION CHARACTER VARYING,
    constraint APP_PK
        primary key (ID)
);

-- 转成的节点信息
create table NODE
(
    ID         CHARACTER VARYING           not null,
    NAME       CHARACTER VARYING           not null,
    NODETYPE   CHARACTER VARYING           not null,
    X          INTEGER           default 0 not null,
    Y          INTEGER           default 0 not null,
    PARENTNODE CHARACTER VARYING,
    EXTENT     CHARACTER VARYING,
    WIDTH      INTEGER,
    HEIGHT     INTEGER,
    HANDLES    CHARACTER VARYING default '[]',
    constraint NODE_PK
        primary key (ID)
);

-- 连线
create table EDGE
(
    ID           CHARACTER VARYING not null,
    SOURCEID     CHARACTER VARYING not null,
    TARGETID     CHARACTER VARYING not null,
    SOURCEHANDLE CHARACTER VARYING,
    TARGETHANDLE CHARACTER VARYING,
    constraint NODE_LINK_PK
        primary key (ID)
);

