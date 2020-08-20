package org.forchange.canal.ui.dto.request;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.forchange.canal.infrastructure.exception.CanalException;

/**
 * @fileName: TaskConfig.java
 * @description: TaskConfig.java类说明
 * @author: by echo huang
 * @date: 2020-08-19 18:57
 */
@Data
public class TaskConfig {
    private String subscribeFilter = ".*\\..*";
    private String taskName;

    /**
     * canal连接器名称
     */
    private String canalConnectorName;
    /**
     * 监听器配置
     */
    private ListenerConfig listener;

    public void validate() {
        if (StringUtils.isEmpty(this.taskName)) {
            throw new CanalException("canal采集任务名称不能为空");
        }
        if (StringUtils.isEmpty(this.canalConnectorName)) {
            throw new CanalException("canal连接器名称不能为空");
        }
    }
}
