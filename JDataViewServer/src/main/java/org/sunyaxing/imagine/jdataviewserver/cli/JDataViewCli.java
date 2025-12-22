package org.sunyaxing.imagine.jdataviewserver.cli;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.sunyaxing.imagine.jdataviewserver.controller.dtos.JavaAppDto;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "JDataViewCli", version = "JDataViewCli 1.0", mixinStandardHelpOptions = true)
public class JDataViewCli implements Callable<Integer> {

    @CommandLine.Option(names = {"--list", "-l"},
            description = "列出所有的服务")
    private boolean listJavaApp;

    @Override
    public Integer call() throws Exception {
        if (listJavaApp) {
            // 本地JVM中的服务
            List<VirtualMachineDescriptor> vms = VirtualMachine.list();
            List<JavaAppDto> javaAppDtos = vms.stream().map(vm -> {
                String pid = vm.id();
                String name = vm.displayName();
                return JavaAppDto.builder().pid(Long.valueOf(pid)).appName(name).host("127.0.0.1").build();
            }).toList();
        }
        return 0;
    }
}
