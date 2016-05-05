package com.self.generator.common.zookeeper;

/**
 * Created by shaojieyue on 11/8/15.
 */
public class ZKPaths{
    //分布式锁路径
    public static final String LOCK_PATH = "/id_generator/lock";
    //注册服务目录
    public static final String SERVER_PATH = "/id_generator/servers";
    //抢占的shard id目录
    public static final String SHARD_ID_PATH = "/id_generator/shard_ids";
}
