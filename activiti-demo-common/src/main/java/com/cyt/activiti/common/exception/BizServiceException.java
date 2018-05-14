package com.cyt.activiti.common.exception;

/**
 * @author CaoYangTao
 * @date 2018/4/25  13:42
 */
public class BizServiceException extends RuntimeException {
    public BizServiceException() {
        super();
    }

    public BizServiceException(String message) {
        super(message);
    }

    public BizServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public BizServiceException(Throwable cause) {
        super(cause);
    }
}
