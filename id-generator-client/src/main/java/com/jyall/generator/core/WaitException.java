package com.jyall.generator.core;

/**
 * Created by shaojieyue on 11/15/15.
 */
public class WaitException extends Exception{
    public WaitException(String message, Throwable cause){
        super(message, cause);
    }

    public WaitException(String message) {
        super(message);
    }
}
