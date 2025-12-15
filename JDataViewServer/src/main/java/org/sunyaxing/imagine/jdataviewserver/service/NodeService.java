package org.sunyaxing.imagine.jdataviewserver.service;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.sunyaxing.imagine.jdataviewapi.data.ClassRegistryMsg;
import org.sunyaxing.imagine.jdataviewapi.data.JDataViewMsg;
import org.sunyaxing.imagine.jdataviewserver.entity.NodeEntity;
import org.sunyaxing.imagine.jdataviewserver.entity.cover.EntityCover;
import org.sunyaxing.imagine.jdataviewserver.service.repository.NodeRepository;

import java.util.List;

@Service
public class NodeService extends ServiceImpl<NodeRepository, NodeEntity> {
    public static final String PREFIX = "NODE-";

    public static List<NodeEntity> parseClassRegistryMsg(JDataViewMsg agentMsg) {
        return agentMsg.getContent().stream().map(strData -> {
            ClassRegistryMsg classRegistryMsg = JSONObject.parseObject(strData, ClassRegistryMsg.class);
            NodeEntity entity = EntityCover.INSTANCE.msgToEntity(classRegistryMsg);
            entity.setId(PREFIX + IdUtil.getSnowflakeNextIdStr());
            return entity;
        }).toList();
    }
}