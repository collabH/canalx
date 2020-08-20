package org.forchange.canal.infrastructure.channel;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.forchange.canal.infrastructure.exception.CanalException;
import org.forchange.canal.ui.dto.request.ListenerConfig;

import java.util.List;

import static com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import static com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import static com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import static com.alibaba.otter.canal.protocol.CanalEntry.Header;
import static com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import static com.alibaba.otter.canal.protocol.CanalEntry.RowData;

/**
 * @fileName: BaseDataChannel.java
 * @description: 数据通道基类
 * @author: by echo huang
 * @date: 2020-08-19 18:48
 */
@Slf4j
public abstract class BaseDataChannel {
    /**
     * 数据管道核心数据处理逻辑
     *
     * @param listener 监听器配置
     * @param entries  批量数据
     */
    public void handle(ListenerConfig listener, List<Entry> entries) {
        for (Entry entry : entries) {
            // 过滤消息
            if (filter(entry, listener)) {
                continue;
            }
            // 过滤事务begin和end操作
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
                continue;
            }
            RowChange rowChange;
            try {
                rowChange = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new CanalException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                        e);
            }
            for (RowData rowData : rowChange.getRowDatasList()) {
                publish(entry, rowData);
            }

        }
    }

    /**
     * schemaName和tableName过滤
     *
     * @param entry    Dump数据
     * @param listener 监听器配置
     * @return 是否过滤
     */
    private boolean filter(Entry entry, ListenerConfig listener) {
        String listenRules = listener.getListenRules();
        String[] rulesArr = listenRules.split(",");
        List<String> listenEventType = listener.getListenEventType();
        Header header = entry.getHeader();
        EventType eventType = header.getEventType();
        // 过滤不监听的事件
        if (!listenEventType.contains(eventType.name())) {
            return true;
        }
        for (String rule : rulesArr) {
            String[] listenSchemaTableName = rule.split(":");
            //fixme  如果不存在，则不过滤
            if (ArrayUtils.isEmpty(listenSchemaTableName) || listenSchemaTableName.length != 2) {
                log.error("rule:{}解析异常", rule);
                return false;
            }
            String schemaName = listenSchemaTableName[0];
            String tableName = listenSchemaTableName[1];
            //fixme 如果schemaName为*号，只处理tableName
            if (StringUtils.equals("*", schemaName)) {
                // 兼容*:*情况，不考虑*:test,hello:*情况
                if (StringUtils.equals("*", tableName)) {
                    return false;
                } else {
                    return !StringUtils.contains(tableName, header.getTableName());
                }
            }
            if (StringUtils.equals(header.getSchemaName(), schemaName)) {
                if (StringUtils.equals("*", tableName)) {
                    return false;
                } else {
                    return !StringUtils.contains(tableName, header.getTableName());
                }
            }

        }
        return true;
    }

    /**
     * 自定义消息解析
     *
     * @param rowData 一行消息
     * @param entry   消息体
     * @return
     */
    public abstract void publish(Entry entry, RowData rowData);
}
