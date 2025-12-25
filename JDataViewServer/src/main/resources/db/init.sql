-- AGENT 投递的消息内容
DROP TABLE IF EXISTS AGENT_MSG;
create table AGENT_MSG
(
    ID                CHARACTER VARYING NOT NULL,
    APP_NAME          CHARACTER VARYING NOT NULL,
    PID               BIGINT            NOT NULL,
    THREAD_ID         BIGINT            NOT NULL,
    THREAD_NAME       VARCHAR(255),               -- 线程名通常不会太长，VARCHAR(255) 足够
    STEP_INDEX        BIGINT DEFAULT 0,           -- ATOMICLONG 通常表示计数，用 BIGINT 存储其值，这里假设初始值为 0
    DEPTH             BIGINT DEFAULT 0,           -- 深度，用 BIGINT 存储其值，初始值为 0
    CLASS_NAME        VARCHAR(500),               -- 类名可能较长，可根据实际情况调整长度
    METHOD_NAME       VARCHAR(500),               -- 方法名可能较长，可根据实际情况调整长度
    METHOD_START_TIME BIGINT            NOT NULL, -- 时间戳通常用 BIGINT 存储
    METHOD_STATE      VARCHAR(50),                -- 枚举值，假设 METHODSTATE 枚举的字符串表示不会超过 50 个字符
    METHOD_END_TIME   BIGINT,
    constraint AGENT_MSG_PK
        primary key (ID)                          -- 结束时间可能为空（如果方法还在执行）
);
create index AGENT_MSG_FOR_SELECT
    on AGENT_MSG (APP_NAME, THREAD_ID, CLASS_NAME, METHOD_NAME, METHOD_START_TIME);
