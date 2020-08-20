package org.forchange.canal.infrastructure.exception;

/**
 * @fileName: CanalException.java
 * @description: 自定义异常
 * @author: by echo huang
 * @date: 2020-08-19 20:11
 */
public class CanalException extends RuntimeException {
    public CanalException(String message) {
        super(message);
    }

    public CanalException(String message, Throwable cause) {
        super(message, cause);
    }

    public CanalException(Throwable cause) {
        super(cause);
    }

    protected CanalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
