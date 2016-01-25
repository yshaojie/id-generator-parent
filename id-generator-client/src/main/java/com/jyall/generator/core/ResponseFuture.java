package com.jyall.generator.core;

import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.TimeUnit;

public class ResponseFuture{

    private SettableFuture<Long> future ;
    private volatile boolean cancel = false; //请求是否取消,如果是等待超时也视为取消
    private ResponseFuture() {
        future = SettableFuture.create();
    }

    public static ResponseFuture create(){
        return new ResponseFuture();
    }

    /**
     * 请求完成
     * @param id
     */
    public void completed(Long id) {
        future.set(id);
    }

    /**
     * 请求失败
     * @param throwable
     */
    public void fail(Throwable throwable){
        future.setException(throwable);
    }

    /**
     * 请求是否取消
     * @return
     */
    public boolean isCancel(){
        return cancel;
    }

    /**
     * 阻塞等待返回
     * @return
     * @throws WaitException
     */
    public Long get() throws WaitException{
        try {
            return future.get(100,TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            cancel = true;//如果等待超时,则也认为请求取消
            throw new WaitException("wait result exception",e);
        }
    }

}
