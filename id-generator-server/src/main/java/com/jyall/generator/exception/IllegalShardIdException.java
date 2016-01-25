package com.jyall.generator.exception;

/**
 * 非法的workid异常
 * Created by shaojieyue on 11/5/15.
 */
public class IllegalShardIdException extends IllegalArgumentException{
    public IllegalShardIdException() {
    }

    public IllegalShardIdException(String message) {
        super(message);
    }
}
