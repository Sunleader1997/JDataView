package org.sunyaxing.imagine.jdataviewserver.controller;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.springframework.web.bind.annotation.*;
import org.sunyaxing.imagine.jdataviewserver.common.Result;
import org.sunyaxing.imagine.jdataviewserver.controller.dtos.JavaAppDto;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/local")
public class LocalAppController {

    @GetMapping("/localApp")
    public Result<List<JavaAppDto>> loadLocalApp() {
        // 本地JVM中的服务
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        List<JavaAppDto> javaAppDtos = vms.stream().map(vm -> {
            String pid = vm.id();
            String name = vm.displayName();
            return JavaAppDto.builder().pid(Long.valueOf(pid)).appName(name).host("127.0.0.1").build();
        }).toList();
        return Result.success(javaAppDtos);
    }

    @PostMapping("/attach")
    public Result<Boolean> attach(@RequestBody JavaAppDto javaAppDto) {
        try {
            List<VirtualMachineDescriptor> vms = VirtualMachine.list();
            Optional<VirtualMachineDescriptor> optional = vms.stream().filter(vm -> vm.id().equals(javaAppDto.getPid().toString())).findFirst();
            if (optional.isPresent()) {
                VirtualMachineDescriptor virtualMachineDescriptor = optional.get();
                String displayName = virtualMachineDescriptor.displayName();
                String packageName = "";
                int lastDotIndex = displayName.lastIndexOf('.');
                if (lastDotIndex > 0) {
                    packageName = displayName.substring(0, lastDotIndex);
                }
                VirtualMachine virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
                virtualMachine.loadAgent("/opt/JDataViewAgent/agent/JDataViewAgent-1.0.0.jar", "scanPack=" + packageName);
                return Result.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success(false);
    }
}
