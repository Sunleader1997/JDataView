package org.sunyaxing.imagine.jdataviewserver.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.sunyaxing.imagine.jdataviewapi.data.JDataViewMsg;
import org.sunyaxing.imagine.jdataviewserver.entity.AppEntity;
import org.sunyaxing.imagine.jdataviewserver.service.repository.AppRepository;

@Service
public class AppService extends ServiceImpl<AppRepository, AppEntity> {
    public static final String PREFIX = "APP-";

    /**
     * 根据消息创建APP
     *
     * @param jDataViewMsg
     */
    public void insertByAgentMsg(JDataViewMsg<?> jDataViewMsg) {
        // app 是否存在
        boolean appExists = this.lambdaQuery()
                .eq(AppEntity::getName, jDataViewMsg.getAppName())
                .exists();
        if (!appExists) {
            AppEntity entity = AppEntity.builder()
                    .id(PREFIX + IdUtil.getSnowflakeNextIdStr())
                    .name(jDataViewMsg.getAppName())
                    .description("")
                    .build();
            this.save(entity);
        }
    }
}