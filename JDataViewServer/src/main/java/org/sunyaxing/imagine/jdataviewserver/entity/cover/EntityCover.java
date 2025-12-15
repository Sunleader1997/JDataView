package org.sunyaxing.imagine.jdataviewserver.entity.cover;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.sunyaxing.imagine.jdataviewapi.data.ClassRegistryMsg;
import org.sunyaxing.imagine.jdataviewapi.data.ThreadSpace;
import org.sunyaxing.imagine.jdataviewserver.entity.AgentMsgEntity;
import org.sunyaxing.imagine.jdataviewserver.entity.NodeEntity;

@Mapper(uses = {CommonCover.class})
public interface EntityCover {
    EntityCover INSTANCE = Mappers.getMapper(EntityCover.class);

    @Mappings({
            @Mapping(source = "threadId", target = "threadId"),
    })
    AgentMsgEntity msgToEntity(ThreadSpace threadSpace);

    @Mappings({
            @Mapping(source = "className", target = "name"),
            @Mapping(source = "className", target = "nodeType"),
            @Mapping(source = "methodNames", target = "handles", qualifiedByName = "arrayToString"),
    })
    NodeEntity msgToEntity(ClassRegistryMsg classRegistryMsg);
}
