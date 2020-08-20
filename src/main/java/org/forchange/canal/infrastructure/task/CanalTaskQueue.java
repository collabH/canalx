package org.forchange.canal.infrastructure.task;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.forchange.canal.infrastructure.contants.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static org.forchange.canal.infrastructure.contants.TaskStatus.FAIL;
import static org.forchange.canal.infrastructure.contants.TaskStatus.SUCCESS;
import static org.forchange.canal.infrastructure.contants.TaskStatus.TASK_NOT_FOUNT;

/**
 * @fileName: CanalTaskQueue.java
 * @description: Canal任务队列
 * @author: by echo huang
 * @date: 2020-08-20 11:34
 */
@Component
@Slf4j
public class CanalTaskQueue {
    /**
     * key:taskName value:TaskQueue
     */
    public static final Map<String, CanalTask> CANAL_TASK_QUEUE = Maps.newConcurrentMap();

    @Autowired
    private ExecutorService taskPool;

    /**
     * 添加并执行任务，如果存在正在运行的任务，则取消旧任务，运行新任务
     *
     * @param taskName 采集log任务名称
     * @param task     任务名称
     * @param <T>
     * @return 任务运行状态
     */
    public <T extends CanalTask> TaskStatus addTask(String taskName, T task) {
        try {
            CanalTask runningTask = CANAL_TASK_QUEUE.get(taskName);
            // 如果没有正在运行的任务，则将task加入队列中
            if (Objects.isNull(runningTask)) {
                runTask(taskName, task);
                log.info("taskName:{}运行成功", taskName);
                return SUCCESS;
            }
            // 如果存在正在运行的任务,取消正在运行的任务，运行新任务
            runningTask.cancelTask();
            runTask(taskName, task);
            log.info("taskName:{}运行成功", taskName);
            return SUCCESS;
        } catch (Exception e) {
            return FAIL;
        }
    }


    /**
     * 取消任务
     *
     * @param taskName 任务名称
     * @return 任务状态
     */
    public TaskStatus cancelTask(String taskName) {
        try {
            CanalTask canalTask = CANAL_TASK_QUEUE.get(taskName);
            if (Objects.nonNull(canalTask)) {
                canalTask.cancelTask();
                log.info("taskName:{}取消成功", taskName);
                return SUCCESS;
            }
            return TASK_NOT_FOUNT;
        } catch (Exception e) {
            return FAIL;
        }
    }

    /**
     * 运行任务
     *
     * @param taskName 任务名称
     * @param task     task
     * @param <T>
     */
    public <T extends CanalTask> void runTask(String taskName, T task) {
        CANAL_TASK_QUEUE.putIfAbsent(taskName, task);
        taskPool.execute(task);
    }

}
