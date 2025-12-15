package org.sunyaxing.imagine.jdataviewserver.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.sunyaxing.imagine.jdataviewapi.data.JDataViewMsg;
import org.sunyaxing.imagine.jdataviewapi.data.ThreadSpace;
import org.sunyaxing.imagine.jdataviewserver.entity.AgentMsgEntity;
import org.sunyaxing.imagine.jdataviewserver.entity.cover.EntityCover;
import org.sunyaxing.imagine.jdataviewserver.service.repository.AgentMsgRepository;

import java.util.List;

@Service
public class AgentMsgService extends ServiceImpl<AgentMsgRepository, AgentMsgEntity> {
    public static final String PREFIX = "MSG-";

    public static List<AgentMsgEntity> parseMsg(JDataViewMsg<ThreadSpace> agentMsg) {
        return agentMsg.getContent().stream().map(threadSpace -> {
            AgentMsgEntity entity = EntityCover.INSTANCE.msgToEntity(threadSpace);
            entity.setId(PREFIX + IdUtil.getSnowflakeNextIdStr());
            entity.setAppName(agentMsg.getAppName());
            entity.setPid(agentMsg.getPid());
            return entity;
        }).toList();
    }
}