package com.jyall.generator.common.exception;

/**
 * 服务端错误异常
 * Created by shaojieyue on 11/15/15.
 */
public class ServerInternalException extends Exception{
    public ServerInternalException(){
        super("server internal error");
    }
}
