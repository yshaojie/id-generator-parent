package com.self.generator.demo;

import com.self.generator.DistributedIdGeneratorClient;
import com.self.generator.common.zookeeper.ZKClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    public static void main(String[] args) throws InterruptedException, IOException {
        ZKClient zkClient = ZKClient.getClient("localhost:2181");
        final DistributedIdGeneratorClient client = new DistributedIdGeneratorClient(zkClient);
        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10,
                1L, TimeUnit.HOURS,
                new LinkedBlockingQueue<Runnable>(1000000));
        final AtomicLong count = new AtomicLong();
        final AtomicLong count1 = new AtomicLong();
        final File target = new File("/home/shaojieyue/mm.txt");
        target.deleteOnExit();
        target.createNewFile();
        final FileWriter fileWriter = new FileWriter(target);
        final long start = System.currentTimeMillis();
        for (int i = 0; ; i++) {
            try {
                threadPoolExecutor.submit(new Runnable() {
                    public void run() {
                        try {
                            final long id = client.nextCommonId();
//                            fileWriter.write(id+"\n");
                            if (id > 0) {
                                final long counter = count.incrementAndGet();
                                if (counter % 100000 == 0) {
                                    System.out.println("============>"+counter);
                                    fileWriter.flush();
                                }
//                                if (counter == 100000) {
//                                    fileWriter.close();
//                                    System.out.println("----------->"+(System.currentTimeMillis()-start));
//                                }
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }catch (Throwable e){}
        }

    }
}
