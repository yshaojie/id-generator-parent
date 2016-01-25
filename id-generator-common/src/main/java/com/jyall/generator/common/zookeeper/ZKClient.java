package com.jyall.generator.common.zookeeper;

import com.google.common.base.Function;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shaojieyue on 11/8/15.
 */
public class ZKClient{
    private   CuratorFramework zkClient ;
    private final Logger logger = LoggerFactory.getLogger(ZKClient.class);
    private static final ConcurrentMap<String,ZKClient> clientCache = new ConcurrentHashMap();
    public static final synchronized ZKClient getClient(String connectionString){
        ZKClient zkClient = clientCache.get(connectionString);
        if(zkClient==null){
            final CuratorFramework curatorFramework = newZkClient(connectionString);
            zkClient = new ZKClient(curatorFramework);
            final ZKClient previous = clientCache.put(connectionString, zkClient);
            if(previous!=null){//如果包含了,则关闭连接
                previous.getCuratorClient().close();
            }
        }
        return zkClient;
    }



    public boolean delete(String path){
        boolean success = false;
        try{
            if(zkClient.checkExists().forPath(path)!=null){
                logger.info("delete the node "+path);
                zkClient.delete().forPath(path);
                success = true;
            }else{
                logger.warn("the node "+path+" not exist,delete fail.");
            }
        }catch(Exception e){
            logger.error("exceprion", e);
        }

        return success;
    }

    public void execute(Function f){
        f.apply(zkClient);
    }

    public final String getData(String path){
        if(path==null){
            return null;
        }

        String ret = null;
        try{
            //检测节点是否存在
            Stat stat = zkClient.checkExists().forPath(path);
            if(stat==null){
                logger.warn("path="+path+" is not exist");
                return null;
            }

            byte[] data = zkClient.getData().forPath(path);
            if(data!=null){
                ret = new String(data);
            }
        }catch(Exception e){
            logger.error("zk error", e);
        }
        return ret;
    }

    /**
     * 为节点添加单个watch，如果已经存在，则不添加
     *
     * @throws Exception
     */
    public synchronized void dataSingleWatch(String path, CuratorWatcher watcher){
        if(!checkDataWatcher(path)){
            logger.info("watch path="+path);
            try{
                zkClient.getData().usingWatcher(watcher).forPath(path);
            }catch(Exception e){
                try{
                    if(!checkDataWatcher(path)){
                        zkClient.getChildren().usingWatcher(watcher).forPath(path);
                    }
                }catch(Exception e1){
                    logger.error("ex", e);
                }
            }
        }else{
            logger.warn("path="+path+" hased data watch,add watch canel");
        }
    }

    /**
     * 为节点添加单个watch，如果已经存在，则不添加
     *
     * @throws Exception
     */
    public synchronized void childrenSingleWatch(String path, CuratorWatcher watcher){
        if(!checkChildrenWatcher(path)){
            logger.info("watch path="+path);
            try{
                zkClient.getChildren().usingWatcher(watcher).forPath(path);
            }catch(Exception e){
                if(!checkChildrenWatcher(path)){
                    try{
                        zkClient.getChildren().usingWatcher(watcher).forPath(path);
                    }catch(Exception e1){
                        logger.error("ex", e);
                    }
                }
            }
        }else{
            logger.warn("path="+path+" hased children watch,add watch canel");
        }
    }

    /**
     * 为节点添加单个watch，如果已经存在，则不添加
     *
     * @throws Exception
     */
    public synchronized void existSingleWatch(String path, CuratorWatcher watcher){
        if(!checkExistWatcher(path)){
            logger.info("watch path="+path);
            try{
                zkClient.checkExists().usingWatcher(watcher).forPath(path);
            }catch(Exception e){
                if(!checkExistWatcher(path)){
                    try{
                        zkClient.checkExists().usingWatcher(watcher).forPath(path);
                    }catch(Exception e1){
                        logger.error("ex", e);
                    }
                }
            }
        }else{
            logger.warn("path="+path+" hased children watch,add watch canel");
        }
    }

    /**
     * 检查该节点是否存在data watch
     */
    public boolean checkDataWatcher(String path){
        try{
            //ZooKeeper zoo = zkClient.getZookeeperClient().getZooKeeper();
            //List<String> dataWatches = zoo.getDataWatches();
            //return dataWatches.contains(path);
        }catch(Exception e){
            logger.error("exceprion", e);
        }
        return false;
    }

    /**
     * 检查该节点是否存在 child 节点
     */
    public boolean checkChildrenWatcher(String path){
        try{
            //ZooKeeper zoo = zkClient.getZookeeperClient().getZooKeeper();
            //List<String> dataWatches = zoo.getChildWatches();
            //return dataWatches.contains(path);
        }catch(Exception e){
            logger.error("exceprion", e);
        }
        return false;
    }

    /**
     * 检查该节点是否存exist监听
     */
    public boolean checkExistWatcher(String path){
        //try {
        //    ZooKeeper zoo = zkClient.getZookeeperClient().getZooKeeper();
        //    List<String> dataWatches = zoo.getExistWatches();
        //    return dataWatches.contains(path);
        //} catch (Exception e) {
        //    logger.error("exceprion", e);
        //}
        return false;
    }

    private static final CuratorFramework newZkClient(String connectionString){
        // these are reasonable arguments for the ExponentialBackoffRetry.
        // The first retry will wait 1 second - the second will wait up to 2 seconds - the
        // third will wait up to 4 seconds.
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // The simplest way to get a CuratorFramework instance. This will use default values.
        // The only required arguments are the connection string and the retry policy
        final CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
        return curatorFramework;
    }

    /**
     * 获取CuratorFramework
     * @return
     */
    public  final CuratorFramework getCuratorClient(){
        return zkClient;
    }

    public ZKClient(CuratorFramework zkClient){
        this.zkClient = zkClient;
        this.zkClient.start();
    }
}
