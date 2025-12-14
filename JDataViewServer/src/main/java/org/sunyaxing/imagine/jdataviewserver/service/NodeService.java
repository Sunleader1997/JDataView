package org.sunyaxing.imagine.jdataviewserver.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.sunyaxing.imagine.jdataviewserver.entity.NodeEntity;
import org.sunyaxing.imagine.jdataviewserver.service.repository.NodeRepository;

@Service
public class NodeService extends ServiceImpl<NodeRepository, NodeEntity> {

}