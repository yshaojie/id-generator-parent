package com.self.generator;

import com.google.common.base.Preconditions;
import com.self.generator.common.RegisterBean;
import com.self.generator.common.utils.JsonUtil;
import com.self.generator.common.zookeeper.ZKClient;
import com.self.generator.common.zookeeper.ZKPaths;
import com.self.generator.core.IdGeneratorClient;
import com.self.generator.core.WaitException;
import com.self.generator.netty.IdGeneratorNettyClient;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by shaojieyue
 * Create at 2016-01-22 14:15
 */
public class DistributedIdGeneratorClient extends AbstarctIdGeneratorClient {
    private static final Logger logger = LoggerFactory.getLogger(DistributedIdGeneratorClient.class);
    private ZKClient zkClient = null;
    private volatile List<AbstarctIdGeneratorClient> clients;
    private volatile ConcurrentMap<String,Integer> pathIndexMap;
    private AtomicLong counter = new AtomicLong();
    public DistributedIdGeneratorClient(ZKClient zkClient) {
        this.zkClient = zkClient;
        this.pathIndexMap = new ConcurrentHashMap<String, Integer>();
        Preconditions.checkNotNull(zkClient);
        try {
            init();
        } catch (Exception e) {
            throw new RuntimeException("inti DistributedIdGeneratorClient fail.",e);
        }
    }

    private void init() throws Exception {
        buildClients();
        //监听id服务列表
        zkClient.childrenSingleWatch(ZKPaths.SERVER_PATH,new IdServerNodeWatcher());
    }

    class IdServerNodeWatcher implements  CuratorWatcher{
        public void process(WatchedEvent event) throws Exception {
            //触发监听后,继续监听该路径
            zkClient.childrenSingleWatch(ZKPaths.SERVER_PATH,new IdServerNodeWatcher());
            logger.info("-==========>event="+event.getType());
            //子节点有变化
            if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                logger.info(ZKPaths.SERVER_PATH+" chilend data change,rebuild clients");
                buildClients();
            }
        }
    }

    private final synchronized void buildClients() throws Exception {
        ConcurrentMap<String,Integer> oldPathIndexMap = this.pathIndexMap;
        List<AbstarctIdGeneratorClient> oldClients = this.clients;
        ConcurrentMap<String,Integer> newPathIndexMap = new ConcurrentHashMap<String, Integer>();
        List<AbstarctIdGeneratorClient> newClients = new ArrayList<AbstarctIdGeneratorClient>();
        int newIndex = -1;
        final List<String> serverNodes = zkClient.getCuratorClient().getChildren().forPath(ZKPaths.SERVER_PATH);
        for (String serverNode : serverNodes) {//所有id服务列表
            final String path = ZKPaths.SERVER_PATH + "/" + serverNode;
            final Integer index = oldPathIndexMap.get(path);
            newIndex ++;
            AbstarctIdGeneratorClient idGeneratorClient;
            if (index != null) {//服务存在
                idGeneratorClient = clients.get(index);
            }else {//服务不存在,说明是新的id服务连接上来,需要创建新的连接

                final String data = zkClient.getData(path);
                final RegisterBean registerBean = JsonUtil.toObject(data);
                idGeneratorClient = new IdGeneratorNettyClient(registerBean.getHost(),registerBean.getPort());
                logger.info("create new client success,client={}",idGeneratorClient.getRemoteAddress());
            }
            newClients.add(newIndex,idGeneratorClient);//添加新的client到指定位置
            newPathIndexMap.put(path,newIndex);//新的zk 服务路径途径的client索引位置
        }

        for (String oldPath : oldPathIndexMap.keySet()) {//遍历旧的,关闭已经下线的服务
            if (!newPathIndexMap.containsKey(oldPath)) {//不包含,说明服务已经关闭,需要关闭client到这个服务的连接
                final Integer oldIndex = oldPathIndexMap.get(oldPath);
                final AbstarctIdGeneratorClient idGeneratorClient = oldClients.get(oldIndex);
                idGeneratorClient.close();
                logger.info("server {} is offline,close connection.",idGeneratorClient.getRemoteAddress());
            }
        }
        this.pathIndexMap = newPathIndexMap;
        this.clients = newClients;

    }



    @Override
    protected synchronized long nextId(int idType) throws WaitException {
        final long count = counter.incrementAndGet();
        if (count > 100000000) {
            counter.set(0);
        }

        final int clientSize = clients.size();
        if (clientSize == 0) {
            throw  new WaitException("no client conn to id server.");
        }
        final long index = count % clientSize;
        AbstarctIdGeneratorClient currentClient = clients.get((int) index);
        long id = -1;
        try {
            id = currentClient.nextId(idType);
        }catch (WaitException e){

        }
        if (id<0) {
            logger.warn("first get id fail. retry other clients. fail address={}",currentClient.getRemoteAddress());
            for (int i = 0; i < clientSize; i++) {
                currentClient = clients.get(i);
                try {
                    id = currentClient.nextId(idType);
                }catch (WaitException e){

                }
                if (id > 0) {
                    break;
                }
            }
        }

        if (id < 0) {
            throw new WaitException("distributed get id fail.");
        }
        return id;
    }

    public void close() throws IOException {
        if (zkClient != null) {
            for (IdGeneratorClient client : clients) {
                client.close();
            }
        }

        if (zkClient!=null) {
            zkClient.getCuratorClient().close();
        }
    }

    public String getRemoteAddress() {
        return null;
    }
}
