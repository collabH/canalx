package org.forchange.canal;

import org.forchange.canal.infrastructure.config.CanalConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * @fileName: Bootstrap.java
 * @description: Bootstrap.java类说明
 * @author: by echo huang
 * @date: 2020-08-19 18:42
 */
@EnableKafka
@SpringBootApplication
@EnableConfigurationProperties(CanalConfig.class)
public class Bootstrap {
    public static void main(String[] args) {
        new SpringApplication(Bootstrap.class).run(args);
    }
}
