# JDataView

#### 介绍

LINUX JAVA 可视化性能分析工具

#### 软件架构

软件架构说明

1. [JDataViewAgent](JDataViewAgent) 用于采集数据,通过websocket将数据发送给[JDataViewServer](JDataViewServer)
2. [JDataViewServer](JDataViewServer) 持久化数据，分析数据，提供可视化支撑
3. [app](app) 测试程序，用于接入[JDataViewAgent](JDataViewAgent)

#### 安装运行&使用说明

1. 将 JDataViewAll-1.0.0-noJdk.zip 解压至根目录

```SH
unzip JDataViewAll-1.0.0-noJdk.zip -d /
```

2. 启动

```SH
cd /opt/JDataView/
java -jar JDataViewServer-1.0.0.jar
```

3. 运行成功后将出现面板
   ![img](screan\1panel.png)
4. 选择右侧refresh回车，刷新应用列表
   ![img](screan\2panel.png)
5. 选择应用后回车,选择 attach
   ![img](screan\3panel.png)
6. 输入要扫描的包前缀
   ![img](screan\4panel.png)
7. 回车后等待注入完成，选择应用后回车,选择 stack
   ![img](screan\5panel.png)
8. 触发所选择服务的业务线程后，选择右侧refresh回车，刷新线程列表
   ![img](screan\6panel.png)
9. 选择对应线程列表，回车后即可查看堆栈时间
   ![img](screan\6panel.png)

#### 参与贡献

1. Fork 本仓库
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request

#### 特技

1. 使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2. Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3. 你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4. [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5. Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6. Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
