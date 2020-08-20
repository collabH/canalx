package org.forchange.canal.infrastructure.channel;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.forchange.canal.domain.binlog.DTSBinlogFormatDo;
import org.forchange.canal.domain.binlog.Data;
import org.forchange.canal.domain.binlog.Source;
import org.forchange.canal.infrastructure.utils.SnowflakeIdWorker;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.alibaba.otter.canal.protocol.CanalEntry.Column;
import static com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import static com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import static com.alibaba.otter.canal.protocol.CanalEntry.Header;
import static com.alibaba.otter.canal.protocol.CanalEntry.RowData;

/**
 * @fileName: KafkaDataChannel.java
 * @description: KafkaDataChannel.java类说明
 * @author: by echo huang
 * @date: 2020-08-19 18:53
 */
@Component("kafka")
@Slf4j
public class KafkaDataChannel extends BaseDataChannel implements InitializingBean {
    private SnowflakeIdWorker snowflakeIdWorker;

    private static final Map<String, AtomicInteger> FAIL_TIMES_COUNTER = Maps.newConcurrentMap();

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Override
    public void publish(Entry entry, RowData rowData) {
        Header header = entry.getHeader();
        EventType eventType = header.getEventType();
        DTSBinlogFormatDo dtsBinlog = new DTSBinlogFormatDo();
        buildDtsBinlog(dtsBinlog, header);
        switch (eventType) {
            case DELETE:
                handleColumns(rowData.getBeforeColumnsList(), dtsBinlog, dtsBinlog.getData().getBefore());
                break;
            case INSERT:
                handleColumns(rowData.getAfterColumnsList(), dtsBinlog, dtsBinlog.getData().getAfter());
                break;
            case UPDATE:
                handleColumns(rowData.getBeforeColumnsList(), dtsBinlog, dtsBinlog.getData().getBefore());
                handleColumns(rowData.getAfterColumnsList(), dtsBinlog, dtsBinlog.getData().getAfter());
                break;
            default:
                // fixme 新事件可以在这里添加
                break;
        }
        send(dtsBinlog);
    }

    /**
     * 发送数据，失败会重试三次
     *
     * @param dtsBinlogFormatDo
     */
    private void send(DTSBinlogFormatDo dtsBinlogFormatDo) {
        String topic = dtsBinlogFormatDo.getDbName() + "." + dtsBinlogFormatDo.getTableName();
        AtomicInteger failCounter = getFailCounter(topic);
        String binlog = JSON.toJSONString(dtsBinlogFormatDo);
        kafkaTemplate.executeInTransaction(kafkaOperations -> {
            ListenableFuture future = kafkaOperations.send(topic, dtsBinlogFormatDo.getPrimaryKey(), binlog);
            future.addCallback(value -> {
                // fixme 发送成功目前什么都不做
            }, throwable -> {
                // 失败重发
                if (!failCounter.compareAndSet(1, 3)) {
                    failCounter.decrementAndGet();
                    kafkaTemplate.send(topic, dtsBinlogFormatDo.getPrimaryKey(), binlog);
                    return;
                }
                //fixme 后期可以通过钉钉告警出来
                log.error("topic:{},binlog:{}发送失败", topic, binlog);
            });
            return future.isDone();
        });
    }

    /**
     * 根据topic拿到失败计数器
     *
     * @param topic topic
     * @return 失败计数器
     */
    private static AtomicInteger getFailCounter(String topic) {
        AtomicInteger failCounter = FAIL_TIMES_COUNTER.getOrDefault(topic, new AtomicInteger(3));
        FAIL_TIMES_COUNTER.putIfAbsent(topic, failCounter);
        return failCounter;
    }

    /**
     * 封装dfsBinlog格式
     *
     * @param dtsBinlog dtsBinlog格式
     * @param header    binlogDump文件头
     */
    private void buildDtsBinlog(DTSBinlogFormatDo dtsBinlog, Header header) {
        // 封装数据
        Data data = new Data();
        Map<String, String> after = Maps.newHashMap();
        Map<String, String> before = Maps.newHashMap();
        data.setAfter(after);
        data.setBefore(before);
        dtsBinlog.setData(data);
        dtsBinlog.setDbName(header.getSchemaName());
        dtsBinlog.setTableName(header.getTableName());
        Source source = new Source();
        source.setSourceType(header.getSourceType().name());
        source.setVersion(header.getLogfileName() + ":" + header.getLogfileOffset());
        dtsBinlog.setSource(source);
        dtsBinlog.setRecordType(header.getEventType().name());
        dtsBinlog.setRecordTimestamp(System.currentTimeMillis() / 1000);
        dtsBinlog.setExtraTags(Maps.newHashMap());
        //fixme 目前基于雪花id
        dtsBinlog.setRecordID(snowflakeIdWorker.nextId());
    }


    /**
     * 处理列
     *
     * @param columnsList 列集合
     * @param dtsBinlog   binlog消息题
     * @param values      处理值
     */
    private void handleColumns(List<Column> columnsList, DTSBinlogFormatDo dtsBinlog, Map<String, String> values) {
        List<String> primaryKeys = Lists.newArrayList();
        for (Column column : columnsList) {
            // 设置主键值
            if (column.getIsKey()) {
                primaryKeys.add(column.getName());
                dtsBinlog.setPrimaryKey(column.getValue());
            }
            values.putIfAbsent(column.getName(), column.getValue());
        }
        dtsBinlog.getExtraTags().putIfAbsent("PRIMARY", primaryKeys);
        dtsBinlog.getExtraTags().putIfAbsent("readerThroughoutTime", System.currentTimeMillis());
// todo 代参看        dtsBinlog.getExtraTags().putIfAbsent("pk_uk_info", System.currentTimeMillis());
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        snowflakeIdWorker = SnowflakeIdWorker.getInstance();
    }
}
