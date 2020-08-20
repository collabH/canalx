package org.forchange.canal.infrastructure.config;

import lombok.Data;

/**
 * @fileName: CanalConfig.java
 * @description: CanalConfig.java类说明
 * @author: by echo huang
 * @date: 2020-08-19 18:57
 */
@Data
public class CanalProperties {

    private String name;
    private String hostname;
    private Integer port;
    private String destination;
    private String username;
    private String password;
}
