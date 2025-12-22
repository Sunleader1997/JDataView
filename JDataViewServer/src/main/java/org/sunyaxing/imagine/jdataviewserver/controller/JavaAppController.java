package org.sunyaxing.imagine.jdataviewserver.controller;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.sunyaxing.imagine.jdataviewserver.common.Result;
import org.sunyaxing.imagine.jdataviewserver.controller.dtos.GetMethodTreeDto;
import org.sunyaxing.imagine.jdataviewserver.controller.dtos.JavaAppDto;
import org.sunyaxing.imagine.jdataviewserver.controller.dtos.ThreadDto;
import org.sunyaxing.imagine.jdataviewserver.service.AgentMsgService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/javaApp")
public class JavaAppController {

    @Autowired
    private AgentMsgService agentMsgService;

    /**
     * 记录已经启动的APP
     */
    private static final HashMap<Long, VirtualMachine> ATTACHED_VMS = new HashMap<>();

    /**
     * 获取APP列表
     */
    @GetMapping("/getJavaApps")
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
        }).collect(Collectors.toMap(JavaAppDto::getAppName, app -> app));
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
        return Result.success(aliveApps.values().stream().toList());
    }

    /**
     * 获取APP线程历史
     */
    @PostMapping("/getTreadList")
    public Result<List<ThreadDto>> getTreadList(@RequestBody JavaAppDto javaAppDto) {
        List<ThreadDto> javaAppDtos = agentMsgService.generateThreadDto(javaAppDto);
        return Result.success(javaAppDtos);
    }

    @PostMapping("/getMethodTree")
    public Result<List<AgentMsgService.MethodCall>> getRes(@RequestBody GetMethodTreeDto getMethodTreeDto) {
        List<AgentMsgService.MethodCall> res = agentMsgService.generateBy(getMethodTreeDto.getAppName(), getMethodTreeDto.getThreadId());
        return Result.success(res);
    }

    @PostMapping("/clearAppMsg")
    public Result<Boolean> clearAppMsg(@RequestBody JavaAppDto javaAppDto) {
        agentMsgService.clearBy(javaAppDto.getAppName());
        return Result.success(true);
    }

    @PostMapping("/attach")
    public Result<Boolean> attach(@RequestBody JavaAppDto javaAppDto) {
        if (ATTACHED_VMS.containsKey(javaAppDto.getPid())) {
            return Result.fail("已连接");
        }
        try {
            List<VirtualMachineDescriptor> vms = VirtualMachine.list();
            Optional<VirtualMachineDescriptor> optional = vms.stream().filter(vm -> vm.id().equals(javaAppDto.getPid().toString())).findFirst();
            if (optional.isPresent()) {
                VirtualMachineDescriptor virtualMachineDescriptor = optional.get();
                String displayName = virtualMachineDescriptor.displayName().split(" ")[0];
                String packageName = "";
                int lastDotIndex = displayName.lastIndexOf('.');
                if (lastDotIndex > 0) {
                    packageName = displayName.substring(0, lastDotIndex);
                }
                VirtualMachine virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
                virtualMachine.loadAgent("/opt/JDataViewAgent/agent/JDataViewAgent-1.0.0.jar", "mode=install;scanPack=" + packageName + ";appName=" + displayName);
                ATTACHED_VMS.put(javaAppDto.getPid(), virtualMachine);
                return Result.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success(false);
    }

    @PostMapping("/detach")
    public Result<Boolean> detach(@RequestBody JavaAppDto javaAppDto) {
        try {
            VirtualMachine virtualMachine = ATTACHED_VMS.get(javaAppDto.getPid());
            virtualMachine.loadAgent("/opt/JDataViewAgent/agent/JDataViewAgent-1.0.0.jar", "mode=uninstall");
            virtualMachine.detach();
            ATTACHED_VMS.remove(javaAppDto.getPid());
            return Result.success(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success(false);
    }

    @PreDestroy
    public void destroy() {
        for (VirtualMachine virtualMachine : ATTACHED_VMS.values()) {
            try {
                virtualMachine.detach();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
