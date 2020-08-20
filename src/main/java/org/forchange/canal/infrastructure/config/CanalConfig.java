package org.forchange.canal.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @fileName: CanalConfig.java
 * @description: CanalConfig.java类说明
 * @author: by echo huang
 * @date: 2020-08-19 20:04
 */
@Data
@Component
@ConfigurationProperties(prefix = "canal")
public class CanalConfig {
    private List<CanalProperties> configs;
}
