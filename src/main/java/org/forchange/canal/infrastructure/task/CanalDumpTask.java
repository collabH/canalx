package org.forchange.canal.infrastructure.task;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.exception.CanalClientException;
import lombok.extern.slf4j.Slf4j;
import org.forchange.canal.infrastructure.channel.BaseDataChannel;
import org.forchange.canal.ui.dto.request.ListenerConfig;
import org.forchange.canal.ui.dto.request.TaskConfig;

/**
 * @fileName: CanalDumpTask.java
 * @description: canal dumpbinlog 任务
 * @author: by echo huang
 * @date: 2020-08-20 11:25
 */
@Slf4j
public class CanalDumpTask implements CanalTask {
    private volatile boolean isRunning = true;

    /**
     * 数据通道
     */
    private BaseDataChannel dataChannel;

    private CanalConnector connector;

    private TaskConfig taskConfig;

    public CanalDumpTask(BaseDataChannel dataChannel, CanalConnector canalConnector, TaskConfig taskConfig) {
        this.dataChannel = dataChannel;
        this.connector = canalConnector;
        this.taskConfig = taskConfig;
    }

    @Override
    public void run() {
        ListenerConfig listener = taskConfig.getListener();
        connector.connect();
        // 设置canal过滤器规则
        connector.subscribe(taskConfig.getSubscribeFilter());
        connector.rollback();
        while (isRunning) {
            Integer batchSize = listener.getBatchSize();
            try {
                Message message = connector.getWithoutAck(batchSize);
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId != -1 || size != 0) {
                    //  分发数据
                    dataChannel.handle(listener, message.getEntries());
                }
                connector.ack(batchId);
            } catch (CanalClientException e) {
                connector.rollback();
            }
        }
    }


    @Override
    public void cancelTask() {
        isRunning = false;
        connector.disconnect();
    }
}
