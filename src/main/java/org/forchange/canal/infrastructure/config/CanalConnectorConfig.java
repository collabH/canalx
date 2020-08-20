package org.forchange.canal.infrastructure.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.forchange.canal.infrastructure.exception.CanalException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @fileName: CanalConnectorConfig.java
 * @description: canal连接器配置
 * @author: by echo huang
 * @date: 2020-08-19 20:07
 */
@Configuration
public class CanalConnectorConfig {

    @Bean
    public Map<String, CanalConnector> canalConnectorMap(CanalConfig canalConfig) {
        List<CanalProperties> configs = canalConfig.getConfigs();
        if (CollectionUtils.isEmpty(configs)) {
            throw new CanalException("canal配置");
        }
        Map<String, CanalConnector> canalConnectorMap = Maps.newHashMap();
        for (CanalProperties config : configs) {
            CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(config.getHostname(),
                    config.getPort()), config.getDestination(), config.getUsername(), config.getPassword());
            canalConnectorMap.putIfAbsent(config.getName(), connector);
        }
        return canalConnectorMap;
    }

    /**
     * 任务运行线程池
     *
     * @return
     */
    @Bean(name = "taskPool",destroyMethod = "shutdown")
    public ExecutorService taskThreadPool() {
        return new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 2,
                2, TimeUnit.MINUTES, new ArrayBlockingQueue<>(1000), new ThreadFactoryBuilder()
                .setNameFormat("canal-task-%d").build());
    }
}
