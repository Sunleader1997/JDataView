package org.sunyaxing.imagine.jdataviewserver.controller;

import cn.hutool.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunyaxing.imagine.jdataviewserver.common.Result;
import org.sunyaxing.imagine.jdataviewserver.service.AgentMsgService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/demo")
public class DemoController {
    @Autowired
    private AgentMsgService agentMsgService;

    @GetMapping("/getRes")
    public Result<Map<Long, List<AgentMsgService.MethodCall>>> getRes() {
        Map<Long, List<AgentMsgService.MethodCall>> res = agentMsgService.generateBy("org.sunyaxing.imagine.jdataview.AppApplication");
        return Result.success(res);
    }

    /**
     * 获取接口调用树
     * 仅加载一级调用
     */
    @GetMapping("/getFunctionTree")
    public Result<JSONObject> getFunctionTree() {
        return null;
    }
}
