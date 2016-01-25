package com.jyall.generator.common.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

/**
 * Created by shaojieyue on 11/8/15.
 */
public class ShardIdLocks{
    private final InterProcessMutex lock;
    private final String clientName;

    public ShardIdLocks(CuratorFramework curatorFramework, String clientName){
        this.clientName = clientName;
        this.lock = new InterProcessMutex(curatorFramework,ZKPaths.LOCK_PATH);
    }
}
