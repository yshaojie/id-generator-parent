package com.self.generator.common.exception;

/**
 * 不知道的请求异常
 * Created by shaojieyue on 11/15/15.
 */
public class UnknowRequestException extends RuntimeException{
    public UnknowRequestException(){
        super("unknow id fetch request");
    }
}
