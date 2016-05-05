package com.self.generator.netty;

import com.self.generator.common.ErrorCodes;
import com.self.generator.common.exception.ServerInternalException;
import com.self.generator.common.exception.UnknowRequestException;
import com.self.generator.core.ResponseFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * id generator 客户端处理逻辑
 * Created by shaojieyue on 11/15/15.
 */
public class IdGeneratorClientHandler extends SimpleChannelInboundHandler<Long>{
    public static final Logger logger = LoggerFactory.getLogger(IdGeneratorClientHandler.class);
    private final BlockingQueue<ResponseFuture> queue;

    public IdGeneratorClientHandler(BlockingQueue<ResponseFuture> queue){
        this.queue = queue;
        if(queue == null){
            throw new IllegalArgumentException("queue is null");
        }
    }

    @Override protected void channelRead0(ChannelHandlerContext ctx, Long result) throws Exception{
        final ResponseFuture responseFuture = queue.take();
        if(responseFuture.isCancel()){//请求取消
            logger.info("discard id="+result);
            return;
        }
        if(result == null || result < 0){//返回结果不正常
            Exception exception = null;
            if(result ==ErrorCodes.BAD_REQUEST){//不知道请求类型异常
                exception = new UnknowRequestException();
            }else if(result == ErrorCodes.SERVER_INTERNAL_ERROR){//服务端内部错误
                exception = new ServerInternalException();
            }else {//不知到的异常,正常不应该执行到此处
                exception = new Exception("unknow exception,errorCode="+result);
            }
            responseFuture.fail(exception);
        }else {
            responseFuture.completed(result);
        }
    }

    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        throws Exception{
        final ResponseFuture responseFuture = queue.take();
        if(responseFuture!=null){
            responseFuture.fail(cause);
        }
    }
}
