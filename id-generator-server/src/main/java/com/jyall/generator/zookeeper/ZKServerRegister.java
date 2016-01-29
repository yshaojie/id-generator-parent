package com.jyall.generator.zookeeper;

import com.google.common.base.Charsets;
import com.jyall.commons.jackson.JsonUtil;
import com.jyall.generator.core.ServerRegister;

import com.jyall.generator.common.RegisterBean;
import com.jyall.generator.common.zookeeper.ZKPaths;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * zk 注册中心实现 Created by shaojieyue on 1i1/8/15.
 */
public class ZKServerRegister implements ServerRegister{
    public static final Logger logger = LoggerFactory.getLogger(ZKServerRegister.class);
    private CuratorFramework curatorFramework;
    private int shardId;

    public ZKServerRegister(CuratorFramework curatorFramework, int shardId){
        this.curatorFramework = curatorFramework;
        this.shardId = shardId;
    }

    public boolean register(String ip, int port){
        String path = ZKPaths.SERVER_PATH+"/"+shardId;
        //要注册的数据
        final String data = joinRegisterData(ip, port);
        boolean success = true;
        try{
            final String result = curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(
                path, data.getBytes(Charsets.UTF_8));
            logger.info("zk register,create path result={}", result);
        }catch(Exception e){
            success = false;
            logger.error("id generator server register fail.", e);
        }
        logger.info("id generator server result={}", success);
        return success;
    }

    /**
     * 组装注册数据
     * @param ip
     * @param port
     * @return
     */
    private String joinRegisterData(String ip, int port){
        RegisterBean registerBean = new RegisterBean();
        registerBean.setHost(ip);
        registerBean.setPort(port);
        registerBean.setShardId(shardId);
        registerBean.setRegisterTime(System.currentTimeMillis());
        return JsonUtil.toJson(registerBean);
    }
}
