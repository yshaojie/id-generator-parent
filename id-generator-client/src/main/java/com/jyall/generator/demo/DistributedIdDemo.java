package com.jyall.generator.demo;

import com.jyall.generator.DistributedIdGeneratorClient;
import com.jyall.generator.common.zookeeper.ZKClient;
import com.jyall.generator.core.WaitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by shaojieyue
 * Create at 2016-01-22 15:26
 */
public class DistributedIdDemo {
    private static final Logger logger = LoggerFactory.getLogger(DistributedIdDemo.class);

    public static void main(String[] args) throws InterruptedException {
        ZKClient zkClient = ZKClient.getClient("localhost:2181");
        final DistributedIdGeneratorClient client = new DistributedIdGeneratorClient(zkClient);
        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10,
                1L, TimeUnit.HOURS,
                new LinkedBlockingQueue<Runnable>(100));
        final AtomicLong count = new AtomicLong();
        final AtomicLong count1 = new AtomicLong();
        while (true){
//            final long dd = count1.incrementAndGet();
//            if (dd % 10 == 0) {
//                System.out.println("===========>dd="+dd);
//            }
            try {
                threadPoolExecutor.submit(new Runnable() {
                    public void run() {
                    try {
                        final long id = client.nextCommonId();
                        if (id > 0) {
                            final long counter = count.incrementAndGet();
                            if (counter % 100 == 0) {
                                System.out.println("============>"+counter);
                            }
                        }
//            System.out.println(id);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    }
                });
            }catch (Throwable e){}

        }

    }
}
