package org.forchange.canal.application.service;

import org.forchange.canal.infrastructure.contants.TaskStatus;
import org.forchange.canal.ui.dto.request.TaskConfig;

/**
 * @fileName: CanalService.java
 * @description: Canal服务
 * @author: by echo huang
 * @date: 2020-08-19 18:47
 */
public interface CanalService {

    /**
     * 启动服务
     *
     * @param taskConfig 任务配置
     * @return 任务运行状态
     */
    TaskStatus addTask(TaskConfig taskConfig);

    /**
     * 取消任务
     *
     * @param taskName task名称
     * @return 任务状态
     */
    TaskStatus cancelTask(String taskName);
}
