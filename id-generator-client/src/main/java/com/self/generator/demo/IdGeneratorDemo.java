package com.self.generator.demo;

import com.self.generator.netty.IdGeneratorNettyClient;
import com.self.generator.core.WaitException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by shaojieyue on 11/15/15.
 */
public class IdGeneratorDemo{
    public static void main(String[] args) throws WaitException, InterruptedException{
        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        int port = 37451;
        String host = "localhost";
        final IdGeneratorNettyClient idGeneratorNettyClient = new IdGeneratorNettyClient(host, port);
        for(int i = 0; i<100; i++){
            executorService.submit(new Runnable(){
                public void run(){
                    final long commonId;
                    try{
                        commonId = idGeneratorNettyClient.nextOrderId();
                        System.out.println(commonId);
                    }catch(WaitException e){
                        e.printStackTrace();
                    }
                }
            });
        }

    }
}
