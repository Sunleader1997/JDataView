package org.sunyaxing.imagine.jdataviewserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.sunyaxing.imagine.jdataviewserver.common.Result;
import org.sunyaxing.imagine.jdataviewserver.controller.dtos.GetMethodTreeDto;
import org.sunyaxing.imagine.jdataviewserver.controller.dtos.JavaAppDto;
import org.sunyaxing.imagine.jdataviewserver.controller.dtos.ThreadDto;
import org.sunyaxing.imagine.jdataviewserver.service.AgentMsgService;

import java.util.List;

@RestController
@RequestMapping("/api/javaApp")
public class JavaAppController {

    @Autowired
    private AgentMsgService agentMsgService;

    /**
     * 获取APP列表
     */
    @GetMapping("/getJavaApps")
    public Result<List<JavaAppDto>> getJavaApps() {
        // 本地JVM中的服务
//        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
//        List<JavaAppDto> javaAppDtos = vms.stream().map(vm -> {
//            String pid = vm.id();
//            String name = vm.displayName();
//            return JavaAppDto.builder().pid(Long.valueOf(pid)).appName(name).host("127.0.0.1").build();
//        }).toList();
        List<JavaAppDto> javaAppDtos = agentMsgService.generateJavaAppDto();
        return Result.success(javaAppDtos);
    }

    /**
     * 获取APP列表
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
}
