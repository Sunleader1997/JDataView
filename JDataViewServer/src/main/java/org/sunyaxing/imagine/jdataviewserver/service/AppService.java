package org.sunyaxing.imagine.jdataviewserver.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.sunyaxing.imagine.jdataviewserver.entity.AppEntity;
import org.sunyaxing.imagine.jdataviewserver.service.repository.AppRepository;

@Service
public class AppService extends ServiceImpl<AppRepository, AppEntity> {

}