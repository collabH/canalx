package org.forchange.canal.infrastructure.task;

/**
 * @fileName: CanalTask.java
 * @description: CanalTask.java类说明
 * @author: by echo huang
 * @date: 2020-08-20 11:45
 */
public interface CanalTask extends Runnable {
    /**
     * 取消任务
     */
    void cancelTask();
}
