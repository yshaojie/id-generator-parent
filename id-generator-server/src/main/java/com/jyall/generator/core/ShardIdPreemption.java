package com.jyall.generator.core;

import com.google.common.base.Charsets;
import com.jyall.commons.jackson.JsonUtil;
import com.jyall.generator.common.zookeeper.ZKClient;
import com.jyall.generator.common.zookeeper.ZKPaths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分片id抢占器
 * Created by shaojieyue on 11/8/15.
 */
public class ShardIdPreemption{
    public static final Logger logger = LoggerFactory.getLogger(ShardIdPreemption.class);
    private final InterProcessMutex lock;
    private final String clientName;
    private final long waitTime;//等待锁时间
    private ZKClient client;
    private String host;
    private int port;
    public ShardIdPreemption(ZKClient client, String host, int port){
        lock = new InterProcessMutex(client.getCuratorClient(), ZKPaths.LOCK_PATH);
        this.waitTime = 10;
        this.clientName= host+":"+port;
        this.client = client;
        this.host = host;
        this.port = port;
    }

    /**
     * 抢占shardId
     * @return
     * @throws Exception
     */
    public int preemption() throws Exception{
        int shardId = -1;
        logger.info(clientName+" wait lock.");
        //获取锁,直到超时
        if(!lock.acquire(waitTime, TimeUnit.SECONDS)){
            throw new IllegalStateException(clientName+" could not acquire the lock");
        }
        try{
            logger.info(clientName+" has the lock");
            //获取shardId
            shardId = getShardId();
        }finally{
            logger.info(clientName+" releasing the lock");
            lock.release(); // always release the lock in a finally block
        }
        return shardId;
    }

    private int getShardId() throws Exception{
        int shardId = -1;
        //已经注册的服务列表
        final List<String> childs =
            client.getCuratorClient().getChildren().forPath(ZKPaths.SHARD_ID_PATH);
        Set<Integer> set = new HashSet<Integer>();
        for(String child : childs){
            set.add(Integer.valueOf(child));
        }
        logger.info("registered shard id={}", JsonUtil.toJson(set));
        for(int i = 0;; i++){//循环遍历,直到找到一个未被占用的shardId
            if(!set.contains(i)){//说明该shardId没有被注册
                shardId = i;
                //通过创建临时节点来标示占用shardId
                client.getCuratorClient().create()
                        .creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                        .forPath(ZKPaths.SHARD_ID_PATH+"/"+i,joinPreemptionData(shardId).getBytes(
                            Charsets.UTF_8));
                break;
            }
        }
        if(shardId <0){//抢占shardId失败
            throw new IllegalStateException("preemption shardId fail.");
        }
        logger.info("preemption shard id={}", shardId);
        return shardId;
    }

    /**
     * 组装抢占数据
     * @return
     */
    private String joinPreemptionData(int shardId){
        Map dataMap = new HashMap();
        dataMap.put("host", host);
        dataMap.put("port", port);
        dataMap.put("shardId", shardId);
        dataMap.put("preemptionTime", System.currentTimeMillis());
        return JsonUtil.toJson(dataMap);
    }
}
