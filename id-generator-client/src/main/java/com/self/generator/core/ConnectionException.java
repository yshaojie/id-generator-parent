package com.self.generator.core;

/**
 * client 连接失败异常
 * Created by shaojieyue on 11/19/15.
 */
public class ConnectionException extends Exception{
    public ConnectionException(String host,int port,Throwable cause){
        super("connect "+host+":"+port+" fail.",cause);
    }
}
