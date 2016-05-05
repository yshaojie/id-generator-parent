package com.self.generator;

import com.self.generator.core.ShardIdPreemption;
import com.self.generator.common.zookeeper.ZKClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by shaojieyue on 11/8/15.
 */
public class ShardIdPreemptionTest extends TestCase{
    ShardIdPreemption shardIdPreemption;
    @Before
    public void init(){
        System.out.println("初始化");
        shardIdPreemption = new ShardIdPreemption(ZKClient.getClient("localhost:2181"),"127.0.0.1",
            2432);
    }

    @Test
    public void test_preemption() throws Exception{
        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        final AtomicInteger count = new AtomicInteger();
        for(int i = 0; i<10; i++){
            executorService.submit(new Runnable(){
                @Override public void run(){
                    final int andIncrement = count.getAndIncrement();
                    final ZKClient client = ZKClient.getClient("10.10.20.50:2181");
                    shardIdPreemption = new ShardIdPreemption(client,"127.0.0.1", 23423+andIncrement);
                    try{
                        final int preemption = shardIdPreemption.preemption();
                        System.out.println("preemption="+preemption);
                        Assert.assertEquals(andIncrement,preemption);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });

        }
        Thread.sleep(400*1000);
    }
}
