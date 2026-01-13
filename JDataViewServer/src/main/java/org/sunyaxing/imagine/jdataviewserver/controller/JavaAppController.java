package org.sunyaxing.imagine.jdataviewserver.controller;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunyaxing.imagine.jdataviewserver.common.Result;
import org.sunyaxing.imagine.jdataviewserver.controller.dtos.GetMethodTreeDto;
import org.sunyaxing.imagine.jdataviewserver.controller.dtos.JavaAppDto;
import org.sunyaxing.imagine.jdataviewserver.controller.dtos.ThreadDto;
import org.sunyaxing.imagine.jdataviewserver.service.AgentMsgService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JavaAppController {

    private static final Logger log = LoggerFactory.getLogger(JavaAppController.class);
    @Autowired
    private AgentMsgService agentMsgService;

    /**
     * 记录已经启动的APP
     */
    private static final HashMap<Long, VirtualMachine> ATTACHED_VMS = new HashMap<>();

    public Result<List<JavaAppDto>> getJavaApps() {
        // 读取 JVM 列表
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        // 所有存活APP
        Map<String, JavaAppDto> aliveApps = vms.stream().map(vm -> {
            String pid = vm.id();
            String name = vm.displayName().split(" ")[0];
            JavaAppDto javaAppDto = JavaAppDto.builder().pid(Long.valueOf(pid)).appName(name).host("127.0.0.1").alive(true).build();
            javaAppDto.setHasAttached(ATTACHED_VMS.containsKey(javaAppDto.getPid()));
            return javaAppDto;
        }).collect(Collectors.toMap(JavaAppDto::getAppName, app -> app, (existing, replacement) -> existing));
        // 从数据库中获取APP列表
        for (JavaAppDto appFromDb : agentMsgService.generateJavaAppDto()) {
            boolean exist = aliveApps.containsKey(appFromDb.getAppName());
            if (exist) {
                JavaAppDto aliveApp = aliveApps.get(appFromDb.getAppName());
                aliveApp.setAlive(true);
                aliveApp.setHasLog(true);
            } else {
                appFromDb.setAlive(false);
                appFromDb.setHasLog(true);
                aliveApps.put(appFromDb.getAppName(), appFromDb);
            }
        }
        List<JavaAppDto> result = aliveApps.values().stream().sorted(Comparator.comparing(b -> !b.isHasAttached())).toList();
        return Result.success(result);
    }

    public Result<List<ThreadDto>> getTreadList(JavaAppDto javaAppDto) {
        List<ThreadDto> javaAppDtos = agentMsgService.generateThreadDto(javaAppDto);
        return Result.success(javaAppDtos);
    }

    public Result<List<AgentMsgService.MethodCall>> getRes(GetMethodTreeDto getMethodTreeDto) {
        List<AgentMsgService.MethodCall> res = agentMsgService.generateBy(getMethodTreeDto.getAppName(), getMethodTreeDto.getThreadId());
        return Result.success(res);
    }

    public Result<Boolean> clearAppMsg(JavaAppDto javaAppDto) {
        agentMsgService.clearBy(javaAppDto.getAppName());
        return Result.success(true);
    }

    public Result<Boolean> attach(JavaAppDto javaAppDto) {
        if (ATTACHED_VMS.containsKey(javaAppDto.getPid())) {
            return Result.fail("已连接");
        }
        try {
            List<VirtualMachineDescriptor> vms = VirtualMachine.list();
            Optional<VirtualMachineDescriptor> optional = vms.stream().filter(vm -> vm.id().equals(javaAppDto.getPid().toString())).findFirst();
            if (optional.isPresent()) {
                VirtualMachineDescriptor virtualMachineDescriptor = optional.get();
                String displayName = virtualMachineDescriptor.displayName().split(" ")[0];
                VirtualMachine virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
                virtualMachine.loadAgent("/opt/JDataView/agent/JDataViewAgent-1.0.0.jar", "mode=install;scanPack=" + javaAppDto.getScanPackage() + ";appName=" + displayName);
                ATTACHED_VMS.put(javaAppDto.getPid(), virtualMachine);
                return Result.success(true);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Result.success(false);
    }

    public Result<Boolean> detach(JavaAppDto javaAppDto) {
        try {
            VirtualMachine virtualMachine = ATTACHED_VMS.get(javaAppDto.getPid());
            virtualMachine.loadAgent("/opt/JDataView/agent/JDataViewAgent-1.0.0.jar", "mode=uninstall");
            virtualMachine.detach();
            ATTACHED_VMS.remove(javaAppDto.getPid());
            return Result.success(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Result.success(false);
    }

    @PreDestroy
    public void destroy() {
        for (VirtualMachine virtualMachine : ATTACHED_VMS.values()) {
            try {
                virtualMachine.loadAgent("/opt/JDataView/agent/JDataViewAgent-1.0.0.jar", "mode=uninstall");
                virtualMachine.detach();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
