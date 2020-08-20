package org.forchange.canal.application.service;

import org.forchange.canal.Bootstrap;
import org.forchange.canal.infrastructure.contants.TaskStatus;
import org.forchange.canal.ui.dto.request.ListenerConfig;
import org.forchange.canal.ui.dto.request.TaskConfig;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @fileName: CanalServiceTest.java
 * @description: CanalServiceTest.java类说明
 * @author: by echo huang
 * @date: 2020-08-20 16:38
 */
@SpringBootTest(classes = Bootstrap.class)
@RunWith(SpringRunner.class)
class CanalServiceTest {

    @Autowired
    private CanalService canalService;

    @Test
    @Ignore
    void addTask() throws InterruptedException {
        TaskConfig taskConfig = new TaskConfig();
        taskConfig.setCanalConnectorName("test");
        ListenerConfig listenerConfig = new ListenerConfig();
        listenerConfig.setListenRules("xxx:xxx");
        taskConfig.setListener(listenerConfig);
        taskConfig.setTaskName("canal-test");

        TaskStatus taskStatus = canalService.addTask(taskConfig);
        System.out.println(taskStatus);
        while (true) {
            Thread.sleep(1000);
            System.out.println("handle....");
        }
    }
}