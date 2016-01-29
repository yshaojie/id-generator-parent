package com.jyall.generator.core;

/**
 * 服务注册
 * Created by shaojieyue on 11/8/15.
 */
public interface ServerRegister{
    /**
     * 将服务注册到注册中心
     * @param ip 服务id
     * @param port 服务端口
     * @return
     */
    boolean register(String ip,int port);
}
