package org.sunyaxing.imagine.jdataviewserver.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunyaxing.imagine.jdataviewapi.data.JDataViewMsg;
import org.sunyaxing.imagine.jdataviewapi.data.ThreadSpace;
import org.sunyaxing.imagine.jdataviewserver.entity.AgentMsgEntity;
import org.sunyaxing.imagine.jdataviewserver.service.repository.AgentMsgRepository;

@Service
public class AgentMsgService extends ServiceImpl<AgentMsgRepository, AgentMsgEntity> {

    @Autowired
    private AppService appService;

    public void handleMsg(JDataViewMsg<ThreadSpace> agentMsg) {
        // 根据 agentMsg 创建应用
        appService.insertByAgentMsg(agentMsg);
    }
}