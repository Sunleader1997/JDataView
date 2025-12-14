package org.sunyaxing.imagine.jdataviewserver.service.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.springframework.stereotype.Repository;
import org.sunyaxing.imagine.jdataviewserver.entity.NodeEntity;

@Repository
@CacheNamespace
public interface NodeRepository extends BaseMapper<NodeEntity> {
}
