package org.sunyaxing.imagine.jdataviewserver.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.sunyaxing.imagine.jdataviewserver.entity.AppEntity;
import org.sunyaxing.imagine.jdataviewserver.service.repository.AppRepository;

@Service
public class AppService extends ServiceImpl<AppRepository, AppEntity> {
    public static final String PREFIX = "APP-";

    /**
     * 根据消息创建APP
     */
    public void insertByAgentMsg(String host, Long pid, String appName) {
        // app 是否存在
        boolean appExists = this.lambdaQuery()
                .eq(AppEntity::getHost, host)
                .eq(AppEntity::getPid, pid)
                .exists();
        AppEntity entity = AppEntity.builder()
                .pid(pid)
                .host(host)
                .name(appName)
                .build();
        if (!appExists) {
            this.save(entity);
        }else {
            this.lambdaUpdate()
                    .set(AppEntity::getName, appName)
                    .eq(AppEntity::getHost, host)
                    .eq(AppEntity::getPid, pid)
                    .update();
        }
    }
}