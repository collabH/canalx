package org.forchange.canal.infrastructure.utils;

import org.forchange.canal.infrastructure.exception.CanalException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * @fileName: SpringApplicationContext.java
 * @description: SpringApplicationContext.java类说明
 * @author: by echo huang
 * @date: 2020-08-19 20:23
 */
@Component
public class SpringApplicationContext implements ApplicationContextAware {
    
    private static ApplicationContext applicationContext = null;

    public static <T> T getBeanByName(String beanName) {
        if (applicationContext == null) {
            return null;
        }
        return (T) applicationContext.getBean(beanName);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 根据类型获取bean集合，key为beanName，value为bean
     * @param clazz bean的实例类
     * @param <T>
     * @return
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return getApplicationContext().getBeansOfType(clazz);
    }

    public static <T> T getBean(String name) {
        return (T) getApplicationContext().getBean(name);
    }

    public static <T> void addBean(String beanName, T beanObj) {
        ConfigurableListableBeanFactory beanDefReg = ((AnnotationConfigServletWebServerApplicationContext) applicationContext).getBeanFactory();
        if (!beanDefReg.containsBeanDefinition(beanName)) {
            beanDefReg.registerSingleton(beanName, beanObj);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringApplicationContext.applicationContext = applicationContext;
    }

    /**
     * 获取applicationContext
     *
     * @return
     */
    private static ApplicationContext getApplicationContext() {
        if (Objects.isNull(applicationContext)) {
            throw new CanalException("applicationContext注入失败");
        }
        return applicationContext;
    }
}
