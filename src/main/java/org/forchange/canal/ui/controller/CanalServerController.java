package org.forchange.canal.ui.controller;

import org.forchange.canal.application.service.CanalService;
import org.forchange.canal.infrastructure.contants.TaskStatus;
import org.forchange.canal.ui.dto.request.TaskConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.forchange.canal.infrastructure.contants.CanalContants.CANAL_SERVER_API;

/**
 * @fileName: CanalServerController.java
 * @description: canal服务控制器
 * @author: by echo huang
 * @date: 2020-08-19 18:45
 */
@RestController
@RequestMapping(value = CANAL_SERVER_API)
public class CanalServerController {

    @Autowired
    private CanalService canalService;

    /**
     * 添加task
     *
     * @param taskConfig 任务配置
     * @return 是否成功
     */
    @PostMapping("/addTask")
    public String addTask(@RequestBody TaskConfig taskConfig) {
        taskConfig.validate();
        TaskStatus taskStatus = canalService.addTask(taskConfig);
        return taskStatus.name();
    }

    /**
     * @param taskName 任务名称
     * @return 是否成功
     */
    @GetMapping("/cancelTask/{taskName}")
    public String cancelTask(@PathVariable("taskName") String taskName) {
        TaskStatus taskStatus = canalService.cancelTask(taskName);
        return taskStatus.name();
    }
}
