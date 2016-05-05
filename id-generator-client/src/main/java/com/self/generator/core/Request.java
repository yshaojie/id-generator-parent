package com.self.generator.core;

/**
 * 请求实体
 * Created by shaojieyue on 11/15/15.
 */
public class Request{
    private ResponseFuture responseFuture;
    private int idType;

    public Request(int idType){
        this.idType = idType;
        responseFuture = ResponseFuture.create();
    }

    public int getIdType(){
        return idType;
    }

    public ResponseFuture getResponseFuture(){
        return responseFuture;
    }
}
