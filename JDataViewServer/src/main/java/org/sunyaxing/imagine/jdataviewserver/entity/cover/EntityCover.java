package org.sunyaxing.imagine.jdataviewserver.entity.cover;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.sunyaxing.imagine.jdataviewapi.data.ThreadSpace;
import org.sunyaxing.imagine.jdataviewserver.entity.AgentMsgEntity;

@Mapper(uses = {CommonCover.class})
public interface EntityCover {
    EntityCover INSTANCE = Mappers.getMapper(EntityCover.class);

    @Mappings({
            @Mapping(source = "threadId", target = "threadId"),
    })
    AgentMsgEntity msgToEntity(ThreadSpace threadSpace);
}
