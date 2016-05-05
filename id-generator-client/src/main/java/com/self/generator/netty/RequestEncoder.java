package com.self.generator.netty;

import com.self.generator.core.Request;
import com.self.generator.core.ResponseFuture;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 请求编码器
 * Created by shaojieyue on 11/15/15.
 */
public class RequestEncoder extends MessageToByteEncoder<Request>{
    private final BlockingQueue<ResponseFuture> queue;

    public RequestEncoder(BlockingQueue<ResponseFuture> queue){
        this.queue = queue;
        if(queue == null){
            throw new IllegalArgumentException("queue is null");
        }
    }

    @Override protected void encode(ChannelHandlerContext ctx, Request msg, ByteBuf out)
        throws Exception{
        final ResponseFuture responseFuture = msg.getResponseFuture();
        boolean success = false ;
        try {
            //将请求放入队列,等待响应
            success = queue.offer(responseFuture, 2, TimeUnit.SECONDS);
            if(success){//放入队列成功
                out.writeInt(msg.getIdType());//写入请求
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            if(!success){//没有插入成功,则直接设置为失败
                responseFuture.fail(new TimeoutException("request timeout"));
            }
        }
    }
}
