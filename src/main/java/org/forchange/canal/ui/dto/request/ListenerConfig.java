package org.forchange.canal.ui.dto.request;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * @fileName: ListenerConfig.java
 * @description: 监听配置
 * @author: by echo huang
 * @date: 2020-08-19 19:38
 */
@Data
public class ListenerConfig {

    /**
     * 格式为:
     * schemaName1:tableName1,schemaName2:tableName2，例如:"for_os":"*","for_ops":"*"
     * fixme 默认为"*",表示监听全部,使用逗号来分割同时监听多个schema,如果出现一个schema为*后,只处理tableName
     * fixme 默认为"*",表示监听全部,使用逗号来分割同时监听多个table
     */
    private String listenRules = "*:*";

    /**
     * 批量处理的数据大小，默认为5000
     */
    private Integer batchSize = 5000;

    /**
     * channel类型,default为kafka
     * 1. Kafka
     * 2. RocketMQ
     * 3. TODO
     */
    private String channelType = "kafka";

    /**
     * 监听的事件类型
     * DELETE
     * INSERT
     * UPDATE
     */
    private List<String> listenEventType = Lists.newArrayList("DELETE",
            "UPDATE", "INSERT");
}
