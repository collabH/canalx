package org.forchange.canal.application.service.impl;

import com.alibaba.otter.canal.client.CanalConnector;
import org.forchange.canal.application.service.CanalService;
import org.forchange.canal.infrastructure.channel.BaseDataChannel;
import org.forchange.canal.infrastructure.contants.TaskStatus;
import org.forchange.canal.infrastructure.exception.CanalException;
import org.forchange.canal.infrastructure.task.CanalDumpTask;
import org.forchange.canal.infrastructure.task.CanalTaskQueue;
import org.forchange.canal.infrastructure.utils.SpringApplicationContext;
import org.forchange.canal.ui.dto.request.TaskConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * @fileName: CanalServiceImpl.java
 * @description: canal服务
 * @author: by echo huang
 * @date: 2020-08-19 19:53
 */
@Service
public class CanalServiceImpl implements CanalService {
    @Autowired
    private Map<String, CanalConnector> connectorMap;
    @Autowired
    private CanalTaskQueue canalTaskQueue;

    @Override
    public TaskStatus addTask(TaskConfig taskConfig) {
        String canalConnectorName = taskConfig.getCanalConnectorName();
        if (Objects.isNull(connectorMap.get(canalConnectorName))) {
            throw new CanalException(String.format("canalConnectorName:%s不存在,请添加相关配置", canalConnectorName));
        }
        CanalConnector canalConnector = connectorMap.get(canalConnectorName);
        BaseDataChannel dataChannel = SpringApplicationContext.getBean(taskConfig.getListener().getChannelType(), BaseDataChannel.class);
        return canalTaskQueue.addTask(taskConfig.getTaskName(), new CanalDumpTask(dataChannel, canalConnector, taskConfig));
    }

    @Override
    public TaskStatus cancelTask(String taskName) {
        return canalTaskQueue.cancelTask(taskName);
    }
}
