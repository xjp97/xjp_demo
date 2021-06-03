package com.ccq.exception;

/**
 * @author xujunping
 * @date 2019/10/31
 */
public class FileSystemException extends RuntimeException {
    public FileSystemException() {
    }

    public FileSystemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public FileSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileSystemException(String message) {
        super(message);
    }

    public FileSystemException(Throwable cause) {
        super(cause);
    }
}